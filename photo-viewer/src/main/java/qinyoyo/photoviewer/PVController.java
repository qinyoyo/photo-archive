package qinyoyo.photoviewer;


import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import qinyoyo.utils.BaiduGeo;
import qinyoyo.utils.DateUtil;
import qinyoyo.utils.SpringContextUtil;
import qinyoyo.utils.Util;
import tang.qinyoyo.ArchiveUtils;
import tang.qinyoyo.archive.ArchiveInfo;
import tang.qinyoyo.archive.PhotoInfo;
import tang.qinyoyo.exiftool.CommandRunner;
import tang.qinyoyo.exiftool.ExifTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
public class PVController implements ApplicationRunner {
    @Autowired
    private Environment env;
    private String rootPath;
    private ArchiveInfo archiveInfo;
    private boolean isReady = false;
    private boolean isDebug = false;
    private boolean canRemove = false;
    private boolean noVideoThumb = false;
    private boolean htmlEditable = false;
    private boolean favoriteFilter = false;
    private int loopTimer = 4000;
    public static final String STDOUT = "stdout.log";
    private static  Logger logger = Logger.getLogger("PVController");

    public String getRootPath() {
        return rootPath;
    }

    public ArchiveInfo getArchiveInfo() {
        return archiveInfo;
    }

    public boolean isDataReady() {
        return isReady;
    }

    @ResponseBody
    @RequestMapping(value = "favorite")
    public String setFavorite(HttpServletRequest request, HttpServletResponse response, Boolean filter) {
        if (filter!=null) {
            favoriteFilter = filter;
            return "ok";
        } else return "error";
    }

    @RequestMapping(value = "favicon.ico")
    String favicon() {
        return "/static/image/favicon.ico";
    }

    String [] randomMusic = null;
    int randomIndex = 0;
    @RequestMapping(value = "music")
    String switchMusic() {
        if (randomIndex>=0) {
            if (randomMusic==null) {
                File [] mp3s = new File(rootPath,".music").listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".mp3");
                    }
                });
                if (mp3s==null || mp3s.length==0) {
                    randomIndex = -1;
                    return null;
                } else {
                    randomMusic=new String[mp3s.length];
                    for (int i=0;i<mp3s.length;i++) {
                        randomMusic[i]="/.music/"+mp3s[i].getName();
                    }
                }
                randomIndex=-1;
            }
            randomIndex++;
            if (randomIndex>=randomMusic.length) randomIndex=0;
            return randomMusic[randomIndex];
        }  else return null;
    }


    @RequestMapping(value = "/")
    public String getFolder(Model model, HttpServletRequest request, HttpServletResponse response, String path, String newStep) {
        if (!isReady) {
            model.addAttribute("message","Not ready!!!");
            return "error";
        }
        if (newStep!=null && !newStep.isEmpty()) {
            EditorController editorController = SpringContextHolder.getBean(EditorController.class);
            editorController.createHtmlFile(rootPath,path,newStep,archiveInfo);
        }
        commonAttribute(model,request);
        Map<String, Object> res = getPathAttributes(path, false);
        model.addAllAttributes(res);
        if (path!=null && !path.isEmpty() &&
                 (new File(rootPath+File.separator+path+File.separator+".need-scan").exists() ||
                    (res.get("htmls")==null && !archiveInfo.getInfos().stream().anyMatch(p->
                        p.getSubFolder().equals(path) ||
                        p.getSubFolder().startsWith(path+File.separator)                )
                    )
                 )
           ) model.addAttribute("needScan",true);
        setBackgroundMusic(model,path);
        return "index";
    }

    @RequestMapping(value = "play")
    public String playFolder(Model model, HttpServletRequest request, HttpServletResponse response, String path, Integer index) {
        if (!isReady) {
            model.addAttribute("message","Not ready!!!");
            return "error";
        }
        commonAttribute(model,request);
        model.addAttribute("debug",false);
        model.addAttribute("canRemove",false);
        model.addAttribute("htmlEditable",false);
        if (loopTimer==0) model.addAttribute("loopTimer",4000);
        model.addAttribute("loopPlay",true);
        model.addAttribute("startFrom",index);
        model.addAllAttributes(getPathLoopImages(path));
        setBackgroundMusic(model,path);
        return "index";
    }

    @RequestMapping(value = "search")
    public String doSearch(Model model, HttpServletRequest request, HttpServletResponse response, String text) {
        if (!isReady) {
            model.addAttribute("message","Not ready!!!");
            return "error";
        }
        commonAttribute(model,request);
        if (text == null || text.trim().isEmpty()) return getFolder(model, request, response, "", null);
        text = text.trim().toLowerCase();
        List<String> dirs = new ArrayList<>();
        List<PhotoInfo> htmls = new ArrayList<>();
        List<PhotoInfo> videos = new ArrayList<>();
        List<PhotoInfo> audios = new ArrayList<>();
        List<PhotoInfo> photos = new ArrayList<>();
        for (PhotoInfo p : archiveInfo.getInfos()) {
            if (p.getSubFolder().toLowerCase().contains(text) && !p.getSubFolder().toLowerCase().endsWith(".web") && !dirs.contains(p.getSubFolder()))
                dirs.add(p.getSubFolder());
            if (photoInfoContains(p,text)) {
                String mime = p.getMimeType();
                if (mime==null) continue;
                if (mime.contains("html")) htmls.add(p);
                else if (mime.contains("audio")) audios.add(p);
                else if (mime.contains("video")) videos.add(p);
                else if (mime.contains("image") && (!favoriteFilter || (p.getRating()!=null && p.getRating()==5))) photos.add(p);
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
            model.addAttribute("photos",photos);
        }
        setBackgroundMusic(model,null);
        return "index";
    }

    @ResponseBody
    @RequestMapping(value = "orientation")
    public String orientation(HttpServletRequest request, String path, Integer [] orientations, Integer rating) {
        if (!isReady) {
            return "error";
        }
        if (path==null) return "error";
        PhotoInfo pi = archiveInfo.find(new File(rootPath + File.separator + path));
        if (pi==null) return "error";
        if (pi.modifyOrientation(rootPath, rating, orientations)) {
            afterChanged();
            return "ok,"+
                    (pi.getOrientation()==null?"":pi.getOrientation())+
                    ","+(pi.getRating()==null?"0":pi.getRating());
        }
        else return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "share")
    public String share(HttpServletRequest request, HttpServletResponse response, String path) {
        if (!isReady) return "error";
        if (path==null) return "error";
        File src = new File(rootPath , path);
        if (src.exists()) {
            File target = new File(rootPath+File.separator+".share",src.getName());
            target.getParentFile().mkdirs();
            try {
                Files.copy(src.toPath(), target.toPath());
                return "ok";
            } catch (Exception e) {}
        }
        return "error";
    }

    @ResponseBody
    @RequestMapping(value = "remove")
    public String remove(HttpServletRequest request, HttpServletResponse response, String path) {
        if (!isReady) {
            return "error";
        }
        if (path==null) return "error";
        PhotoInfo pi = archiveInfo.find(new File(rootPath , path));
        if (pi!=null) {
            pi.delete(rootPath);
            archiveInfo.getInfos().remove(pi);
            afterChanged();
            return "ok";
        }
        return "error";
    }


    @ResponseBody
    @RequestMapping(value = "scan")
    public String scan(HttpServletRequest request, HttpServletResponse response, String path) {
        if (path==null || path.isEmpty()) return "error";
        new Thread() {
            @Override
            public void run() {
                File dir = new File(rootPath, path);
                if (dir.exists() && dir.isDirectory()) {
                    ArchiveUtils.removeEmptyFolder(dir);
                    if (dir.exists()) {
                        new File(rootPath+File.separator+path+File.separator+".need-scan").delete();
                        archiveInfo.addFile(dir);
                        archiveInfo.getInfos().stream().filter(p->p.getSubFolder().startsWith(path)).forEach(p->archiveInfo.createThumbFiles(p));
                        archiveInfo.sortInfos();
                        archiveInfo.saveInfos();
                    }
                }
            }
        }.start();
        return "ok";
    }

    @ResponseBody
    @RequestMapping(value = "stdout")
    public String stdout(HttpServletRequest request) {
        File file = new File(STDOUT);
        if (request.getQueryString()!=null && request.getQueryString().toLowerCase().contains("truncate")) {
            ArchiveUtils.writeToFile(file,"","UTF8");
            return "ok";
        } else return ArchiveUtils.getFromFile(file,"UTF8").replace("\n","<br>").replaceAll("\\u001B[^m]*m","");
    }

    @ResponseBody
    @RequestMapping(value = "shutdown")
    public String shutdown(HttpServletRequest request, HttpServletResponse response, Integer delay) {
        CommandRunner.shutdown(delay==null?10:delay);
        return "ok";
    }

    private boolean photoInfoContains(PhotoInfo p,String text) {
        String s = p.getFileName();
        if (s!=null && s.toLowerCase().contains(text)) return true;
        s = ArchiveUtils.join(" ", p.getCountry(),p.getProvince(),p.getCity(),p.getLocation(),p.getSubjectCode(),p.getHeadline(),p.getSubTitle(),
                DateUtil.date2String(p.getShootTime(),null),p.getModel(),p.getLens());
        if (s!=null && s.toLowerCase().contains(text)) return true;
        return false;
    }
    List<PhotoInfo> mimeListInPath(String mime, String folder) {
        if (!isReady) return null;
        List<PhotoInfo> list = archiveInfo.getInfos().stream()
                .filter(p->
                        folder.equals(p.getSubFolder()) && p.getMimeType()!=null && p.getMimeType().contains(mime) &&
                                (!favoriteFilter || !mime.equals("image") || (p.getRating()!=null && p.getRating()==5))
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
            } else {
                // 子目录
                File[] subDirs = new File(rootPath,folder).listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (pathname.isDirectory() && pathname.getName().endsWith(".web")) {
                            return new File(pathname,"index.html").exists();
                        } else return false;
                    }
                });
                if (subDirs!=null && subDirs.length>0) {
                    if (list==null) list = new ArrayList<>();
                    for (File d: subDirs) {
                        PhotoInfo pi = new PhotoInfo(rootPath,new File(d,"index.html"));
                        pi.setMimeType("text/html");
                        pi.setSubTitle(d.getName().substring(0,d.getName().length()-4));
                        list.add(pi);
                    }
                }
                return list;
            }
        } else return list;
    }


    public static void initStatics(final Object model) {
        BeansWrapper wrapper = new BeansWrapper(new Version(2, 3, 27));
        TemplateModel statics = wrapper.getStaticModels();
        if (model instanceof Model) ((Model)model).addAttribute("statics", statics);
        else if (model instanceof Map) ((Map)model).put("statics", statics);
    }

    private boolean isMobile(HttpServletRequest request) {
        String userAgent = request.getHeader("USER-AGENT");
        return userAgent.contains("Mobile") || userAgent.contains("Phone");
    }
    private void commonAttribute(Model model, HttpServletRequest request) {
        initStatics(model);
        String params = request.getQueryString();
        String userAgent = request.getHeader("USER-AGENT");
        if (isMobile(request)) {
            model.addAttribute("isMobile",true);
            String browsers = env.getProperty("photo.support-orientation");
            boolean supportOrientation = false;
            if (browsers!=null) {
                String [] bs = browsers.split(",");
                for (String b:bs) {
                    if (userAgent.contains(b)) {
                        supportOrientation = true;
                        break;
                    }
                }
            }
            if (!supportOrientation || (params != null && params.contains("orientation"))) {
                model.addAttribute("orientation", true);
            }
        }
        if (isDebug || (params!=null && params.contains("debug"))) model.addAttribute("debug",true);
        if (canRemove) model.addAttribute("canRemove",true);
        if (noVideoThumb) model.addAttribute("noVideoThumb",true);
        if (favoriteFilter) model.addAttribute("favoriteFilter",true);
        if (htmlEditable && !isMobile(request)) model.addAttribute("htmlEditable",true);
        model.addAttribute("loopTimer",loopTimer);
    }

    public Map<String,Object> getPathAttributes(String path, boolean just4ResourceList) {
        Map<String,Object> model = new HashMap<>();
        model.put("separator",File.separator);
        if (path!=null && !path.isEmpty()) {
            model.put("pathNames",path.split("\\\\|/"));
        } else path = "";
        File dir = new File(rootPath+File.separator+path);
        if (dir.exists() && dir.isDirectory()) {
            // 子目录
            File[] subDirs = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().startsWith(".") || (!just4ResourceList && pathname.getName().endsWith(".web"))) return false;
                    return pathname.isDirectory();
                }
            });
            List<Map<String,Object>> subDirectories = new ArrayList<>();
            if (!path.isEmpty()) {
                Map<String,Object> map = new HashMap<>();
                map.put("name","..");
                try {
                    String pp = dir.getParentFile().getCanonicalPath();
                    if (pp.length()<=rootPath.length()) map.put("path","");
                    else map.put("path", pp.substring(rootPath.length()+1));
                    subDirectories.add(map);
                }catch (Exception e) {}
            }
            if (subDirs!=null && subDirs.length>0) {
                for (File f : subDirs) {
                    Map<String,Object> map = new HashMap<>();
                    map.put("name",f.getName());
                    try {
                        map.put("path", f.getCanonicalPath().substring(rootPath.length()+1));
                        subDirectories.add(map);
                    }catch (Exception e) {}
                }
            }
            if (subDirectories.size()>0) {
                subDirectories.sort((a,b)->a.get("name").toString().compareTo(b.get("name").toString()));
                model.put("subDirectories",subDirectories);
            }
            if (!just4ResourceList) {
                // html文件
                List<PhotoInfo> htmls = mimeListInPath("html", path);
                if (htmls != null && htmls.size() > 0) model.put("htmls", htmls);
            }
            // video文件
            List<PhotoInfo> videos = mimeListInPath("video",path);
            if (videos!=null && videos.size()>0)  model.put("videos",videos);

            // audio文件
            List<PhotoInfo> audios = mimeListInPath("audio",path);
            if (audios!=null && audios.size()>0)  model.put("audios",audios);

            //Photo Info
            List<PhotoInfo> photos = mimeListInPath("image",path);
            if (photos!=null && photos.size()>0) {
                model.put("photos",photos);
            }
        }
        return model;
    }

    private boolean loopFilter(PhotoInfo p) {
        return (!favoriteFilter || (p.getRating()!=null && p.getRating()==5)) &&
                p.getMimeType()!=null && p.getMimeType().contains("image/") &&
                !p.getSubFolder().endsWith(".web") && p.getSubFolder().indexOf(".web"+File.separator)<0;
    }
    private Map<String,Object> getPathLoopImages(final String path) {
        Map<String,Object> model = new HashMap<>();
        model.put("separator",File.separator);
        List<PhotoInfo> photos;
        if (path!=null && !path.isEmpty()) {
            model.put("pathNames",path.split("\\\\|/"));
            photos = archiveInfo.getInfos().stream().filter(p ->
                    loopFilter(p) && p.getSubFolder().indexOf(path) == 0
            ).collect(Collectors.toList());
        } else photos = archiveInfo.getInfos().stream().filter(p -> loopFilter(p)
        ).collect(Collectors.toList());
        // photos.sort((a,b)->a.compareTo(b));
        if (photos!=null && photos.size()>0) model.put("photos",photos);
        return model;
    }
    private void setBackgroundMusic(Model model,String path) {
        String bkm = (path==null || path.isEmpty() ? "" : "/"+path)+ "/.music.mp3";
        if (new File(rootPath,bkm).exists()) model.addAttribute("backgroundMusic",bkm.replaceAll("\\\\","/"));
        else {
            String url = switchMusic();
            if (url!=null) {
                model.addAttribute("backgroundMusic",url);
            }
        }
    }

    void afterChanged() {
        new Thread() {
            @Override
            public void run() {
                archiveInfo.saveInfos();
            }
        }.start();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String ffmpeg = env.getProperty("photo.ffmpeg");
        if (ffmpeg!=null) ArchiveInfo.FFMPEG = ffmpeg;
        String exiftool = env.getProperty("photo.exiftool");
        if (exiftool!=null) ExifTool.EXIFTOOL = exiftool;

        new ArchiveInfo();

        loopTimer = Util.null2Default(Util.toInt(env.getProperty("photo.loop-timer")),0);
        isDebug = Util.boolValue(env.getProperty("photo.debug"));
        canRemove = Util.boolValue(env.getProperty("photo.removable"));
        noVideoThumb = Util.boolValue(env.getProperty("photo.no-video-thumb"));
        htmlEditable = Util.boolValue(env.getProperty("photo.html-editable"));

//        if (!isDebug) System.setOut(new PrintStream(new File(STDOUT)));
        new Thread() {
            @Override
            public void run() {
                rootPath = env.getProperty("photo.root-path");
                if (rootPath==null) rootPath = SpringContextUtil.getProjectHomeDirection();
                archiveInfo = new ArchiveInfo(rootPath);
                if (!archiveInfo.isReadFromFile()) {
                    archiveInfo.sortInfos();
                    archiveInfo.scanSameFiles(false);
                    ArchiveUtils.removeEmptyFolder(new File(rootPath));
                    archiveInfo.saveInfos();
                }
                rootPath = archiveInfo.getPath();  // 标准化
                new Thread() {
                        @Override
                        public void run() {
                            archiveInfo.createThumbFiles();
                        }
                    }.start();
                System.out.println("Photo viewer started.");
                isReady = true;
                BaiduGeo.seekAddressInfo(archiveInfo);
            }
        }.start();
    }
}
