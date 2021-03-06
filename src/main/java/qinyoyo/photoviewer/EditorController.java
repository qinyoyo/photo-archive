package qinyoyo.photoviewer;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import qinyoyo.photoinfo.archive.Modification;
import qinyoyo.photoinfo.archive.PhotoInfo;
import qinyoyo.utils.DateUtil;
import qinyoyo.utils.FileUtil;
import qinyoyo.utils.StepHtmlUtil;
import qinyoyo.utils.Util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class EditorController implements ApplicationRunner {
    @Autowired
    PVController pvController;
    private static final Configuration CONFIGURATION = new Configuration(Configuration.VERSION_2_3_22);

    @RequestMapping(value = "**/editor")
    public Object editor(Model model, HttpServletRequest request, HttpServletResponse response, String html) {
        SessionOptions options = SessionOptions.getSessionOptions(request);
        if (!options.isUnlocked()) {
            model.addAttribute("message","请先解锁!!!");
            return "message";
        }
        if (html==null || html.isEmpty()) {
            model.addAttribute("message","请指定一个文件");
            return "message";
        }
        String currentUrl = request.getServletPath();
        currentUrl = currentUrl.substring(0,currentUrl.length()-6);
        if (currentUrl.startsWith("/")) currentUrl = currentUrl.substring(1);
        if (currentUrl.endsWith("/") ) currentUrl = currentUrl.substring(0,currentUrl.length()-1);

        String rootPath = pvController.getRootPath();
        String folder = ArchiveUtils.formatterSubFolder(currentUrl,rootPath);
        String htmlPath = folder + File.separator + html;

        Document doc;
        try {
            doc = Jsoup.parse(new File(rootPath,htmlPath),"utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("message",htmlPath + " 打开失败");
            return "message";
        }

        if (doc.title()!=null) model.addAttribute("title",doc.title());
        model.addAttribute("body",doc.body().html());
        try {

            if (folder.endsWith(".web")) folder = new File(folder).getParent();
            Optional<PhotoInfo> photoInfo = pvController.getArchiveInfo().subFolderInfos(folder,true).stream().filter(pi ->
                    pi.getMimeType() != null && pi.getMimeType().contains("image") && pi.getShootTime() != null).findFirst();
            Map<String, Object> pa = pvController.getPathAttributesByDate(DateUtil.date2String(photoInfo.isPresent() ? photoInfo.get().getShootTime() : new Date(),"yyyy-MM-dd"), options.isFavoriteFilter());

            pa.put("currentPath",currentUrl);
            pa.put("sessionOptions",options);
            String resourceHtml = freeMarkerWriter("resource.ftl",pa);
            model.addAttribute("resource",resourceHtml);
            model.addAttribute("sourceFile",new File(rootPath,htmlPath).getCanonicalPath());
            return "editor_html";
        } catch (Exception e) {
            model.addAttribute("message",e.getMessage());
            return "message";
        }
    }

    @ResponseBody
    @RequestMapping(value = "resource")
    public String resource(HttpServletRequest request, HttpServletResponse response, String path, String date, String current, Boolean folderOnly) {
        if (path==null) path="";
        if (current==null) current="";
        try {
            SessionOptions options = SessionOptions.getSessionOptions(request);
            Map<String, Object> pa = folderOnly!=null && folderOnly ? pvController.getFolderPathAttributes(path,true) :
                    (date==null ? pvController.getPathAttributes(path, true, options.isFavoriteFilter())
                            : pvController.getPathAttributesByDate(date, options.isFavoriteFilter()));
            if (current.startsWith("/") || current.startsWith("\\")) current = current.substring(1);
            if (current.endsWith("/") || current.endsWith("\\")) current = current.substring(0,current.length()-1);
            if (folderOnly!=null && folderOnly) pa.put("folderOnly",true);
            pa.put("currentPath",current);
            pa.put("sessionOptions",options);
            String resourceHtml = freeMarkerWriter("resource.ftl", pa);
            return resourceHtml;
        } catch (Exception e) {
            return "error";
        }
    }

    @ResponseBody
    @RequestMapping(value = "mkdir")
    public String mkdir(HttpServletRequest request, HttpServletResponse response, String current, String path) {
        if (Util.isEmpty(path)) return "请输入目录名";
        current = ArchiveUtils.formatterSubFolder(current,pvController.getRootPath());
        File dir = new File(new File(pvController.getRootPath(),current), path);
        if (dir.exists()) return "已存在";
        if (dir.mkdirs()) return "ok";
        else return "目录创建失败";
    }

    @ResponseBody
    @RequestMapping(value = "save", method = POST)
    public String saveEditor(HttpServletRequest request, String source,String body) {
        SessionOptions options = SessionOptions.getSessionOptions(request);
        if (!options.isUnlocked()) {
            return "请先解锁!!!";
        }
        if (source==null) return "error";
        try {
            Document doc = Jsoup.parse(new File(source),"UTF8");
            if (doc==null) return "文件错误";
            doc.body().html(body);
            doc.select("img.lazy-load").forEach(img->{
                if (img.hasAttr("data-src") && img.hasAttr("src")) img.removeAttr("src");
            });
            doc=StepHtmlUtil.formattedStepHtml(doc);
            FileUtil.writeToFile(new File(source),StepHtmlUtil.htmlString(doc),"UTF8");
            try {
                new File(source + "_ed.html").delete();
                String target = pvController.getRootPath() + File.separator + ".modified_steps"
                        + new File(source).getCanonicalPath().substring(pvController.getRootPath().length());
                new File(target).getParentFile().mkdirs();
                Modification.makeLink(source, target);
            } catch (Exception e1){ Util.printStackTrace(e1);}
            return "ok";
        } catch (Exception e) {
            return e.getMessage();
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

    static final String NEW_STEP_HTML = "<!doctype html>\n" +
            "<html>\n" +
            "<head>\n" +
            "  <title>TITLE</title>\n"+
            "</head>\n"+
            "<body>\n" +
            "  <div>\n" +
            "    <h2 style=\"text-align: center;\"> <u>TITLE</u> </h2>\n" +
            "      <div>输入游记内容</div>\n" +
            "  </div>\n" +
            "</body>\n</html>";
    public void createHtmlFile(String rootPath, String path, String newStep, ArchiveInfo archiveInfo) {
        try {
            File dir = new File(new File(rootPath, path), newStep+".web");
            if (!new File(dir,"index.html").exists()) {
                dir.mkdirs();
                Document doc = Jsoup.parse(NEW_STEP_HTML.replaceAll("TITLE", newStep));
                doc = StepHtmlUtil.formattedStepHtml(doc);
                FileUtil.writeToFile(new File(dir, "index.html"), StepHtmlUtil.htmlString(doc), "utf-8");
                archiveInfo.rescanFile(new File(dir, "index.html"));
            }
        } catch (Exception e){ Util.printStackTrace(e);}
    }
    @Override
    public void run(ApplicationArguments args) throws Exception {
        CONFIGURATION.setTemplateLoader(new ClassTemplateLoader(PVController.class, "/templates"));
        CONFIGURATION.setDefaultEncoding("UTF-8");
        CONFIGURATION.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        CONFIGURATION.setCacheStorage(NullCacheStorage.INSTANCE);
    }
}
