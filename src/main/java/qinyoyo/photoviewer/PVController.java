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
import qinyoyo.photoinfo.archive.*;
import qinyoyo.utils.*;
import qinyoyo.photoinfo.ArchiveUtils;
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
    public String login(Model model,HttpServletRequest request, String password, Boolean debug, Boolean htmlEditable ) {
        if (password!=null && password.equals(unlockPassword)) {
            SessionOptions options = SessionOptions.getSessionOptions(request);
            options.setUnlocked(true);
            if (debug!=null) options.setDebug(debug);
            if (htmlEditable!=null) options.setHtmlEditable(htmlEditable);
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
    @RequestMapping(value = "regTest")
    String regTest() {
        return "reg_test";
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
        setBackgroundMusic(request,model,path);
        return "index";
    }

    String pointType() {
        String mapType = env.getProperty("photo.map");
        if (mapType==null || mapType.equals("bmap")) return "bd09";
        else return "gcj02";
    }

    @RequestMapping(value = "/step")
    public String stepInMap(Model model, HttpServletRequest request, String path) {
        if (!isReady) {
            model.addAttribute("message","Not ready!!!");
            return "message";
        }
        SessionOptions sessionOptions = SessionOptions.getSessionOptions(request);
        commonAttribute(model,request);
        Map<String, Object> res = getFolderPathAttributes(path, false);
        model.addAllAttributes(res);
        final String standardFubFolder = ArchiveUtils.formatterSubFolder(path, archiveInfo.getPath());
        List<PhotoInfo> photos = archiveInfo.getInfos().stream().filter(p ->
                (standardFubFolder.equals(p.getSubFolder()) || ((standardFubFolder.isEmpty() || p.getSubFolder().startsWith(standardFubFolder+File.separator))))
                && p.getMimeType()!=null && p.getMimeType().contains("image")
                && (!sessionOptions.isFavoriteFilter() || (p.getRating()!=null && p.getRating()==5))
                && p.getLongitude()!=null && p.getLatitude()!=null
        ).collect(Collectors.toList());

        if (photos!=null && photos.size()>0) model.addAttribute("photos",photos);
        model.addAttribute("CLIENT_POINT_TYPE", pointType());
        return "step";
    }

    @RequestMapping(value = "/exif")
    public String exifEdit(Model model, HttpServletRequest request, String path, Boolean recursion) {
        if (!isReady) {
            model.addAttribute("message","Not ready!!!");
            return "message";
        }
        SessionOptions options = SessionOptions.getSessionOptions(request);
        if (!options.isUnlocked()) {
            model.addAttribute("message","请先解锁!!!");
            return "message";
        }
        String subFolder=ArchiveUtils.formatterSubFolder(path,archiveInfo.getPath());
        commonAttribute(model,request);
        Map<String, Object> res = getFolderPathAttributes(path, false);
        model.addAllAttributes(res);
        final boolean incSub = (recursion!=null && recursion ? true : false);
        model.addAttribute("recursion",incSub);
        List<PhotoInfo> photos = archiveInfo.subFolderInfos(subFolder,incSub).stream().filter(p ->
               p.getMimeType()!=null && p.getMimeType().contains("image")
            ).collect(Collectors.toList());
        if (photos!=null && photos.size()>0) model.addAttribute("photos",photos);
        model.addAttribute("countries", TimeZoneTable.countryTimeZones);
        String mapType = env.getProperty("photo.map");
        model.addAttribute("CLIENT_POINT_TYPE", pointType());
        return "exif";
    }
    @ResponseBody
    @RequestMapping(value = "position")
    public String scan(double lng,double lat, boolean towgs84, HttpServletRequest request) {
        PositionUtil.LatLng p1 = towgs84 ? PositionUtil.bd09ToWgs84(lat,lng) : PositionUtil.wgs84ToBd09(lat,lng);
        return String.format("%.7f,%.7f",p1.longitude,p1.latitude);
    }
    @ResponseBody
    @RequestMapping(value = "/moveFile")
    public String moveFiles(HttpServletRequest request, HttpServletResponse response, String path, String subFolder,String fileName) {
        SessionOptions options = SessionOptions.getSessionOptions(request);
        if (!options.isUnlocked()) {
            return "请先解锁!!!";
        }
        if (Util.isEmpty(subFolder) || Util.isEmpty(fileName)) return "没有指定文件";
        String [] subFolders = subFolder.split("\\|",-1),
                files = fileName.split("\\|",-1);
        List<PhotoInfo> photoList = new ArrayList<>();
        for (int i=0;i< files.length;i++) {
            PhotoInfo p = archiveInfo.find(subFolders[i],files[i]);
            if (p!=null) photoList.add(p);
        }
        if (!photoList.isEmpty() && archiveInfo.moveFile(photoList,path)) return "ok";
        else return "error";
    }
    @ResponseBody
    @RequestMapping(value = "/exifSave")
    public String exifSave(PhotoInfo p1, String type, String selectedTags, HttpServletRequest request, HttpServletResponse response, String path, Integer [] orientations) {
        SessionOptions options = SessionOptions.getSessionOptions(request);
        if (!options.isUnlocked()) {
            return "请先解锁!!!";
        }
        if (p1!=null && !Util.isEmpty(selectedTags)) {
            if (p1.getLatitude()!=null && p1.getLongitude()!=null &&
                    (PositionUtil.BD09.equals(type) || PositionUtil.GCJ02.equals(type)) ) {
                PositionUtil.LatLng latLng = PositionUtil.BD09.equals(type) ?
                        PositionUtil.bd09ToWgs84(p1.getLatitude(), p1.getLongitude()) :
                        PositionUtil.gcj02ToWgs84(p1.getLatitude(), p1.getLongitude());
                if (latLng!=null) {
                    p1.setLatitude(latLng.latitude);
                    p1.setLongitude(latLng.longitude);
                } else {
                    p1.setLatitude(null);
                    p1.setLongitude(null);
                }
            }
            String [] subFolders = p1.getSubFolder().split("\\|",-1),
                    files = p1.getFileName().split("\\|",-1);
            String [] tags = selectedTags.split(",");
            List<Key> selectedKey = new ArrayList<>();
            for (String tag : tags) {
                if (tag.equals("orientation")) {
                    if (orientations!=null && orientations.length>0 && files.length==1) {
                        Integer orientation0 = p1.getOrientation();
                        Integer orientation1 = Orientation.byWithOriginal(orientation0,orientations);
                        if (!Orientation.equals(orientation0,orientation1)) {
                            p1.setOrientation(orientation1);
                            selectedKey.add(PhotoInfo.FIELD_TAG.get(tag));
                        }
                    }
                }
                else if (PhotoInfo.FIELD_TAG.containsKey(tag)) {
                    selectedKey.add(PhotoInfo.FIELD_TAG.get(tag));
                }
            }
            if (selectedKey.isEmpty()) return "没有需要修改的标签";
            if (selectedKey.contains(Key.COUNTRY_CODE)) {
                String code = TimeZoneTable.standCountryName(p1.getCountry(),true);
                if (!Util.isEmpty(code)) p1.setCountryCode(code);
            }
            if (selectedKey.contains(Key.STATE)) p1.setProvince(BaiduGeo.truncLocationName(p1.getProvince(),Key.STATE,p1.getCountry()));
            if (selectedKey.contains(Key.CITY)) p1.setCity(BaiduGeo.truncLocationName(p1.getCity(),Key.CITY,p1.getCountry(),p1.getProvince()));
            if (selectedKey.contains(Key.LOCATION)) p1.setLocation(BaiduGeo.truncLocationName(p1.getLocation(),Key.LOCATION,
                    p1.getCountry(),p1.getProvince(),p1.getCity()));
            if (selectedKey.contains(Key.SUBJECT_CODE)) p1.setSubjectCode(BaiduGeo.truncLocationName(p1.getSubjectCode(),Key.SUBJECT_CODE,
                    p1.getCountry(),p1.getProvince(),p1.getCity()));

            List<PhotoInfo> photoList = new ArrayList<>();
            for (int i=0;i< files.length;i++) {
                PhotoInfo p = archiveInfo.find(subFolders[i],files[i]);
                if (p!=null) photoList.add(p);
            }
            List<Modification> modifications=new ArrayList<>();
            if (!selectedKey.isEmpty()) {
                for (int i=0;i<files.length;i++) {
                    int index = i;
                    Optional<PhotoInfo> op0 = photoList.stream().filter(p ->
                            p.getSubFolder().equals(subFolders[index]) && p.getFileName().equals(files[index]))
                            .findFirst();
                    if (op0.isPresent()) {
                        PhotoInfo p0 = op0.get();
                        List<Key> keys = ArchiveUtils.differentOf(p0, p1,selectedKey);
                        if (keys != null && !keys.isEmpty()) {
                            Map<Key, Object> params = Modification.exifMap(p1, keys);
                            if (i>0 && params.containsKey(Key.DATETIMEORIGINAL)) {
                                Object dt = params.get(Key.DATETIMEORIGINAL);
                                if (dt!=null && dt instanceof Date) {  // 避免时间相同
                                    params.put(Key.DATETIMEORIGINAL,new Date(((Date)dt).getTime() + i*1000));
                                }
                            }
                            String fullPath = subFolders[i].isEmpty()?files[i] : (subFolders[i]+File.separator+files[i]);
                            modifications.add(Modification.exifModified(fullPath, params));
                        }
                    }
                }
            }
            if (modifications.isEmpty()) return "没有需要修改的对象";
            if (files.length==1) {
                int count =  Modification.setExifTags(modifications,archiveInfo,true);
                if (count>0) {
                   afterChanged();
                   return "ok";
                } else return "不需要修改";
            } else  {
                new Thread() {
                    @Override
                    public void run() {
                        int count = Modification.setExifTags(modifications,archiveInfo,true);
                        if (count>0) {
                            archiveInfo.sortInfos();
                            archiveInfo.saveInfos();
                        }
                    }
                }.start();
                return "ok,提交后台处理，稍后需要刷新";
            }
        }
        return "没有需要修改的对象";
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
    public String remove(String path,HttpServletRequest request) {
        if (!isReady) return "message";
        SessionOptions options = SessionOptions.getSessionOptions(request);
        if (!options.isUnlocked()) {
            return "请先解锁!!!";
        }
        if (Modification.removeAction(path,archiveInfo,true)) {
            return "ok";
        }
        else return "error";
    }
    @ResponseBody
    @RequestMapping(value = "orientation")
    public String orientation(String path, Integer [] orientations, Integer rating, HttpServletRequest request) {
        if (!isReady) {
            return "error";
        }
        SessionOptions options = SessionOptions.getSessionOptions(request);
        if (!options.isUnlocked()) {
            return "请先解锁!!!";
        }
        if (path==null) return "error";
        PhotoInfo pi = archiveInfo.find(new File(rootPath + File.separator + path));
        if (pi==null) return "error";
        if (pi.modifyOrientation(rootPath, rating, orientations)) {
            Map<Key,Object> map = new HashMap<>();
            if (rating!=null) map.put(Key.RATING,rating);
            if (orientations!=null) map.put(Key.ORIENTATION,pi.getOrientation());
            Modification.save(Modification.exifModified(path,map),rootPath);
            afterChanged();
            return "ok,"+
                    (pi.getOrientation()==null?"":pi.getOrientation())+
                    ","+(pi.getRating()==null?"0":pi.getRating()) +
                    ","+pi.getLastModified();
        }
        else return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "scan")
    public String scan(String path, HttpServletRequest request) {
        SessionOptions options = SessionOptions.getSessionOptions(request);
        if (!options.isUnlocked()) {
            return "请先解锁!!!";
        }
        if (path==null || path.isEmpty()) return "error";
        new Thread() {
            @Override
            public void run() {
                Modification.scanAction(path,archiveInfo,true);
            }
        }.start();
        return "ok";
    }

    @ResponseBody
    @RequestMapping(value = "stdout")
    public String stdout(HttpServletRequest request) {
        File file = new File(STDOUT);
        if (request.getQueryString()!=null && request.getQueryString().toLowerCase().contains("truncate")) {
            FileUtil.writeToGbkFile(file,"");
            return "ok";
        } else {
            String s = FileUtil.getFromGbkFile(file);
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
                    msg = Util.getStackTrace(e);
                }
                else msg = "内部异常";
            }
        } else if (e!=null) {
            msg=Util.getStackTrace(e);
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
    List<PhotoInfo> mimeListInPath(String mime, String folder, boolean favoriteFilter, boolean incSubFolder) {
        if (!isReady) return null;
        final String standardFubFolder = ArchiveUtils.formatterSubFolder(folder, archiveInfo.getPath());
        List<PhotoInfo> list = archiveInfo.getInfos().stream().filter(p->
                                (standardFubFolder.equals(p.getSubFolder()) || (incSubFolder && (standardFubFolder.isEmpty() || p.getSubFolder().startsWith(standardFubFolder+File.separator))))
                                && p.getMimeType()!=null && p.getMimeType().contains(mime)
                                && (!favoriteFilter || !mime.equals("image") || (p.getRating()!=null && p.getRating()==5))
                            ).collect(Collectors.toList());
        if (mime.equals("html")) {  // 游记文件，特殊处理
            List<PhotoInfo> list1 = archiveInfo.getInfos().stream()
                    .filter(p->p.getFileName().equals("index.html") && p.getSubFolder().endsWith(".web") &&
                            p.getMimeType()!=null && p.getMimeType().contains(mime) &&
                            folder.equals(p.getSubFolder().lastIndexOf(File.separator) >=0 ? p.getSubFolder().substring(0,p.getSubFolder().lastIndexOf(File.separator)) : "")
                    ).collect(Collectors.toList());
            List<PhotoInfo> list2 = ArchiveUtils.stepUnderFolder(rootPath,folder,false);
            if (list1!=null && list1.size()>0) {
                if (list == null) list = new ArrayList<>();
                list.addAll(list1);
                if (list2 != null && list2.size() > 0) {
                    for (Iterator iterator = list2.iterator(); iterator.hasNext(); ) {
                        PhotoInfo p2 = (PhotoInfo) iterator.next();
                        for (PhotoInfo p1 : list1) {
                            if (p1.getSubFolder().equals(p2.getSubFolder()) && p1.getFileName().equals(p2.getFileName())) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                }
            }
            if (list2!=null && list2.size()>0){
                if (list==null) list=new ArrayList<>();
                list.addAll(list2);
                archiveInfo.getInfos().addAll(list2);
                afterChanged();
            }
            return list;
        } else return list;
    }


    public static void initStatics(final Object model) {
        BeansWrapper wrapper = new BeansWrapper(new Version(2, 3, 27));
        TemplateModel statics = wrapper.getStaticModels();
        if (model instanceof Model) ((Model)model).addAttribute("statics", statics);
        else if (model instanceof Map) ((Map)model).put("statics", statics);
    }


    public void commonAttribute(Model model, HttpServletRequest request) {
        initStatics(model);
        SessionOptions options = SessionOptions.getSessionOptions(request);
        model.addAttribute("sessionOptions",options);
        if (noVideoThumb) model.addAttribute("noVideoThumb",true);
    }

    public Map<String,Object> getFolderPathAttributes(String path, boolean just4ResourceList) {
        Map<String,Object> model = new HashMap<>();
        path=ArchiveUtils.formatterSubFolder(path,archiveInfo.getPath());
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
                    if (pathname.getName().startsWith(".") || (!just4ResourceList && pathname.getName().endsWith(".web")))
                        return false;
                    return pathname.isDirectory();
                }
            });
            List<Map<String, Object>> subDirectories = new ArrayList<>();
            if (!path.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", "..");
                try {
                    String pp = dir.getParentFile().getCanonicalPath();
                    if (pp.length() <= rootPath.length()) map.put("path", "");
                    else map.put("path", pp.substring(rootPath.length() + 1));
                    subDirectories.add(map);
                } catch (Exception e) {
                    Util.printStackTrace(e);
                }
            }
            if (subDirs != null && subDirs.length > 0) {
                for (File f : subDirs) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", f.getName());
                    try {
                        map.put("path", f.getCanonicalPath().substring(rootPath.length() + 1));
                        subDirectories.add(map);
                    } catch (Exception e) {
                        Util.printStackTrace(e);
                    }
                }
            }
            if (subDirectories.size() > 0) {
                subDirectories.sort((a, b) -> a.get("name").toString().compareTo(b.get("name").toString()));
                model.put("subDirectories", subDirectories);
            }
        }
        return model;
    }
    public Map<String,Object> getPathAttributes(String path, boolean just4ResourceList, boolean favoriteFilter) {
        Map<String,Object> model = getFolderPathAttributes(path, just4ResourceList);
        path=ArchiveUtils.formatterSubFolder(path,archiveInfo.getPath());
        File dir = new File(rootPath+File.separator+path);
        if (dir.exists() && dir.isDirectory()) {
            if (!just4ResourceList) {
                // html文件
                List<PhotoInfo> htmls = mimeListInPath("html", path, favoriteFilter, false);
                if (htmls != null && htmls.size() > 0) model.put("htmls", htmls);
            }
            // video文件
            List<PhotoInfo> videos = mimeListInPath("video",path, favoriteFilter,false);
            if (videos!=null && videos.size()>0)  model.put("videos",videos);

            // audio文件
            List<PhotoInfo> audios = mimeListInPath("audio",path, favoriteFilter,false);
            if (audios!=null && audios.size()>0)  model.put("audios",audios);

            //Photo Info
            List<PhotoInfo> photos = mimeListInPath("image",path, favoriteFilter,false);
            if (photos!=null && photos.size()>0) {
                model.put("photos",photos);
            }
        }
        return model;
    }

    public Map<String,Object> getPathAttributesByDate(String date, boolean favoriteFilter) {
        Map<String,Object> model = new HashMap<>();
        model.put("resourceByDate", date);
        // video文件
        List<PhotoInfo> videos = archiveInfo.getInfos().stream().filter(p->
                    p.getMimeType()!=null && p.getMimeType().contains("video") &&
                    (p.getCreateTime()!=null && DateUtil.date2String(p.getCreateTime()).startsWith(date))
                ).collect(Collectors.toList());
        if (videos!=null && videos.size()>0)  model.put("videos",videos);

        // audio文件
        List<PhotoInfo> audios = archiveInfo.getInfos().stream().filter(p->
                    p.getMimeType()!=null && p.getMimeType().contains("audio") &&
                            (p.getCreateTime()!=null && DateUtil.date2String(p.getCreateTime()).startsWith(date))
            ).collect(Collectors.toList());
        if (audios!=null && audios.size()>0)  model.put("audios",audios);

        //Photo Info

        List<PhotoInfo> photos = archiveInfo.getInfos().stream().filter(p->
                p.getMimeType()!=null && p.getMimeType().contains("image") &&
                        (!favoriteFilter || (p.getRating()!=null && p.getRating()==5)) &&
                        (p.getShootTime()!=null && DateUtil.date2String(p.getShootTime()).startsWith(date))
        ).collect(Collectors.toList());
        if (photos!=null && photos.size()>0) {
            model.put("photos",photos);
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
        if (ffmpeg!=null) ExifTool.FFMPEG = ffmpeg;
        String exiftool = env.getProperty("photo.exiftool");
        if (exiftool!=null) ExifTool.EXIFTOOL = exiftool;

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
                archiveInfo.removeNotExistInfo();
                FileUtil.removeEmptyFolder(new File(rootPath));
                System.out.println("归档主目录为 : "+rootPath);
                isReady = true;
                archiveInfo.createThumbFiles();
                System.out.println("Photo viewer started.");
            }
        }.start();
    }
}
