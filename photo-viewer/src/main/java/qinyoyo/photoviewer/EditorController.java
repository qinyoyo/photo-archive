package qinyoyo.photoviewer;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import qinyoyo.photoinfo.ArchiveUtils;
import qinyoyo.photoinfo.archive.ArchiveInfo;
import qinyoyo.photoinfo.archive.PhotoInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class EditorController implements ApplicationRunner {
    @Autowired
    PVController pvController;
    private static final Configuration CONFIGURATION = new Configuration(Configuration.VERSION_2_3_22);
    @RequestMapping(value = "editor")
    public Object editor(Model model, HttpServletRequest request, HttpServletResponse response, String path, Boolean scanResource) {
        SessionOptions options = SessionOptions.getSessionOptions(request);
        if (path==null || path.isEmpty()) {
            model.addAttribute("message","请指定一个文件");
            return "message";
        }
        String rootPath = pvController.getRootPath();
        String html = ArchiveUtils.getFromFile(new File(rootPath,path),"UTF8");
        if (html==null) {
            model.addAttribute("message",path + " 打开失败");
            return "message";
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
            Map<String, Object> pa = pvController.getPathAttributes(new File(path).getParent(), true, options.isFavoriteFilter());
            pa.put("currentPath",new File(path).getParent().replaceAll("\\\\","/"));
            String resourceHtml = freeMarkerWriter("resource.ftl",pa);
            String reUrl = path + "_ed.html";
            attributes.put("resource",resourceHtml);
            attributes.put("sourceFile",new File(rootPath,path).getCanonicalPath());
            freeMarkerWriter("edit_html.ftl", new File(rootPath,reUrl).getCanonicalPath(), attributes);
            return new RedirectView(new String(reUrl.getBytes("UTF-8"),"iso-8859-1"));
        } catch (Exception e) {
            model.addAttribute("message",e.getMessage());
            return "message";
        }
    }

    @ResponseBody
    @RequestMapping(value = "resource")
    public String resource(HttpServletRequest request, HttpServletResponse response, String path, String current) {
        if (path==null || path.isEmpty()) return "error";
        try {
            Map<String, Object> pa = pvController.getPathAttributes(path, true, SessionOptions.getSessionOptions(request).isFavoriteFilter());
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
    private void scanImageForHtml(String path) {
        try {
            String rootPath = pvController.getRootPath();
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
    private void scanFile(File srcFile) {
        try {
            String rootPath = pvController.getRootPath();
            ArchiveInfo archiveInfo = pvController.getArchiveInfo();
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
        pvController.initStatics(model);
        template.process(model, out);
        out.close();
        return new String(bos.toByteArray(),"utf-8");
    }

    public void createHtmlFile(String rootPath, String path, String newStep, ArchiveInfo archiveInfo) {
        try {
            File dir = new File(new File(rootPath, path), newStep+".web");
            if (dir.mkdirs()) {
                Map<String,Object> attr = new HashMap<String,Object>(){{
                    put("title",newStep);
                }};
                freeMarkerWriter("newStep.ftl",dir.getAbsolutePath()+File.separator+"index.html",attr);
                archiveInfo.rescanFile(new File(dir,"index.html"));
            }
        } catch (Exception e) {}
    }
    @Override
    public void run(ApplicationArguments args) throws Exception {
        CONFIGURATION.setTemplateLoader(new ClassTemplateLoader(PVController.class, "/templates"));
        CONFIGURATION.setDefaultEncoding("UTF-8");
        CONFIGURATION.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        CONFIGURATION.setCacheStorage(NullCacheStorage.INSTANCE);
    }
}
