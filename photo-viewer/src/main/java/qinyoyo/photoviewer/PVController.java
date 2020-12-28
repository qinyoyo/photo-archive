package qinyoyo.photoviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import qinyoyo.utils.DateUtil;
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
        List<PhotoInfo> list = archiveInfo.getInfos().stream()
                .filter(p->
                        folder.equals(p.getSubFolder()) && p.getMimeType()!=null && p.getMimeType().contains(mime)
                ).collect(Collectors.toList());
        if (mime.equals("html")) {  // 游记文件，特殊处理
            List<PhotoInfo> list1 = archiveInfo.getInfos().stream()
                    .filter(p->p.getFileName().equals("index.html") && p.getSubFolder().endsWith(".web") &&
                            p.getMimeType()!=null && p.getMimeType().contains(mime) &&
                            folder.equals(p.getSubFolder().lastIndexOf(File.separator) >=0 ? p.getSubFolder().substring(0,p.getSubFolder().lastIndexOf(File.separator)) : "")
                    ).collect(Collectors.toList());
            if (list1!=null && list1.size()>0) {
                if (list!=null) {
                    list.addAll(list1);
                    return list;
                }   else return list1;
            } else return list;
        } else return list;
    }

    void createThumbs(String thumbPath, String imgPath) {
        ImageUtil.compressImage(imgPath, thumbPath, 300, 200);
    }

    void createThumbsList(List<PhotoInfo> photos,String key) {
        if (photos!=null && photos.size()>0) {
            final List<PhotoInfo> noThumbs = photos.stream().filter(p->!new File(p.fullPath(rootPath+File.separator+".thumb")).exists()).collect(Collectors.toList());
            if (noThumbs!=null && noThumbs.size()>0 && !threadPathList.contains(key)) {
                threadPathList.add(key);
                new Thread() {
                    @Override
                    public void run() {
                        for (PhotoInfo p : noThumbs) createThumbs(p.fullPath(rootPath+File.separator+".thumb"),p.fullPath(rootPath));
                    }
                }.start();
            }
        }
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
                    if (pathname.getName().startsWith(".") || pathname.getName().endsWith(".web")) return false;
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
                createThumbsList(photos,path);
                model.addAttribute("photos",photos);
            }
        }
        return "index";
    }
    private String join(String sep,String ... strings) {
        StringBuilder sb=new StringBuilder();
        boolean first = true;
        for (String s : strings) {
            if (s!=null) {
                if (first) {
                    sb.append(s);
                    first = false;
                } else sb.append(sep).append(s);
            }
        }
        return sb.toString();
    }
    private boolean photoInfoContains(PhotoInfo p,String text) {
        String s = p.getFileName();
        if (s!=null && s.toLowerCase().contains(text)) return true;
        s = join(" ", p.getCountry(),p.getProvince(),p.getCity(),p.getLocation(),p.getSubjectCode(),p.getHeadline(),p.getSubTitle(),
                DateUtil.date2String(p.getShootTime(),null),p.getMake(),p.getModel(),p.getLens());
        if (s!=null && s.toLowerCase().contains(text)) return true;
        return false;
    }
    @RequestMapping(value = "search")
    public String doSearch(Model model, HttpServletRequest request, HttpServletResponse response, String text) {
        if (text == null || text.trim().isEmpty()) return getFolder(model, request, response, "");
        text = text.trim().toLowerCase();
        model.addAttribute("separator", File.separator);
        List<String> dirs = new ArrayList<>();
        List<PhotoInfo> htmls = new ArrayList<>();
        List<PhotoInfo> videos = new ArrayList<>();
        List<PhotoInfo> audios = new ArrayList<>();
        List<PhotoInfo> photos = new ArrayList<>();
        for (PhotoInfo p : archiveInfo.getInfos()) {
            if (p.getSubFolder().toLowerCase().contains(text) && !dirs.contains(p.getSubFolder()))
                dirs.add(p.getSubFolder());
            if (photoInfoContains(p,text)) {
                String mime = p.getMimeType();
                if (mime==null) continue;
                if (mime.contains("html")) htmls.add(p);
                else if (mime.contains("audio")) audios.add(p);
                else if (mime.contains("video")) videos.add(p);
                else if (mime.contains("image")) photos.add(p);
            }
        }
        if (dirs.size() > 0) {
            List<Map<String, Object>> subDirectories = new ArrayList<>();
            for (String dir : dirs) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", dir.replace(File.separator," > "));
                map.put("path", dir);
                subDirectories.add(map);
            }
            subDirectories.sort((a, b) -> a.get("name").toString().compareTo(b.get("name").toString()));
            model.addAttribute("subDirectories", subDirectories);
        }
        if (htmls!=null && htmls.size()>0)  model.addAttribute("htmls",htmls);
        if (videos!=null && videos.size()>0)  model.addAttribute("videos",videos);
        if (audios!=null && audios.size()>0)  model.addAttribute("audios",audios);
        if (photos!=null && photos.size()>0) {
            createThumbsList(photos,text);
            model.addAttribute("photos",photos);
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
