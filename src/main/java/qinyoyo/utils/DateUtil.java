package qinyoyo.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil {
    public static String date2String(Date dt,String fmt) {
        return date2String(dt,fmt,null);
    }
    public static String date2StringByZoneId(Date dt,String fmt, String zoneId) {
        return date2String(dt,fmt,TimeZone.getTimeZone(zoneId));
    }
    public static String date2String(Date dt,String fmt, TimeZone zone) {
        if (dt == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(fmt==null?"yyyy-MM-dd HH:mm:ss":fmt);
        if (zone!=null) sdf.setTimeZone(zone);
        return sdf.format(dt);
    }
    public static String date2String(Date dt) {
        return date2String(dt,"yyyy-MM-dd HH:mm:ss",null);
    }

    public static Date dayOf(Date dt) {
        return string2Date(date2String(dt,"yyyy-MM-dd")+" 00:00:00","yyyy-MM-dd HH:mm:ss");
    }
    public static Date nextDayOf(Date dt) {
        return new Date(dayOf(dt).getTime()+24l*60*60*1000);
    }
    public static Date string2Date(String s, String fmt) {
        return string2Date(s, fmt, null);
    }
    public static Date string2Date(String s, String fmt, TimeZone zone) {
        if (s == null) return null;
        else if (fmt==null) {
            DateFormat [] defaultFormats = new DateFormat[]{
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA),
                    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US) };
            for (DateFormat sdf: defaultFormats) {
                try {
                    if (zone != null) sdf.setTimeZone(zone);
                    return sdf.parse(s);
                } catch (Exception e) {
                }
            }
            return null;
        } else try {
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            if (zone!=null) sdf.setTimeZone(zone);
            return sdf.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
    public static Date string2Date(String s) {
        return string2DateByZone(s,null);
    }
    public static Date string2DateByZone(String s,TimeZone zone) {
        try {
            if (s == null || s.equals("-")) return null;
            Pattern p = Pattern.compile("(\\d{4})[^0-9](\\d{2})[^0-9](\\d{2})[^0-9](\\d{2})[^0-9](\\d{2})[^0-9](\\d{2})(\\.\\d+)?");
            Matcher m = p.matcher(s);
            if (m.find()) {
                String newS = String.format("%s-%s-%s %s:%s:%s", m.group(1), m.group(2), m.group(3), m.group(4), m.group(5), m.group(6));
                String ms = m.group(7);
                if (ms == null || ms.isEmpty()) ms = ".000";
                else if (ms.length() > 4) ms = ms.substring(0, 4);
                else while (ms.length() < 4) ms = ms + "0";
                if (m.end() == s.length()) return string2Date(newS + ms, "yyyy-MM-dd HH:mm:ss.SSS",zone);
                else {
                    String z = s.substring(m.end()).trim();
                    newS = newS + ms;
                    if (z.isEmpty()) return string2Date(newS, "yyyy-MM-dd HH:mm:ss.SSS",zone);
                    else {
                        if (z.toLowerCase().equals("z")) z="+0000";
                        return string2Date(newS + z, "yyyy-MM-dd HH:mm:ss.SSSz");
                    }
                }
            } else return string2Date(s,null,zone);
        } catch (Exception e) {
            throw new RuntimeException(s + " 无法格式化");
        }
    }
    public static Date getShootTimeFromFileName(String fileName) {
        if (fileName==null || fileName.isEmpty()) return null;
        try {
            Pattern p = Pattern.compile("(\\d{13}(\\d*))");
            Matcher m = p.matcher(fileName);
            if (m.find()) {
                if (m.group(2).length()>=2) return null;
                else if (m.group(2).isEmpty()) {
                    long v = Long.parseLong(m.group(1));
                    return new Date(v);
                }
            }
            p = Pattern.compile("(\\d{4})[^0-9]?(\\d{2})[^0-9]?(\\d{2})[^0-9]{0,2}(\\d{2})[^0-9]?(\\d{2})[^0-9]?(\\d{2})");
            m = p.matcher(fileName);
            if (m.find()) {
                String s=String.format("%s-%s-%s %s:%s:%s",m.group(1),m.group(2),m.group(3),m.group(4),m.group(5),m.group(6));
                return string2Date(s,"yyyy-MM-dd HH:mm:ss");
            }
            p=Pattern.compile("(\\d{4})[^0-9]?(\\d{2})[^0-9]?(\\d{2})[^0-9]{0,2}(\\d{2})[^0-9]?(\\d{2})");
            m = p.matcher(fileName);
            if (m.find()) {
                String s=String.format("%s-%s-%s %s:%s:00",m.group(1),m.group(2),m.group(3),m.group(4),m.group(5));
                return string2Date(s,"yyyy-MM-dd HH:mm:ss");
            }
            p=Pattern.compile("(\\d{4})[^0-9]?(\\d{2})[^0-9]?(\\d{2})[^0-9]{0,2}(\\d{2})");
            m = p.matcher(fileName);
            if (m.find()) {
                String s=String.format("%s-%s-%s %s:00:00",m.group(1),m.group(2),m.group(3),m.group(4));
                return string2Date(s,"yyyy-MM-dd HH:mm:ss");
            }
            p=Pattern.compile("(\\d{4})[^0-9]?(\\d{2})[^0-9]?(\\d{2})");
            m = p.matcher(fileName);
            if (m.find()) {
                String s=String.format("%s-%s-%s 12:00:00",m.group(1),m.group(2),m.group(3));
                return string2Date(s,"yyyy-MM-dd HH:mm:ss");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
