package qinyoyo.utils;


import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.text.CollationKey;
import java.text.Collator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用工具类，提供一些常用的静态函数方法
 */

public class Util {
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
        } else return o.toString().trim().isEmpty();
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
     * 对象转换为string，避免为null是的错误
     * @param o	 对象
     * @return	对象转换为string
     */
    static public String toString(Object o) {
        if (o == null)
            return null;
        else if (o instanceof Date) {
            return DateUtil.date2String((Date)o, null);
        } else
            return o.toString();
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
            if (s.equals("1") || s.equals("true") || s.startsWith("y") || s.equals("t") || s.equals(".t."))
                return true;
            else
                return false;
        }
    }

    static public String getStackTrace(Exception e) {
        if (e==null) return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String msg = e.getMessage();
        if (msg==null) msg="";
        else msg = msg + "\n";
        return msg + sw.toString();
    }
    static public void printStackTrace(Exception e) {
        if (System.console()!=null || ("file".equals(Util.class.getResource("").getProtocol()))) {
            e.printStackTrace();
        }
    }
    static public String replaceBetween(String str, @NonNull String begin, @NonNull String end, @NonNull String newText) {
        if (str==null) return "";
        while (true) {
            int s = str.indexOf(begin);
            if (s < 0) return str;
            int e = str.indexOf(end);
            if (e < 0 || e <= s) return str;
            e += end.length();
            str = (s > 0 ? str.substring(0, s) : "") + newText + (e < str.length() ? str.substring(e) : "");
        }
    }
    public static String stepHtmlCss() {
        return FileUtil.getFromResource("/static/css/step.css");
    }
    public static String lazyLoadScript() {
        return FileUtil.getFromResource("/static/js/image_lazy_load.js");
    }
    public static Map<String,Object> getYaml(File file) {
        Yaml yml = new Yaml();
        Reader reader = null;
        try {
            reader = new FileReader(file);
            return yml.loadAs(reader, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }
    /**
     * 设置对象属性
     * @param obj  对象
     * @param fieldName 属性名
     * @param value  属性值
     */
    static public void setPrivateField(Object obj, String fieldName,Object value) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    /**
     * 获得对象属性
     * @param obj   对象
     * @param fieldName  属性名
     * @return 返回对象的私有属性值
     */
    static public Object getPrivateField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    static public int chineseCompare(String c1,String c2) {
        Collator cmp = Collator.getInstance(java.util.Locale.CHINA);
        CollationKey ck1 = cmp.getCollationKey(c1);
        CollationKey ck2 = cmp.getCollationKey(c2);
        return cmp.compare(ck1.getSourceString(), ck2.getSourceString());
    }
}
