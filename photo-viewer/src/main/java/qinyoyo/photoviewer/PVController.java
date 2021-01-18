package qinyoyo.photoviewer;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import qinyoyo.utils.DateUtil;
import qinyoyo.utils.SpringContextUtil;
import qinyoyo.utils.Util;
import tang.qinyoyo.ArchiveUtils;
import tang.qinyoyo.archive.ArchiveInfo;
import tang.qinyoyo.archive.FolderInfo;
import tang.qinyoyo.archive.PhotoInfo;
import tang.qinyoyo.exiftool.CommandRunner;
import tang.qinyoyo.exiftool.ExifTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
    private static final Configuration CONFIGURATION = new Configuration(Configuration.VERSION_2_3_22);
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

    private boolean isMobile(HttpServletRequest request) {
        String userAgent = request.getHeader("USER-AGENT");
        return userAgent.contains("Mobile") || userAgent.contains("Phone");
    }
    private void commonAttribute(Model model, HttpServletRequest request) {
        String params = request.getQueryString();
        String userAgent = request.getHeader("USER-AGENT");
        if (isMobile(request)) {
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

    private Map<String,Object> getPathAttributes(String path, boolean just4ResourceList) {
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

    @RequestMapping(value = "thumbnail")
    public String getThumbnail(HttpServletRequest request, HttpServletResponse response, String path) {
        return null;
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
    @RequestMapping(value = "/")
    public String getFolder(Model model, HttpServletRequest request, HttpServletResponse response, String path, String newStep) {
        if (!isReady) {
            model.addAttribute("message","Not ready!!!");
            return "error";
        }
        if (newStep!=null && !newStep.isEmpty()) {
            try {
                File dir = new File(new File(rootPath, path), newStep+".web");
                if (dir.mkdirs()) {
                    Map<String,Object> attr = new HashMap<String,Object>(){{
                        put("title",newStep);
                    }};
                    freeMarkerWriter("newStep.ftl",dir.getAbsolutePath()+File.separator+"index.html",attr);
                    archiveInfo.addFile(new File(dir,"index.html"));
                }
            } catch (Exception e) {}
        }
        commonAttribute(model,request);
        model.addAllAttributes(getPathAttributes(path,false));
        setBackgroundMusic(model,path);
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
                DateUtil.date2String(p.getShootTime(),null),p.getModel(),p.getLens());
        if (s!=null && s.toLowerCase().contains(text)) return true;
        return false;
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

    @RequestMapping(value = "same")
    public String sameView(Model model) {
        if (!isReady) {
            model.addAttribute("message","Not ready!!!");
            return "error";
        }
        model.addAttribute("separator", File.separator);
        List<Map<String,Object>> list = new ArrayList<>();
        BufferedReader ins=null;
        StringBuilder sb=new StringBuilder();
        try {
            File logFile = new File(rootPath, ".same_photo.log");
            ins = new BufferedReader(new InputStreamReader(new FileInputStream(logFile),"GBK"));
            String line = null;
            while ((line=ins.readLine())!=null) {
                String [] ll= line.split(" <-> ");
                if (ll.length==2) {
                    String f1=ll[0].trim(), f2 = ll[1].trim();
                    File file1=new File(f1);
                    File file2=new File(f2);
                    if (!f1.isEmpty() && !f2.isEmpty() && f1.startsWith(rootPath) && f2.startsWith(rootPath)) {
                        if (file1.exists() && file2.exists()) {
                            PhotoInfo p1=archiveInfo.find(file1), p2=archiveInfo.find(file2);
                            sb.append(line).append("\r\n");
                            list.add(new HashMap<String, Object>() {{
                                put("same1", f1.substring(rootPath.length() + 1));
                                put("title1", (p1==null?new PhotoInfo(rootPath,file1):p1).toString());
                                put("same2", f2.substring(rootPath.length() + 1));
                                put("title2", (p2==null?new PhotoInfo(rootPath,file2):p2).toString());
                            }});
                        }
                    }
                }
            }
            ins.close();
            if (sb.length()==0) logFile.delete();
            else ArchiveUtils.writeToFile(logFile,sb.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        model.addAttribute("sames",list);;
        return "same_view";
    }
    private boolean deleteFile(File file) {
        if (file.exists()) {
            try {
                String fp = file.getCanonicalPath();
                if (fp.indexOf(rootPath) == 0) {
                    return new PhotoInfo(rootPath, file).delete(rootPath);
                } else return file.delete();
            } catch (Exception e) {}
        }
        return false;
    }
    boolean autoDeleteSame(File file1, File file2) {
        try {
            if (file1.exists() && file2.exists()) {
                String f1 = file1.getCanonicalPath(), f2 = file2.getCanonicalPath();
                String s_d = File.separator + ".delete" + File.separator;
                String s_c = File.separator + FolderInfo.DEFPATH + File.separator;
                if (f1.contains(s_d)) return deleteFile(file1);
                else if (f2.contains(s_d)) return deleteFile(file2);
                else if (f1.contains(s_c)) return deleteFile(file1);
                else if (f2.contains(s_c)) return deleteFile(file2);
                else if (file1.length() <= file2.length()) return deleteFile(file1);
                else return deleteFile(file2);
            }
        }catch (Exception e) {}
        return false;
    }

    void afterChanged() {
        new Thread() {
            @Override
            public void run() {
                archiveInfo.saveInfos();
            }
        }.start();
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
    @RequestMapping(value = "delete-file")
    public String deleteFile(HttpServletRequest request, HttpServletResponse response, String path) {
        if (!isReady) {
            return "error";
        }
        if (path==null) return null;
        if (new PhotoInfo(rootPath,new File(rootPath + File.separator + path)).delete(rootPath)) {
            return "ok";
        }
        return "error";
    }

    @ResponseBody
    @RequestMapping(value = "save-file")
    public String saveFile(HttpServletRequest request, HttpServletResponse response, String path) {
        if (!isReady) {
            return "error";
        }
        if (path==null) return null;
        String [] ll= path.split(" <-> ");
        if (ll.length==2) {
            BufferedReader ins=null;
            StringBuilder sb=new StringBuilder();
            for (String fn : ll) {
                if (fn.startsWith(".delete"+File.separator)) {
                    try {
                        Files.move(new File(rootPath, fn).toPath(),new File(rootPath, fn.substring(8)).toPath(), StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                    }
                }
            }
            try {
                File logFile = new File(rootPath, ArchiveInfo.same_photo_log);
                ins = new BufferedReader(new InputStreamReader(new FileInputStream(logFile),"GBK"));
                String line = null;
                boolean found= false;
                while ((line=ins.readLine())!=null) {
                    if (found) sb.append(line).append("\r\n");
                    else if (line.contains(ll[0]) && line.contains(ll[1])) found=true;
                    else sb.append(line).append("\r\n");
                }
                ins.close();
                if (sb.length()==0) logFile.delete();
                else ArchiveUtils.writeToFile(logFile,sb.toString());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }
        }
        return "ok";
    }

    @ResponseBody
    @RequestMapping(value = "auto-delete-same")
    public String autoDeleteSame(HttpServletRequest request, HttpServletResponse response) {
        BufferedReader ins=null;
        StringBuilder sb=new StringBuilder();
        try {
            File logFile = new File(rootPath, ArchiveInfo.same_photo_log);
            ins = new BufferedReader(new InputStreamReader(new FileInputStream(logFile),"GBK"));
            String line = null;
            while ((line=ins.readLine())!=null) {
                String [] ll= line.split(" <-> ");
                if (ll.length==2) {
                    String f1=ll[0].trim(), f2 = ll[1].trim();
                    File file1=new File(f1);
                    File file2=new File(f2);
                    if (!autoDeleteSame(file1,file2)) sb.append(line).append("\r\n");
                }
            }
            ins.close();
            if (sb.length()==0) logFile.delete();
            else ArchiveUtils.writeToFile(logFile,sb.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
        return "ok";
    }


    @ResponseBody
    @RequestMapping(value = "shutdown")
    public String shutdown(HttpServletRequest request, HttpServletResponse response, Integer delay) {
        CommandRunner.shutdown(delay==null?10:delay);
        return "ok";
    }

    void freeMarkerWriter(final String ftl, String filePath, Map<String, Object> attributes) throws Exception {
        Template template;
        template = CONFIGURATION.getTemplate(ftl);
        FileOutputStream fos = new FileOutputStream(filePath);
        Writer out = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"), 102400);
        template.process(attributes, out);
        out.close();
    }

    String freeMarkerWriter(final String ftl, Map<String,Object> model) throws Exception {
        Template template;
        template = CONFIGURATION.getTemplate(ftl);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(02400);
        Writer out = new BufferedWriter(new OutputStreamWriter(bos, "utf-8"), 102400);
        template.process(model, out);
        out.close();
        return new String(bos.toByteArray(),"utf-8");
    }
    @ResponseBody
    @RequestMapping(value = "resource")
    public String resource(HttpServletRequest request, HttpServletResponse response, String path, String current) {
        if (path==null || path.isEmpty()) return "error";
        try {
            Map<String, Object> pa = getPathAttributes(path, true);
            pa.put("currentPath",current);
            String resourceHtml = freeMarkerWriter("resource.ftl", pa);
            return resourceHtml;
        } catch (Exception e) {
            return "error";
        }
    }

    @ResponseBody
    @RequestMapping(value = "save", method = POST)
    public String saveEditor(HttpServletRequest request, String source,String body) {
        if (source==null) return "error";
        try {
            String html = ArchiveUtils.getFromFile(new File(source),"UTF8");
            if (html==null) return "文件错误";
            Pattern p=Pattern.compile("(\\<body[^\\>]*\\>)(.*)\\</body\\>",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            Matcher m = p.matcher(html);
            if (m.find()) {
                html = html.substring(0,m.start())+m.group(1) +"\n" + body + "\n</body>" + html.substring(m.end());
                ArchiveUtils.writeToFile(new File(source),html,"UTF8");
                new File(source+"_ed.html").delete();
                return "ok";
            } else return "not found body";

        } catch (Exception e) {
            return e.getMessage();
        }
    }
    private void scanFile(File srcFile) {
        try {
            String name = srcFile.getName();
            String dir = srcFile.getParentFile().getCanonicalPath();
            List<PhotoInfo> list = archiveInfo.getInfos().stream().filter(p -> name.equals(p.getFileName())).collect(Collectors.toList());
            if (list != null && list.size() > 0) {
                int maxMatch = 0, position=0;
                for (int i=0;i<list.size();i++) {
                    int macher = 0;
                    String path = new File(rootPath,list.get(i).getSubFolder()).getCanonicalPath();
                    while (macher<path.length() && macher<dir.length() && path.charAt(macher)==dir.charAt(macher)) macher++;
                    if (macher>maxMatch) {
                        maxMatch = macher;
                        position = i;
                    }
                }
                Files.copy(new File(list.get(position).fullPath(rootPath)).toPath(), srcFile.toPath());
            } else System.out.println("Not found "+srcFile.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void scanImageForHtml(String path) {
        try {
            String dir = new File(rootPath, path).getParentFile().getCanonicalPath();
            String html = ArchiveUtils.getFromFile(new File(rootPath, path), "UTF8");
            if (html == null) return;
            Pattern p = Pattern.compile("\\<(img|video|audio).*?\\ssrc\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            Matcher m = p.matcher(html);
            while (m.find()) {
                String src=m.group(2).trim();
                if (src.startsWith("/")) src = rootPath+src;
                else src = dir +"/" + src;
                File recFile = new File(src);
                if (!recFile.exists()) scanFile(recFile);
            }
        } catch (Exception e) {

        }
    }

    @RequestMapping(value = "editor")
    public Object editor(Model model,HttpServletRequest request, HttpServletResponse response, String path, Boolean scanResource) {
        if (path==null || path.isEmpty()) {
            model.addAttribute("message","请指定一个文件");
            return "error";
        }
        String html = ArchiveUtils.getFromFile(new File(rootPath,path),"UTF8");
        if (html==null) {
            model.addAttribute("message",path + " 打开失败");
            return "error";
        }
        if (scanResource!=null && scanResource) {
            scanImageForHtml(path);
        }
        Map<String, Object> attributes = new HashMap<>();
        Pattern p=Pattern.compile("\\<title\\>(.*)\\</title\\>",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(html);
        if (m.find()) attributes.put("title",m.group(1));
        p=Pattern.compile("(\\<body[^\\>]*\\>)(.*)\\</body\\>",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        m = p.matcher(html);
        if (m.find()) attributes.put("body",m.group(2));
        try {
            Map<String, Object> pa = getPathAttributes(new File(path).getParent(), true);
            pa.put("currentPath",new File(path).getParent().replaceAll("\\\\","/"));
            String resourceHtml = freeMarkerWriter("resource.ftl",pa);
            String reUrl = path + "_ed.html";
            attributes.put("resource",resourceHtml);
            attributes.put("sourceFile",new File(rootPath,path).getCanonicalPath());
            freeMarkerWriter("edit_html.ftl", new File(rootPath,reUrl).getCanonicalPath(), attributes);
            return new RedirectView(new String(reUrl.getBytes("UTF-8"),"iso-8859-1"));
        } catch (Exception e) {
            model.addAttribute("message",e.getMessage());
            return "error";
        }
    }

    String setUseDefault(String value, String def) {
        if (value==null || value.trim().isEmpty()) return def;
        else return value;
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

        CONFIGURATION.setTemplateLoader(new ClassTemplateLoader(PVController.class, "/templates"));
        CONFIGURATION.setDefaultEncoding("UTF-8");
        CONFIGURATION.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        CONFIGURATION.setCacheStorage(NullCacheStorage.INSTANCE);

        if (!isDebug) System.setOut(new PrintStream(new File(STDOUT)));
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
            }
        }.start();
    }
}
