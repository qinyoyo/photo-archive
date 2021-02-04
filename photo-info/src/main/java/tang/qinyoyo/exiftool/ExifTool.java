package tang.qinyoyo.exiftool;

import javafx.util.Pair;

import java.io.*;
import java.util.*;

import static java.lang.Double.parseDouble;

public class ExifTool {
    public  static final String RESULT = ":RESULT:";
    public  static final String ERROR = ":ERROR:";
    public  static String EXIFTOOL = "exiftool";
    public  static Double INSTALLED_VERSION;
    private static ExifTool instance = null;
    public  static ExifTool getInstance(){
        if (instance!=null) return instance;
        instance = new ExifTool();
        return instance;
    }
    private ExifTool() {
        if (INSTALLED_VERSION==null) INSTALLED_VERSION = getInstalledVersion();
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
                } catch (IOException ex) {
                }
            }
        }
        return INSTALLED_VERSION;
    }
    /**
     * 获得目录或文件的exif信息
     * @param dir  目录或文件
     * @param keys  需要获取的exif信息tag
     * @return 标签值，key为文件名，值为标签及值的map
     * @throws IOException io异常
     */
    public Map<String,Map<String, Object>> query(File dir, Key ... keys) throws IOException {
        // exiftool.exe -T -charset filename="" -c "%+.7f" -filename -SubSecDateTimeOriginal -DateTimeOriginal -Make -Model -LensID -GPSLongitude -GPSLatitude -GPSAltitude

        List<String> argsList = new ArrayList<>();
        argsList.add(EXIFTOOL);

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

    public Map<String,List<String>> excute(File dir, String ... options) throws IOException {
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
    public boolean modifyAttributes(File dir, Map<Key, Object> attrs, boolean overwriteOriginal) {
        try {
            if (attrs == null || attrs.size() == 0) return false;
            String[] argsList = new String[attrs.size() + (overwriteOriginal ? 1 : 0)];
            int i = 0;
            for (Key key : attrs.keySet()) {
                Object v = attrs.get(key);
                argsList[i++] = "-" + Key.getName(key) + "=" + (v == null ? "" : v.toString());
            }
            if (overwriteOriginal) argsList[attrs.size()] = "-overwrite_original";
            Map<String, List<String>> result = excute(dir, argsList);
            List<String> msgList = result.get(ExifTool.RESULT);
            if (msgList == null || msgList.size() == 0) return false;
            for (String msg : msgList) {
                if (msg.contains("1") && msg.contains("file") && msg.contains("updated")) return true;
            }
        } catch (Exception e) {}
        return false;
    }
    private Map<String,Map<String, Object>> processQueryResult(File dir, List<String> stdOut, List<String> stdErr, Key ... keys) {
        Map<String,Map<String, Object>> queryResult = new HashMap<>();
        if (stdErr.size() > 0) {
           throw new RuntimeException(String.join("\n", stdErr));
        }
        StringBuilder sb=new StringBuilder();
        for (String line : stdOut) {
            if (line.endsWith("\t")) line = line+"-";
            List<String> lineSeparated = Arrays.asList(line.split("\t"));
            if (lineSeparated.size() < keys.length + 1) {
            	sb.append(line).append("\n");
                continue;
            }
            Map<String, Object> oneResult = new HashMap<>();
            for (int i=0;i< keys.length; i++) {
                String value = lineSeparated.get(i+(dir.isDirectory()?1:0)).trim();
                if (!value.isEmpty() && !value.equals("-")) oneResult.put(Key.getName(keys[i]),Key.parse(keys[i], value));
            }
            queryResult.put(dir.isDirectory() ? lineSeparated.get(0) : dir.getName(),oneResult);
        }
        String error=sb.toString();
        if (error!=null && !error.isEmpty()) {
            Map<String, Object> emap = new HashMap<>();
        	emap.put(Key.getName(Key.DESCRIPTION), Key.parse(Key.DESCRIPTION,error));
        	queryResult.put(ERROR, emap);
        }
        return queryResult;
    }

    public interface FileActionListener {
        boolean accept(File dir);
        void before(File dir);
        void after(File dir);
    }
    public static void dirAction(File dir, List<String> args, boolean recursive, FileActionListener listener) {
        if (args==null || args.isEmpty()) return;
        if (dir == null || !dir.exists()) return;
        if (listener==null || listener.accept(dir)) {
            try {
                System.out.println(dir.getCanonicalPath());
                if (listener!=null) listener.after(dir);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        if (dir.isDirectory() && recursive) {
            File[] subDirs = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().startsWith(".")) return false;
                    return pathname.isDirectory();
                }
            });
            if (subDirs!=null) {
	            for (File d : subDirs) {
	                dirAction(d, args, recursive, listener);
	            }
            }
        }
    }

}
