package qinyoyo.photoviewer;


import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import qinyoyo.utils.*;
import qinyoyo.photoinfo.ArchiveUtils;
import qinyoyo.photoinfo.archive.Modification;
import qinyoyo.photoinfo.archive.ArchiveInfo;
import qinyoyo.photoinfo.archive.Orientation;
import qinyoyo.photoinfo.archive.PhotoInfo;
import qinyoyo.photoinfo.exiftool.CommandRunner;
import qinyoyo.photoinfo.exiftool.ExifTool;
import qinyoyo.photoinfo.exiftool.Key;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
public class PVController implements ApplicationRunner , ErrorController {
    @Autowired
    private Environment env;
    private String rootPath;
    private ArchiveInfo archiveInfo;
    private boolean isReady = false;
    private boolean noVideoThumb = false;
    private String unlockPassword = "19960802";
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
    @RequestMapping(value = "options")
    public String getOptions(HttpServletRequest request) {
        return SessionOptions.getSessionOptions(request).toString();
    }

    @ResponseBody
    @RequestMapping(value = "login")
    public String login(Model model,HttpServletRequest request, String password, String exifTag,
                   Boolean debug, Boolean htmlEditable ) {
        if (password!=null && password.equals(unlockPassword)) {
            SessionOptions options = SessionOptions.getSessionOptions(request);
            options.setUnlocked(true);
            if (debug!=null) options.setDebug(debug);
            if (htmlEditable!=null) options.setHtmlEditable(htmlEditable);
            if (exifTag==null || exifTag.isEmpty()) {
                options.setRangeTag(Key.SUBJECT_CODE);
            }
            else {
                Optional<Key> ok = Key.findKeyWithName(exifTag);
                if (ok.isPresent()) options.setRangeTag( ok.get());
                else options.setRangeExif(null);
            }
            return "ok";
        } else {
            return "error";
        }
    }

    @ResponseBody
    @RequestMapping(value = "logout")
    public String logout(Model model,HttpServletRequest request) {
        SessionOptions options = SessionOptions.getSessionOptions(request);
        options.setUnlocked(false);
        options.setHtmlEditable(false);
        options.setDebug(false);
        return "ok";
    }

    @ResponseBody
    @RequestMapping(value = "favorite")
    public String setFavorite(HttpServletRequest request, Boolean filter) {
        if (filter!=null) {
            SessionOptions options = SessionOptions.getSessionOptions(request);
            options.setFavoriteFilter(filter);
            return "ok";
        } else return "error";
    }
    @ResponseBody
    @RequestMapping(value = "playBackMusic")
    public String playBackMusic(HttpServletRequest request, Boolean value) {
        if (value!=null) {
            SessionOptions options = SessionOptions.getSessionOptions(request);
            options.setPlayBackMusic(value);
            return "ok";
        } else return "error";
    }
    @ResponseBody
    @RequestMapping(value = "loopTimer")
    public String setLoopTimer(HttpServletRequest request, Integer value) {
        if (value!=null) {
            SessionOptions options = SessionOptions.getSessionOptions(request);
            options.setLoopTimer(value);
            return "ok";
        } else return "error";
    }
    @RequestMapping(value = "favicon.ico")
    String favicon() {
        return "/static/image/favicon.ico";
    }

    String [] randomMusic = null;
    @RequestMapping(value = "music")
    String switchMusic(HttpServletRequest request) {
        if (randomMusic==null) {
            File [] mp3s = new File(rootPath,".music").listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".mp3");
                }
            });
            if (mp3s==null || mp3s.length==0) {
                randomMusic = new String[]{};
                return null;
            } else {
                randomMusic=new String[mp3s.length];
                for (int i=0;i<mp3s.length;i++) {
                    randomMusic[i]="/.music/"+mp3s[i].getName();
                }
            }
        }
        SessionOptions options = SessionOptions.getSessionOptions(request);
        int randomIndex = options.getMusicIndex();
        if (randomIndex>=randomMusic.length) randomIndex=0;
        if (randomIndex<randomMusic.length) {
            options.setMusicIndex(randomIndex+1);
            return randomMusic[randomIndex];
        }  else return null;
    }


    @RequestMapping(value = "/")
    public String getFolder(Model model, HttpServletRequest request, String path, String newStep) {
        if (!isReady) {
            model.addAttribute("message","Not ready!!!");
            return "message";
        }
        if (newStep!=null && !newStep.isEmpty()) {
            EditorController editorController = SpringContextHolder.getBean(EditorController.class);
            editorController.createHtmlFile(rootPath,path,newStep,archiveInfo);
        }
        commonAttribute(model,request);
        Map<String, Object> res = getPathAttributes(path, false,
                SessionOptions.getSessionOptions(request).isFavoriteFilter());
        model.addAllAttributes(res);
        if (path!=null && !path.isEmpty() &&
                 (new File(rootPath+File.separator+path+File.separator+".need-scan").exists() ||
                    (res.get("htmls")==null && !archiveInfo.getInfos().stream().anyMatch(p->
                        p.getSubFolder().equals(path) ||
                        p.getSubFolder().startsWith(path+File.separator)                )
                    )
                 )
           ) model.addAttribute("needScan",true);
        setBackgroundMusic(request,model,path);
        return "index";
    }

    @RequestMapping(value = "play")
    public String playFolder(Model model, HttpServletRequest request, String path, Integer index) {
        if (!isReady) {
            model.addAttribute("message","Not ready!!!");
            return "message";
        }
        commonAttribute(model,request);
        model.addAttribute("notLoadImage",true);  // 不加载图像
        model.addAttribute("loopPlay",true);
        model.addAttribute("startFrom",index);
        SessionOptions options = SessionOptions.getSessionOptions(request);
        model.addAllAttributes(getPathLoopImages(path, options.isFavoriteFilter()));
        setBackgroundMusic(request,model,path);
        return "index";
    }

    @RequestMapping(value = "search")
    public String doSearch(Model model, HttpServletRequest request, HttpServletResponse response, String text) {
        if (!isReady) {
            model.addAttribute("message","Not ready!!!");
            return "message";
        }
        commonAttribute(model,request);
        if (text == null || text.trim().isEmpty()) return getFolder(model, request, "", null);
        text = text.trim().toLowerCase();
        List<String> dirs = new ArrayList<>();
        List<PhotoInfo> htmls = new ArrayList<>();
        List<PhotoInfo> videos = new ArrayList<>();
        List<PhotoInfo> audios = new ArrayList<>();
        List<PhotoInfo> photos = new ArrayList<>();
        SessionOptions options = SessionOptions.getSessionOptions(request);
        for (PhotoInfo p : archiveInfo.getInfos()) {
            if (p.getSubFolder().toLowerCase().contains(text) && !p.getSubFolder().toLowerCase().endsWith(".web") && !dirs.contains(p.getSubFolder()))
                dirs.add(p.getSubFolder());
            if (photoInfoContains(p,text)) {
                String mime = p.getMimeType();
                if (mime==null) continue;
                if (mime.contains("html")) htmls.add(p);
                else if (mime.contains("audio")) audios.add(p);
                else if (mime.contains("video")) videos.add(p);
                else if (mime.contains("image") && (!options.isFavoriteFilter() || (p.getRating()!=null && p.getRating()==5))) photos.add(p);
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
        setBackgroundMusic(request, model,null);
        return "index";
    }


    @ResponseBody
    @RequestMapping(value = "remove")
    public String remove(String path) {
        if (!isReady) return "message";
        if (Modification.removeAction(path,archiveInfo)) {
            Modification.save(new Modification(Modification.Remove,path,null),rootPath);
            return "ok";
        }
        else return "error";
    }
    @ResponseBody
    @RequestMapping(value = "orientation")
    public String orientation(String path, Integer [] orientations, Integer rating) {
        if (!isReady) {
            return "error";
        }
        if (path==null) return "error";
        PhotoInfo pi = archiveInfo.find(new File(rootPath + File.separator + path));
        if (pi==null) return "error";
        if (pi.modifyOrientation(rootPath, rating, orientations)) {
            Map<String,Object> map = new HashMap<>();
            if (rating!=null) map.put(Key.getName(Key.RATING),rating);
            if (orientations!=null) map.put(Key.getName(Key.ORIENTATION),
                    Orientation.name(pi.getOrientation()==null? Orientation.NONE.getValue() : pi.getOrientation()));
            Modification.save(new Modification(Modification.Exif,path,map),rootPath);
            afterChanged();
            return "ok,"+
                    (pi.getOrientation()==null?"":pi.getOrientation())+
                    ","+(pi.getRating()==null?"0":pi.getRating()) +
                    ","+pi.getLastModified();
        }
        else return "fail";
    }

    void syncFromModification() {
        List<Modification> list = Modification.read(rootPath);
        if (list!=null) {
            System.out.println("同步修改...");
            Modification.execute(list,archiveInfo);
            Modification.resetSyncAction(rootPath);
            System.out.println("同步修改完成.");
            afterChanged();
        }
    }

    @ResponseBody
    @RequestMapping(value = "scan")
    public String scan(String path) {
        if (path==null || path.isEmpty()) return "error";
        new Thread() {
            @Override
            public void run() {
                if (Modification.scanAction(path,archiveInfo))
                        Modification.save(new Modification(Modification.Scan,path,null),rootPath);
            }
        }.start();
        return "ok";
    }

    @ResponseBody
    @RequestMapping(value = "range")
    public String range(String path, String value, String type, String start,String end, Boolean includeSubFolder) {
        final String subPath=ArchiveUtils.formatterSubFolder(path);
        if (type==null) return "error";
        Optional<Key> k = Key.findKeyWithName(type);
        if (k.isPresent()) {
            final Key key = k.get();
            new Thread() {
                @Override
                public void run() {
                    Map<String, Object> map = new HashMap<String, Object>() {{
                            put(Key.getName(key), value);
                            put(Modification.start_photo,start);
                            put(Modification.end_photo,end);
                            put(Modification.include_sub_folder, includeSubFolder!=null && includeSubFolder);
                    }};
                    Modification.execute(new ArrayList<Modification>(){{
                        add(new Modification(Modification.Exif,subPath,map));
                    }},archiveInfo);
                    afterChanged();
                    Modification.save(new Modification(Modification.Exif, subPath, map), rootPath);
                }
            }.start();
            return "ok";
        }
        return "error";
    }
    @ResponseBody
    @RequestMapping(value = "stdout")
    public String stdout(HttpServletRequest request) {
        File file = new File(STDOUT);
        if (request.getQueryString()!=null && request.getQueryString().toLowerCase().contains("truncate")) {
            FileUtil.writeToFile(file,"");
            return "ok";
        } else {
            String s = FileUtil.getFromFile(file);
            if (s!=null) return s.replace("\n","<br>").replaceAll("\\u001B[^m]*m","");
            else return "Not message in stdout.";
        }
    }

    @RequestMapping(value = "shutdown")
    public String shutdown(HttpServletRequest request, Model model, Integer delay) {
        if (SessionOptions.getSessionOptions(request).isUnlocked()) {
            model.addAttribute("message","将在 "+ (delay==null?10:delay) +"s 后立即关闭服务器");
            CommandRunner.shutdown(delay==null?10:delay);
        }
        else {
            model.addAttribute("message","不允许远程关闭服务器");
        }
        return "message";
    }
    @Override
    public String getErrorPath() {
        return "/error";
    }

    @ExceptionHandler(Exception.class)
    @RequestMapping("error")
    public String handleError(HttpServletRequest request, Model model, Exception e){
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String msg = "error";
        if (statusCode!=null) {
            if (statusCode == 401) {
                msg = "401 Error";
            } else if (statusCode == 404) {
                msg = "页面不存在";
            } else if (statusCode == 403) {
                msg = "403 Error";
            } else {
                if (e != null) {
                    msg = Util.printStackTrace(e);
                }
                else msg = "内部异常";
            }
        } else if (e!=null) {
            msg=Util.printStackTrace(e);
        }
        model.addAttribute("message",msg);
        return "message";
    }


    private boolean photoInfoContains(PhotoInfo p,String text) {
        String s = p.getFileName();
        if (s!=null && s.toLowerCase().contains(text)) return true;
        s = ArchiveUtils.join(" ", p.getCountry(),p.getProvince(),p.getCity(),p.getLocation(),p.getSubjectCode(),p.getHeadline(),p.getSubTitle(),
                DateUtil.date2String(p.getShootTime(),null),p.getModel(),p.getLens());
        if (s!=null && s.toLowerCase().contains(text)) return true;
        return false;
    }
    List<PhotoInfo> mimeListInPath(String mime, String folder, boolean favoriteFilter) {
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


    private void commonAttribute(Model model, HttpServletRequest request) {
        initStatics(model);
        SessionOptions options = SessionOptions.getSessionOptions(request);
        model.addAttribute("sessionOptions",options);
        if (noVideoThumb) model.addAttribute("noVideoThumb",true);
        List<Map<String,Object>> editableExifTag = new ArrayList<>();
        for (Key k : ArchiveUtils.MODIFIABLE_KEYS) {
            Map<String,Object> map = new HashMap<>();
            map.put("name",Key.getName(k));
            map.put("note",Key.getNotes(k));
            editableExifTag.add(map);
        }
        model.addAttribute("editableExifTag",editableExifTag);
    }

    public Map<String,Object> getPathAttributes(String path, boolean just4ResourceList, boolean favoriteFilter) {
        Map<String,Object> model = new HashMap<>();
        path=ArchiveUtils.formatterSubFolder(path);
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
                List<PhotoInfo> htmls = mimeListInPath("html", path, favoriteFilter);
                if (htmls != null && htmls.size() > 0) model.put("htmls", htmls);
            }
            // video文件
            List<PhotoInfo> videos = mimeListInPath("video",path, favoriteFilter);
            if (videos!=null && videos.size()>0)  model.put("videos",videos);

            // audio文件
            List<PhotoInfo> audios = mimeListInPath("audio",path, favoriteFilter);
            if (audios!=null && audios.size()>0)  model.put("audios",audios);

            //Photo Info
            List<PhotoInfo> photos = mimeListInPath("image",path, favoriteFilter);
            if (photos!=null && photos.size()>0) {
                model.put("photos",photos);
            }
        }
        return model;
    }

    private boolean loopFilter(PhotoInfo p, boolean favoriteFilter) {
        return (!favoriteFilter || (p.getRating()!=null && p.getRating()==5)) &&
                p.getMimeType()!=null && p.getMimeType().contains("image/") &&
                !p.getSubFolder().endsWith(".web") && p.getSubFolder().indexOf(".web"+File.separator)<0;
    }
    private Map<String,Object> getPathLoopImages(final String path, boolean favoriteFilter) {
        Map<String,Object> model = new HashMap<>();
        model.put("separator",File.separator);
        List<PhotoInfo> photos;
        if (path!=null && !path.isEmpty()) {
            model.put("pathNames",path.split("\\\\|/"));
            photos = archiveInfo.getInfos().stream().filter(p ->
                    loopFilter(p,favoriteFilter) && p.getSubFolder().indexOf(path) == 0
            ).collect(Collectors.toList());
        } else photos = archiveInfo.getInfos().stream().filter(p -> loopFilter(p,favoriteFilter)
        ).collect(Collectors.toList());
        // photos.sort((a,b)->a.compareTo(b));
        if (photos!=null && photos.size()>0) model.put("photos",photos);
        return model;
    }
    private void setBackgroundMusic(HttpServletRequest request, Model model,String path) {
        String bkm = (path==null || path.isEmpty() ? "" : "/"+path)+ "/.music.mp3";
        if (new File(rootPath,bkm).exists()) model.addAttribute("backgroundMusic",bkm.replaceAll("\\\\","/"));
        else {
            String url = switchMusic(request);
            if (url!=null) {
                model.addAttribute("backgroundMusic",url);
            }
        }
    }

    void afterChanged() {
        new Thread() {
            @Override
            public void run() {
                archiveInfo.sortInfos();
                archiveInfo.saveInfos();
            }
        }.start();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ArchiveUtils.setOutput(this.getClass(),STDOUT);
        String ffmpeg = env.getProperty("photo.ffmpeg");
        if (ffmpeg!=null) ArchiveUtils.FFMPEG = ffmpeg;
        String exiftool = env.getProperty("photo.exiftool");
        if (exiftool!=null) ExifTool.EXIFTOOL = exiftool;

        new ArchiveInfo();

        unlockPassword = env.getProperty("photo.password");
        if (unlockPassword ==null) unlockPassword = "19960802";
        noVideoThumb = Util.boolValue(env.getProperty("photo.no-video-thumb"));

        String vca = env.getProperty("photo.video-capture-at");
        if (vca!=null) ArchiveUtils.VIDEO_CAPTURE_AT = vca;

        rootPath = env.getProperty("photo.root-path");
        if (rootPath==null) rootPath = SpringContextUtil.getProjectHomeDirection();


        new Thread() {
            @Override
            public void run() {

                archiveInfo = new ArchiveInfo(rootPath);
                rootPath = archiveInfo.getPath();  // 标准化
                System.out.println("归档主目录为 : "+rootPath);

                isReady = true;

                syncFromModification();
                BaiduGeo.seekAddressInfo(archiveInfo);
                archiveInfo.createThumbFiles();
                System.out.println("Photo viewer started.");
            }
        }.start();
    }
}