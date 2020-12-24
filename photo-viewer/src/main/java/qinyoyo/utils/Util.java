package qinyoyo.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import javax.servlet.http.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用工具类，提供一些常用的静态函数方法
 */

public class Util {
    private static final Logger logger = LoggerFactory.getLogger(Util.class);
    public static boolean runInIdeMode = false;
    static public Long toLong(Object o) {
        if (o == null)
            return null;
        else {
            try {
                Double d = Double.parseDouble(o.toString());
                return d.longValue();
            } catch (Exception e) {
                return null;
            }
        }
    }

    static public Integer toInt(Object o) {
        if (o == null)
            return null;
        else {
            try {
                Double d = Double.parseDouble(o.toString());
                return (int) d.longValue();
            } catch (Exception e) {
                return null;
            }
        }
    }

    static public <T> T null2Default(T obj, T def) {
        if (obj == null) return def;
        else return obj;
    }

    /**
     * @param o : 对象
     * @return ：判断对象是否为null或空串，包括全部为空白字符的串
     */
    static public boolean isEmpty(Object o) {
        if (o == null) return true;
        else if (o.getClass().isArray()) {
           return ((Object[])o).length <= 0;
        } else if (o instanceof Collection) {
            return ((Collection)o).size() <= 0;
        } else return StringUtil.toString(o).trim().isEmpty();
    }

    static public boolean isMobile(HttpServletRequest request) {
        String userAgent = request.getHeader( "USER-AGENT" );
        if(isEmpty(userAgent)) return false;
        final String phoneReg = "\\b(ip(hone|od)|android|opera m(ob|in)i"
                +"|windows (phone|ce)|blackberry"
                +"|s(ymbian|eries60|amsung)|p(laybook|alm|rofile/midp"
                +"|laystation portable)|nokia|fennec|htc[-_]"
                +"|mobile|up.browser|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";
        Pattern phonePat = Pattern.compile(phoneReg, Pattern.CASE_INSENSITIVE);
        Matcher matcherPhone = phonePat.matcher(userAgent);
        return matcherPhone.find();
    }


    /**
     * 判断串是否相等
     *
     * @param s1 第一个串
     * @param s2 第二个串
     * @return 两个串相等或均为空时返回真，大小写敏感
     */
    static public boolean equals(Object s1, Object s2) {
        if (s1==null && s2==null) return true;
        else if (s1 != null && s2 != null)
            return s1.toString().trim().equals(s2.toString().trim());
        else if (s1 == null)
            return isEmpty(s2);
        else
            return isEmpty(s1);
    }

    /**
     * 为空时返回缺省值，否则返回对象的toString
     *
     * @param o   对象
     * @param def 缺省值
     * @return o为空时返回缺省值，否则返回对象的toString
     */
    static public String isEmpty(Object o, String def) {
        if (o == null)
            return def;
        else
            return (o.toString().isEmpty() ? def : o.toString());
    }

    /**
     * 根据字符串解析一个boolean值
     *
     * @param obj 对象
     * @return 解析一个boolean值
     */
    static public boolean boolValue(Object obj) {
        if (obj == null) return false;
        else if (obj instanceof Boolean) return (Boolean) obj;
        else if (obj instanceof Integer) return (Integer) obj != 0;
        else if (obj instanceof Short) return (Short) obj != 0;
        else if (obj instanceof Byte) return (Byte) obj != 0;
        else if (obj instanceof Long) return (Long) obj != 0;
        else {
            String s = obj.toString().trim().toLowerCase();
            if (s.equals("1") || s.equals("true") || s.equals("yes") || s.equals("t") || s.equals("y") || s.equals(".t."))
                return true;
            else
                return false;
        }
    }





    /**
     * 判断资源文件是否存在
     *
     * @param resourceFile 资源文件名，全名，包括static,template等目录前缀
     * @return 是否存在
     */
    static public boolean exists(String resourceFile) {
        Resource resource = new ClassPathResource(resourceFile);
        try {
            InputStream is = resource.getInputStream();
            is.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    /**
     * 获得完整url地址
     *
     * @param url url
     * @return 完整地址
     */
    static public String realUrl(String url) {
        if (url == null) return null;
        if (url.toLowerCase().startsWith("http")) return url;
        String contextPath = SpringContextUtil.getContextPath();

        if (Util.isEmpty(contextPath) || contextPath.equals("/"))
            return url;
        if (contextPath.endsWith("/") && url.startsWith("/"))
            return contextPath + url.substring(1);
        else
            return contextPath + url;
    }
    /**
     * 获取ip地址
     *
     * @param request request
     * @return 从 request获得客户ip地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null)
            request = SpringContextUtil.getRequest();
        if (request == null)
            return null;
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.equals("0:0:0:0:0:0:0:1")) {
            ip = "localhost";
        }
        return ip;
    }

    public static Object valueOf(Object obj, Class<?> clazz) {
        if (obj == null || clazz == null) return null;
        String type = clazz.getName();
        String s = obj.toString();
        try {
            if (obj.getClass().equals(clazz)) return obj;
            if (type.endsWith("Boolean")) return Util.boolValue(s);
            else if (type.endsWith("String")) return s;
            else if (type.endsWith("Integer")) return Integer.valueOf(s);
            else if (type.endsWith("Long")) return Long.valueOf(s);
            else if (type.endsWith("Short")) return Short.valueOf(s);
            else if (type.endsWith("Byte")) return Byte.valueOf(s);
            else if (type.endsWith("Double")) return Double.valueOf(s);
            else if (type.endsWith("Float")) return Float.valueOf(s);
            else if (type.endsWith("Date")) return DateUtil.string2Date(s);
            else if (type.endsWith("byte[]")) return s.getBytes();
        } catch (Exception e) {
        }
        if (type.equals("boolean")) return Util.boolValue(s);
        else if (type.equals("char")) {
            if (s.isEmpty() || s == null) return (char) 0;
            else return s.charAt(0);
        } else if (type.equals("int")) {
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                return (int) 0;
            }
        } else if (type.equals("long")) {
            try {
                return Long.parseLong(s);
            } catch (Exception e) {
                return (long) 0;
            }
        } else if (type.equals("short")) {
            try {
                return Short.parseShort(s);
            } catch (Exception e) {
                return (short) 0;
            }
        } else if (type.equals("byte")) {
            try {
                return Byte.parseByte(s);
            } catch (Exception e) {
                return (byte) 0;
            }
        } else if (type.equals("double")) {
            try {
                return Double.parseDouble(s);
            } catch (Exception e) {
                return (double) 0;
            }
        } else if (type.equals("float")) {
            try {
                return Float.parseFloat(s);
            } catch (Exception e) {
                return (float) 0;
            }
        }
        return null;
    }

    /**
     * 获得cookie值
     *
     * @param request request
     * @param name    名称
     * @return cookie值
     */
    static public String getCookie(HttpServletRequest request, String name) {
        if (request == null || name == null)
            return null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (equals(c.getName(), name))
                    return c.getValue();
            }
        }
        return null;
    }

    /**
     * 设置cookie值
     *
     * @param request  request
     * @param response response
     * @param name     名称
     * @param value    值
     * @param age      时效
     */
    static public void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, int age) {
        if (response == null || name == null)
            return;
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(age);
        String contextPath = request.getContextPath();
        cookie.setPath(contextPath.length() > 0 ? contextPath : "/");
        cookie.setSecure(request.isSecure());
        response.addCookie(cookie);
    }
    /*
    public static ServletContext getServletContext() {
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        return webApplicationContext.getServletContext();
    }

    public static ApplicationContext getApplicationContext() {
        ServletContext sc = getServletContext();
        return WebApplicationContextUtils.getWebApplicationContext(sc);
    }
   */


    private static String additionalPropertyFile(@NonNull String path, @NonNull String fileName) {
        PropertiesPropertySourceLoader propLoader = new PropertiesPropertySourceLoader();
        YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();
        String ext = null;
        File file = null;
        int dp = fileName.lastIndexOf(".");
        if (dp > 0) {
            ext = fileName.substring(dp + 1).toLowerCase();
            file = new File(path, fileName);
            if (file != null && file.exists()) {
                if (Arrays.asList(yamlLoader.getFileExtensions()).contains(ext)) {
                    return file.getAbsolutePath();
                } else {
                    if (Arrays.asList(propLoader.getFileExtensions()).contains(ext)) {
                        return file.getAbsolutePath();
                    }
                }
            }
        } else {
            for (String e : propLoader.getFileExtensions()) {
                file = new File(path, fileName + "." + e);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
            if (file == null || !file.exists()) {
                for (String e : yamlLoader.getFileExtensions()) {
                    file = new File(path, fileName + "." + e);
                    if (file.exists()) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    private static String additionalPropertyFile(@NonNull String fileName) {
        String path = SpringContextUtil.getProjectHomeDirection();
        if (isEmpty(path)) return null;
        String fullName = additionalPropertyFile(path, fileName);
        return fullName;
    }


    public static int getProcessID(Logger log) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        log.info(runtimeMXBean.getName());
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
    }

    public static void commonApplicationRun(@NonNull Class<?> clazz, String applicationProperties, String customerProperties, String[] args) {
        Logger log = LoggerFactory.getLogger(Util.class);
        int pid = getProcessID(log);
        try {
            FileUtil.writeToFile(String.valueOf(pid), new File(SpringContextUtil.getProjectHomeDirection(),"pid.log"));
        } catch (Exception e) {}
        SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(clazz);
        appBuilder.properties("file.encoding=UTF-8");
        String myPropertyFile = additionalPropertyFile(customerProperties == null ? "iotequ" : customerProperties);
        if (myPropertyFile!=null) SpringContextUtil.setPropertyFile(myPropertyFile);
        if (applicationProperties != null) {
            String location = "spring.config.location=classpath:/" + applicationProperties +
                    (myPropertyFile == null ? "" : "," + myPropertyFile);
            appBuilder.properties(location);
        } else {
            if (myPropertyFile != null) {
                appBuilder.properties("spring.config.additional-location=" + myPropertyFile);
            }
        }
        appBuilder.run(args);
    }
}
