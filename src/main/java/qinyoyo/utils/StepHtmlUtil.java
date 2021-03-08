package qinyoyo.utils;

import lombok.NonNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class StepHtmlUtil {
    public static final String STYLE_BEGIN = "/*step-html-style-begin*/";
    public static final String STYLE_END = "/*step-html-style-end*/";
    public static final String SCRIPT_BEGIN = "/*image-lazy-load-begin*/";
    public static final String SCRIPT_END = "/*image-lazy-load-end*/";
    public static final String STANDARD_HEAD = "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n" +
            "    <meta name=\"renderer\" content=\"webkit\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no\">\n" +
            "    <meta name=\"apple-mobile-web-app-capable\" content=\"yes\">\n" +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "    <link rel=\"stylesheet\" href=\"/static/font-awesome-4.7.0/css/font-awesome.min.css\">\n" +
            "    <link rel=\"stylesheet\" href=\"/static/css/transform_image.css\">\n" +
            "    <script type=\"text/javascript\" src=\"/static/js/ajax.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"/static/js/alloy_finger.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"/static/js/transform_image.js\"></script>";
    private static String getElementTagAndDeleteRange(Element e, String tag, String deleteFrom, String deleteEnd) {
        Elements sub = e.getElementsByTag(tag);
        if (sub!=null) {
            String hs = sub.html();
            return Util.replaceBetween(hs,deleteFrom,deleteEnd,"");
        }
        return "";
    }
    public static Document formatStepHtml(File htmlFile, String defTitle) throws IOException {
        if (htmlFile==null || !htmlFile.exists() || htmlFile.isDirectory()) throw new FileNotFoundException();
        Document doc = Jsoup.parse(htmlFile,"UTF8");
        if (doc!=null) {
            String style = "", script = "";
            Element head = doc.head();
            if (head==null) throw new IOException("没有找到head");
            Elements headStyles = head.getElementsByTag("style");
            if (headStyles!=null) {
                String hs = headStyles.html();
                if (hs!=null && !hs.isEmpty()) style = style + (style.isEmpty()?"":"\n")+ hs;
                headStyles.remove();
            }
            Elements links = head.getElementsByTag("link");
            for(Iterator iterator = links.iterator(); iterator.hasNext(); ) {
                Element element = (Element)iterator.next();
                if (element.hasAttr("href")) {
                    if (element.attr("href").endsWith("/font-awesome.min.css") ||
                            element.attr("href").endsWith("/font-awesome.css") ||
                            element.attr("href").endsWith("/editor.css") ||
                            element.attr("href").endsWith("/transform_image.css")
                    ) element.remove();
                }
            }
            new Element("link")
                    .attr("rel","stylesheet")
                    .attr("href","/static/font-awesome-4.7.0/css/font-awesome.min.css")
                    .appendTo(head);
            new Element("link")
                    .attr("rel","stylesheet")
                    .attr("href","/static/css/transform_image.css")
                    .appendTo(head);

            Elements headScrips = head.getElementsByTag("script");
            if (headScrips!=null) {
                Element element;
                StringBuilder sb =new StringBuilder();
                for(Iterator iterator = headScrips.iterator(); iterator.hasNext(); ) {
                    element = (Element)iterator.next();
                    if (element.hasAttr("src")) {
                        if (element.attr("src").endsWith("/ajax.js") ||
                                element.attr("src").endsWith("/alloy_finger.js") ||
                                element.attr("src").endsWith("/transform_image.js")
                        ) element.remove();
                    } else {
                        if (sb.length() != 0) sb.append("\n");
                        sb.append(element.html());
                        element.remove();
                    }
                }
            }
            new Element("script")
                    .attr("type","text/javascript")
                    .attr("src","/static/js/ajax.js")
                    .appendTo(head);
            new Element("script")
                    .attr("type","text/javascript")
                    .attr("src","/static/js/alloy_finger.js")
                    .appendTo(head);
            new Element("script")
                    .attr("type","text/javascript")
                    .attr("src","/static/js/transform_image.js")
                    .appendTo(head);

            Element newHead = new Element("head");
            newHead.html(STANDARD_HEAD);
            for(Iterator iterator = links.iterator(); iterator.hasNext(); ) {
                Element element = (Element)iterator.next();
                element.appendTo(newHead);
            }
            for(Iterator iterator = headScrips.iterator(); iterator.hasNext(); ) {
                Element element = (Element)iterator.next();
                element.appendTo(newHead);
            }
            if (head.getElementsByTag("title")!=null) {
                head.getElementsByTag("title").first().appendTo(newHead);
            } else if (defTitle!=null && !defTitle.isEmpty()) {
                new Element("title").text(defTitle).appendTo(newHead);
            }
            String editStyle = FileUtil.getFromResource("/static/css/editor.css");
            if (editStyle!=null) {
                editStyle = STYLE_BEGIN + "\n" + editStyle + "\n" + STYLE_END;
                style = Util.replaceBetween(style, STYLE_BEGIN, STYLE_END, "");
                if (style.isEmpty()) style = editStyle;
                else style = style + "\n" + editStyle;
            }
            String lazyScript = FileUtil.getFromResource("/static/js/image_lazy_load.js");
            if (lazyScript!=null) {
                lazyScript = SCRIPT_BEGIN + "\n" + lazyScript + "\n" + SCRIPT_END;
                script = Util.replaceBetween(script, SCRIPT_BEGIN, SCRIPT_END, "");
                if (script.isEmpty()) script = lazyScript;
                else  script = script + "\n" + lazyScript;
            }
            new Element("style").html(style).appendTo(newHead);
            new Element("script").html(script).appendTo(newHead);
            head.html(newHead.html());
            doc.body().removeAttr("contenteditable");
            Elements imgs = doc.select("img.lazy-load");
            if (imgs!=null) {
                for(Iterator iterator = links.iterator(); iterator.hasNext(); ) {
                    Element element = (Element)iterator.next();
                    if (element.hasAttr("src") && !element.hasAttr("data-src")) {
                        element.attr("data-src",element.attr("src"));
                        element.attr("src","/static/image/loading.gif");
                    }
                }
            }
        }
        return doc;
    }
    public static String extraStyle(@NonNull Document doc) throws IOException {

            Element head = doc.head();
            if (head==null) throw new IOException("没有找到head");
            Elements headStyles = head.getElementsByTag("style");
            if (headStyles!=null) {
                String hs = headStyles.html();
                if (hs!=null && !hs.isEmpty()) style = style + (style.isEmpty()?"":"\n")+ hs;
                headStyles.remove();
            }
            Elements links = head.getElementsByTag("link");
            for(Iterator iterator = links.iterator(); iterator.hasNext(); ) {
                Element element = (Element)iterator.next();
                if (element.hasAttr("href")) {
                    if (element.attr("href").endsWith("/font-awesome.min.css") ||
                            element.attr("href").endsWith("/font-awesome.css") ||
                            element.attr("href").endsWith("/editor.css") ||
                            element.attr("href").endsWith("/transform_image.css")
                    ) element.remove();
                }
            }
            new Element("link")
                    .attr("rel","stylesheet")
                    .attr("href","/static/font-awesome-4.7.0/css/font-awesome.min.css")
                    .appendTo(head);
            new Element("link")
                    .attr("rel","stylesheet")
                    .attr("href","/static/css/transform_image.css")
                    .appendTo(head);

            Elements headScrips = head.getElementsByTag("script");
            if (headScrips!=null) {
                Element element;
                StringBuilder sb =new StringBuilder();
                for(Iterator iterator = headScrips.iterator(); iterator.hasNext(); ) {
                    element = (Element)iterator.next();
                    if (element.hasAttr("src")) {
                        if (element.attr("src").endsWith("/ajax.js") ||
                                element.attr("src").endsWith("/alloy_finger.js") ||
                                element.attr("src").endsWith("/transform_image.js")
                        ) element.remove();
                    } else {
                        if (sb.length() != 0) sb.append("\n");
                        sb.append(element.html());
                        element.remove();
                    }
                }
            }
            new Element("script")
                    .attr("type","text/javascript")
                    .attr("src","/static/js/ajax.js")
                    .appendTo(head);
            new Element("script")
                    .attr("type","text/javascript")
                    .attr("src","/static/js/alloy_finger.js")
                    .appendTo(head);
            new Element("script")
                    .attr("type","text/javascript")
                    .attr("src","/static/js/transform_image.js")
                    .appendTo(head);

            Element newHead = new Element("head");
            newHead.html(STANDARD_HEAD);
            for(Iterator iterator = links.iterator(); iterator.hasNext(); ) {
                Element element = (Element)iterator.next();
                element.appendTo(newHead);
            }
            for(Iterator iterator = headScrips.iterator(); iterator.hasNext(); ) {
                Element element = (Element)iterator.next();
                element.appendTo(newHead);
            }
            if (head.getElementsByTag("title")!=null) {
                head.getElementsByTag("title").first().appendTo(newHead);
            } else if (defTitle!=null && !defTitle.isEmpty()) {
                new Element("title").text(defTitle).appendTo(newHead);
            }
            String editStyle = FileUtil.getFromResource("/static/css/editor.css");
            if (editStyle!=null) {
                editStyle = STYLE_BEGIN + "\n" + editStyle + "\n" + STYLE_END;
                style = Util.replaceBetween(style, STYLE_BEGIN, STYLE_END, "");
                if (style.isEmpty()) style = editStyle;
                else style = style + "\n" + editStyle;
            }
            String lazyScript = FileUtil.getFromResource("/static/js/image_lazy_load.js");
            if (lazyScript!=null) {
                lazyScript = SCRIPT_BEGIN + "\n" + lazyScript + "\n" + SCRIPT_END;
                script = Util.replaceBetween(script, SCRIPT_BEGIN, SCRIPT_END, "");
                if (script.isEmpty()) script = lazyScript;
                else  script = script + "\n" + lazyScript;
            }
            new Element("style").html(style).appendTo(newHead);
            new Element("script").html(script).appendTo(newHead);
            head.html(newHead.html());
            doc.body().removeAttr("contenteditable");
            Elements imgs = doc.select("img.lazy-load");
            if (imgs!=null) {
                for(Iterator iterator = links.iterator(); iterator.hasNext(); ) {
                    Element element = (Element)iterator.next();
                    if (element.hasAttr("src") && !element.hasAttr("data-src")) {
                        element.attr("data-src",element.attr("src"));
                        element.attr("src","/static/image/loading.gif");
                    }
                }
            }
        }
        return doc;
    }
}
