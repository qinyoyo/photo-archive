package qinyoyo.photoinfo.exiftool;

import javafx.util.Pair;
import qinyoyo.photoinfo.archive.SupportFileType;
import qinyoyo.utils.Util;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.parseDouble;

public class ExifTool {
    public  static final String RESULT = ":RESULT:";
    public  static final String ERROR = ":ERROR:";
    public  static String EXIFTOOL = "E:\\Photo\\exiftool.exe";
    public static String FFMPEG = "E:\\Photo\\ffmpeg.exe";
    public  static String FFMPEG_VERSION = null;
    public  static Double INSTALLED_VERSION;
    private static ExifTool instance = null;
    public  static ExifTool getInstance(){
        if (instance!=null) return instance;
        instance = new ExifTool();
        return instance;
    }
    private ExifTool() {
        if (INSTALLED_VERSION==null) INSTALLED_VERSION = getInstalledVersion();
        if (FFMPEG_VERSION==null) FFMPEG_VERSION = getInstalledFfmpegVersion();
    }
    public static Double getInstalledVersion(){
        while (INSTALLED_VERSION == null) {
            List<String> argsList = new ArrayList<>();
            argsList.add(EXIFTOOL);
            argsList.add("-ver");
            try {
                Pair<List<String>, List<String>> result = CommandRunner.runWithResult(false, argsList);
                if (result.getKey().size() == 0) {
                    throw new RuntimeException("Could not get version of <" + EXIFTOOL + ">.");
                }
                System.out.println("Installed <" + EXIFTOOL + "> Version: " + result.getKey());
                INSTALLED_VERSION = parseDouble(result.getKey().get(0));
            } catch (Exception e) {
                INSTALLED_VERSION = null;
                System.out.println(e.getMessage()+" Where is it installed?");
                try {
                    Scanner in = new Scanner(System.in);
                    String input = in.nextLine().trim();
                    EXIFTOOL = new File(input, "exiftool").getCanonicalPath();
                } catch (IOException ex){ Util.printStackTrace(ex);}
            }
        }
        return INSTALLED_VERSION;
    }
    public static String getInstalledFfmpegVersion() {
        while (ExifTool.FFMPEG_VERSION==null) {
            List<String> argsList = new ArrayList<>();
            argsList.add(ExifTool.FFMPEG);
            argsList.add("-version");
            try {
                Pair<List<String>, List<String>> result = CommandRunner.runWithResult(false, argsList);
                if (result.getKey().size() == 0) {
                    throw new RuntimeException("Could not get version of <" + ExifTool.FFMPEG + ">.");
                }
                Pattern p = Pattern.compile("version\\s+(\\S+)",Pattern.CASE_INSENSITIVE);
                for (String s : result.getKey()) {
                    Matcher m = p.matcher(s);
                    if (m.find()) {
                        ExifTool.FFMPEG_VERSION = m.group(1);
                        break;
                    }
                }
                if (ExifTool.FFMPEG_VERSION==null) ExifTool.FFMPEG_VERSION = result.getKey().get(0);
                System.out.println("Installed <" + ExifTool.FFMPEG + "> Version: " + ExifTool.FFMPEG_VERSION);
                return ExifTool.FFMPEG_VERSION;
            } catch (Exception e) {
                System.out.println(e.getMessage()+" Where is ffmpeg installed or 'q' for skip?");
                try {
                    Scanner in = new Scanner(System.in);
                    String input = in.nextLine().trim();
                    if (input.equals("q")) {
                        ExifTool.FFMPEG = null;
                        ExifTool.FFMPEG_VERSION="";
                        return ExifTool.FFMPEG_VERSION;
                    }
                    ExifTool.FFMPEG = new File(input, "ffmpeg").getCanonicalPath();
                } catch (IOException ex){ Util.printStackTrace(ex);}
            }
        }
        return ExifTool.FFMPEG_VERSION;
    }
    /**
     * 获得目录或文件的exif信息
     * @param dir  目录或文件
     * @param keys  需要获取的exif信息tag
     * @return 标签值，key为文件名，值为标签及值的map
     * @throws IOException io异常
     */
    public Map<String,Map<Key, Object>> query(File dir, Key ... keys) throws IOException {
        return query(dir, null, keys);
    }

    public Map<String,Map<Key, Object>> query(File dir, List<String> excludeExts, Key ... keys) throws IOException {
        // exiftool.exe -T -charset filename="" -c "%+.7f" -filename -SubSecDateTimeOriginal -DateTimeOriginal -Make -Model -LensID -GPSLongitude -GPSLatitude -GPSAltitude
        if (dir==null || !dir.exists()) return null;
        if (!dir.isDirectory() && !SupportFileType.isSupport(dir.getName())) return null;
        List<String> argsList = new ArrayList<>();
        argsList.add(EXIFTOOL);

        argsList.add("-n");
        argsList.add("-T");

        argsList.add("-charset");
        argsList.add("iptc=utf8");

        argsList.add("-charset");
        argsList.add("exif=utf8");

        argsList.add("-c");
        argsList.add("\"%+.7f\"");

        if (dir.isDirectory()) {
            argsList.add("-charset");
            argsList.add("filename=\"\"");
            argsList.add("-filename");
        }
        if (excludeExts!=null) for (String ext: excludeExts) {
            argsList.add("--ext");
            argsList.add(ext);
        }

        for (Key key : keys) {
            argsList.add(String.format("-%s", Key.getName(key)));
        }
        if (dir.isDirectory())  argsList.add(".");
        else argsList.add("\"" + dir.getName() + "\"");
        Pair<List<String>, List<String>> result = CommandRunner.runWithResult(dir.isDirectory() ? dir.toPath() : dir.getParentFile().toPath(),true, argsList);
        List<String> stdOut = result.getKey();
        List<String> stdErr = result.getValue();
        return processQueryResult(dir, stdOut, stdErr, keys);
    }
    /**
     * 修改或删除指定tags
     * @param dir 目录或一个文件
     * @param attrs 指定修改的tag，如果置为null，则删除
     * @param overwriteOriginal 是否直接覆盖源文件，不备份
     * @return 是否成功
     */
    synchronized  public boolean update(File dir, Map<Key, Object> attrs, boolean overwriteOriginal) {
        try {
            if (attrs == null || attrs.size() == 0) return false;
            String[] argsList = new String[attrs.size() + (overwriteOriginal ? 1 : 0) + 1];
            int i = 0;
            argsList[i++] = "-n";
            for (Key key : attrs.keySet()) {
                Object v = attrs.get(key);
                argsList[i++] = "-" + Key.getName(key) + "=" + (v == null ? "" : v.toString());
            }
            if (overwriteOriginal) argsList[i++] = "-overwrite_original";
            Map<String, List<String>> result = execute(dir, argsList);
            return updatesFiles(result)==1;
        } catch (Exception e){ Util.printStackTrace(e);}
        return false;
    }

    public boolean remove(File file, List<Key> tags) {
        if (tags==null || tags.size()==0) return false;
        Key [] a = tags.toArray(new Key[tags.size()]);
        return remove(file,a);
    }
    public boolean remove(File file, Key ... tags) {
        if (tags==null || tags.length==0) return false;
        if (file==null || !file.exists()) return false;
        Map<Key, Object> attrs = new HashMap<>();
        for (Key k : tags) attrs.put(k,null);
        return update(file,attrs,true);
    }

    /**
     * 执行一个exiftool命令
     * @param dir 文件或目录
     * @param options 命令参数(如果关闭pringConv，需要指定-n参数)
     * @return 执行结果
     * @throws IOException 异常
     */
    synchronized public Map<String,List<String>> execute(File dir, String ... options) throws IOException {
        List<String> argsList = new ArrayList<>();
        argsList.add(EXIFTOOL);
        argsList.add("-charset");
        argsList.add("filename=\"\"");

        for (String option : options) {
            argsList.add(option);
        }
        if (dir.isDirectory())  argsList.add(".");
        else argsList.add(dir.getName());
        Pair<List<String>, List<String>> result = CommandRunner.runWithResult(dir.isDirectory() ? dir.toPath() : dir.getParentFile().toPath(),true, argsList);
        List<String> stdOut = result.getKey();
        List<String> stdErr = result.getValue();
        return new HashMap<String,List<String>>(){{
            if (stdOut!=null && stdOut.size()>0) put(RESULT,stdOut);
            if (stdErr!=null && stdErr.size()>0) put(ERROR,stdErr);
        }};
    }

    /**
     * 获取修改完成的文件数
     * @param result 结果参数
     * @return 修改文件数
     */
    public static int updatesFiles(Map<String, List<String>> result) {
        if (result==null) return 0;
        List<String> msgList = result.get(ExifTool.RESULT);
        if (msgList == null || msgList.size() == 0) msgList = result.get(ExifTool.ERROR);
        if (msgList == null || msgList.size() == 0) return 0;
        for (String msg : msgList) {
            Pattern p = Pattern.compile("\\b(\\d+)\\b.*?\\bfiles?\\b\\s*\\bupdated\\b",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(msg);
            if (m.find()) return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    private Map<String,Map<Key, Object>> processQueryResult(File dir, List<String> stdOut, List<String> stdErr, Key ... keys) {
        if (!dir.isDirectory() && !SupportFileType.isSupport(dir.getName())) return null;
        Map<String,Map<Key, Object>> queryResult = new HashMap<>();
        if (stdErr.size() > 0) {
           throw new RuntimeException(String.join("\n", stdErr));
        }
        StringBuilder sb=new StringBuilder();
        for (String line : stdOut) {
            if (line.endsWith("\t")) line = line+"-";
            List<String> lineSeparated = Arrays.asList(line.split("\t",-1));
            if (lineSeparated.size() < keys.length + 1) {
            	sb.append(line).append("\n");
                continue;
            }
            Map<Key, Object> oneResult = new HashMap<>();
            for (int i=0;i< keys.length; i++) {
                if (dir.isDirectory() && !SupportFileType.isSupport(lineSeparated.get(0))) continue;
                String value = lineSeparated.get(i + (dir.isDirectory() ? 1 : 0)).trim();
                try {
                    if (!value.isEmpty() && !value.equals("-")) oneResult.put(keys[i], Key.parse(keys[i], value));
                } catch (Exception e) {
                    System.out.println(keys[i] + " :error data format "+ value);
                }
            }
            queryResult.put(dir.isDirectory() ? lineSeparated.get(0) : dir.getName(),oneResult);
        }
        String error=sb.toString();
        if (error!=null && !error.isEmpty()) {
            Map<Key, Object> emap = new HashMap<>();
        	emap.put(Key.DESCRIPTION, error);
        	queryResult.put(ERROR, emap);
        }
        return queryResult;
    }
}
