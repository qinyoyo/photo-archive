package qinyoyo.photoviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import qinyoyo.utils.SpringContextUtil;
import tang.qinyoyo.archive.ArchiveInfo;
import tang.qinyoyo.archive.ImageUtil;
import tang.qinyoyo.archive.PhotoInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PVController implements ApplicationRunner {
    @Autowired
    private Environment env;
    private String rootPath;
    private ArchiveInfo archiveInfo;

    List<PhotoInfo> mimeListInPath(String mime, String folder) {
        return archiveInfo.getInfos().stream()
                .filter(p->
                        folder.equals(p.getSubFolder()) && p.getMimeType()!=null && p.getMimeType().contains(mime)
                ).collect(Collectors.toList());
    }

    void createThumbs(String thumbPath, String imgPath) {
        ImageUtil.compressImage(imgPath, thumbPath, 300, 200);
    }

    @RequestMapping(value = "thumbnail")
    public String getThumbnail(HttpServletRequest request, HttpServletResponse response, String path) {
        return null;
    }
    List<String> threadPathList = new ArrayList<>();
    @RequestMapping(value = "/")
    public String getFolder(Model model, HttpServletRequest request, HttpServletResponse response, String path) {
        model.addAttribute("separator",File.separator);
        if (path!=null && !path.isEmpty()) {
            model.addAttribute("pathNames",path.split(File.separator.equals("\\")?"\\\\" : File.separator));
        } else path = "";
        File dir = new File(rootPath+File.separator+path);
        if (dir.exists() && dir.isDirectory()) {
            // 子目录
            File[] subDirs = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().startsWith(".")) return false;
                    return pathname.isDirectory();
                }
            });
            if (subDirs!=null && subDirs.length>0) {
                List<Map<String,Object>> subDirectories = new ArrayList<>();
                for (File f : subDirs) {
                    Map<String,Object> map = new HashMap<>();
                    map.put("name",f.getName());
                    try {
                        map.put("path", f.getCanonicalPath().substring(rootPath.length()+1));
                    }catch (Exception e) {}
                    subDirectories.add(map);
                }
                subDirectories.sort((a,b)->a.get("name").toString().compareTo(b.get("name").toString()));
                model.addAttribute("subDirectories",subDirectories);
            }
            // html文件
            List<PhotoInfo> htmls = mimeListInPath("html",path);
            if (htmls!=null && htmls.size()>0)  model.addAttribute("htmls",htmls);

            // video文件
            List<PhotoInfo> videos = mimeListInPath("video",path);
            if (videos!=null && videos.size()>0)  model.addAttribute("videos",videos);

            // audio文件
            List<PhotoInfo> audios = mimeListInPath("audio",path);
            if (audios!=null && audios.size()>0)  model.addAttribute("audios",audios);

            //Photo Info
            List<PhotoInfo> photos = mimeListInPath("image",path);
            if (photos!=null && photos.size()>0) {
                final List<PhotoInfo> noThumbs = photos.stream().filter(p->!new File(p.fullPath(rootPath+File.separator+".thumb")).exists()).collect(Collectors.toList());
                if (noThumbs!=null && noThumbs.size()>0 && !threadPathList.contains(path)) {
                    threadPathList.add(path);
                    new Thread() {
                        @Override
                        public void run() {
                            for (PhotoInfo p : noThumbs) createThumbs(p.fullPath(rootPath+File.separator+".thumb"),p.fullPath(rootPath));
                        }
                    }.start();
                }
                model.addAttribute("photos",photos);
            }
        }
        return "index";
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        rootPath = env.getProperty("photo.root-path");
        if (rootPath==null) rootPath = SpringContextUtil.getProjectHomeDirection();
        archiveInfo = new ArchiveInfo(rootPath);
        if (!archiveInfo.isReadFromFile()) {
            archiveInfo.sortInfos();
            archiveInfo.saveInfos();
        }
        rootPath = archiveInfo.getPath();  // 标准化
    }
}
