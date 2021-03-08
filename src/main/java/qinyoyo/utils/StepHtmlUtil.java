package qinyoyo.utils;

import lombok.NonNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StepHtmlUtil {
    public static final String STYLE_BEGIN = "/*step-html-style-begin*/";
    public static final String STYLE_END = "/*step-html-style-end*/";
    public static final String SCRIPT_BEGIN = "/*image-lazy-load-begin*/";
    public static final String SCRIPT_END = "/*image-lazy-load-end*/";
    public static final String STANDARD_META = "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n" +
            "    <meta name=\"renderer\" content=\"webkit\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no\">\n" +
            "    <meta name=\"apple-mobile-web-app-capable\" content=\"yes\">\n" +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">";
    public static final String STANDARD_LINK = "    <link rel=\"stylesheet\" href=\"/static/font-awesome-4.7.0/css/font-awesome.min.css\">\n" +
            "    <link rel=\"stylesheet\" href=\"/static/css/transform_image.css\">";
    public static final String STANDARD_SCRIPT = "    <script type=\"text/javascript\" src=\"/static/js/ajax.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"/static/js/alloy_finger.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"/static/js/transform_image.js\"></script>";

    public static boolean isStandardElement(@NonNull Element e) {
        if (e.is("meta")) return true;
        else if (e.is("link") && e.hasAttr("href") &&
                (e.attr("href").endsWith("/font-awesome.min.css") ||
                e.attr("href").endsWith("/font-awesome.css") ||
                e.attr("href").endsWith("/editor.css") ||
                e.attr("href").endsWith("/transform_image.css"))) return true;
        else if (e.is("script") && e.hasAttr("src") &&
                (e.attr("src").endsWith("/ajax.js") ||
                e.attr("src").endsWith("/alloy_finger.js") ||
                e.attr("src").endsWith("/image_lazy_load.js") ||
                e.attr("src").endsWith("/transform_image.js"))) return true;
        else if (e.is("style")) {
            String style = e.html();
            if (style==null) return true;
            style = Util.replaceBetween(style, STYLE_BEGIN, STYLE_END, "");
            if (style.isEmpty()) return true;
            e.html(style);
            return false;
        } else if (e.is("script") && !e.hasAttr("src")) {
            String script = e.html();
            if (script==null) return true;
            script = Util.replaceBetween(script, STYLE_BEGIN, STYLE_END, "");
            if (script.isEmpty()) return true;
            e.html(script);
            return false;
        } else return false;
    }

    public static Document formattedStepHtml(Document doc, Element ... additional) throws IOException {
        if (doc==null) throw new FileNotFoundException();
        doc.outputSettings().outline(true).prettyPrint(true).indentAmount(2);
        Element head = doc.head();
        if (head==null) head=new Element("head");
        List<Element> extraElements = head.children().stream().filter(e->!isStandardElement(e)).collect(Collectors.toList());

        Element newHead = new Element("head");
        newHead.html(STANDARD_META);
        newHead.append(STANDARD_LINK);
        for (Element e : extraElements.stream().filter(e->e.is("link")).collect(Collectors.toList()))
            e.appendTo(newHead);
        newHead.append(STANDARD_SCRIPT);
        for (Element e : extraElements.stream().filter(e->e.is("script") && e.hasAttr("src")).collect(Collectors.toList()))
            e.appendTo(newHead);

        String style = Util.editCss();
        if (style==null) style="";
        else style = STYLE_BEGIN + "\n" + style + STYLE_END + "\n";
        for (Element e : extraElements.stream().filter(e->e.is("style")).collect(Collectors.toList())) {
            String html = e.text();
            if (html!=null && !html.isEmpty()) style = style + (style.isEmpty()?"":"\n") + html;
        }
        if (!style.isEmpty()) new Element("style").text(style).appendTo(newHead);

        String script = Util.lazyLoadScript();
        if (script==null) script="";
        script = SCRIPT_BEGIN + "\n" + script + SCRIPT_END + "\n";
        for (Element e : extraElements.stream().filter(e->e.is("script") && !e.hasAttr("src")).collect(Collectors.toList())) {
            String html = e.text();
            if (html!=null && !html.isEmpty()) script = script + (script.isEmpty()?"":"\n") + html;
        }
        if (!script.isEmpty()) new Element("script").text(script).appendTo(newHead);

        for (Element e : extraElements.stream().filter(e->!e.is("script") && !e.is("link") && !e.is("style")).collect(Collectors.toList())) {
            e.appendTo(newHead);
        }
        if (additional!=null) for (Element e: additional) e.appendTo(newHead);

        doc.head().html(newHead.html());

        doc.body().removeAttr("contenteditable");

        Elements imgs = doc.select("img.lazy-load");
        if (imgs!=null) {
            for(Element e : imgs) {
                if (e.hasAttr("src") && !e.hasAttr("data-src")) {
                    e.attr("data-src",e.attr("src"));
                    e.attr("src","/static/image/loading.gif");
                }
            }
        }
        return doc;
    }
    public static Document formattedStepHtml(File htmlFile, Element ... additional) throws IOException {
        if (htmlFile==null || !htmlFile.exists() || htmlFile.isDirectory()) throw new FileNotFoundException();
        Document doc = Jsoup.parse(htmlFile,"UTF8");
        if (doc==null) throw new FileNotFoundException();
        return formattedStepHtml(doc,additional);
    }
    public static String htmlString(Document doc) {
        doc.outputSettings().outline(true).prettyPrint(true).indentAmount(2);

        return doc.toString();
    }
}
