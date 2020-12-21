package qinyoyo.exiftool;

import javafx.util.Pair;
import qinyoyo.SystemOut;

import java.io.*;
import java.util.*;

import static java.lang.Double.parseDouble;

public class ExifTool {
    public  static String EXIFTOOL = "exiftool";
    public  static Double INSTALLED_VERSION;

    private final Set<Feature> features;

    private ExifTool(Set<Feature> features) throws IOException, InterruptedException {
        this.features = features;
        if (INSTALLED_VERSION==null) INSTALLED_VERSION = getInstalledVersion();
        features.forEach(feature -> {
            if (!Feature.isCompatible(feature, INSTALLED_VERSION)) {
                throw new UnsupportedOperationException(String.format(
                        "Feature %s not supported by ExifTool version %s", feature, INSTALLED_VERSION
                ));
            }
        });
    }

    public static Double getInstalledVersion() throws IOException{
        if (INSTALLED_VERSION == null) {
            List<String> argsList = new ArrayList<>();
            argsList.add(EXIFTOOL);
            argsList.add("-ver");
            Pair<List<String>, List<String>> result = CommandRunner.runAndFinish(argsList);
            if (result.getKey().size() == 0) {
                throw new RuntimeException("Could not get version of <" + EXIFTOOL + ">. Where is it installed?");
            }
            SystemOut.println("Installed <" + EXIFTOOL + "> Version: " + result.getKey());
            INSTALLED_VERSION = parseDouble(result.getKey().get(0));
        }
        return INSTALLED_VERSION;
    }
    /**
     * 获得目录或文件的exif信息
     * @param dir  目录或文件
     * @param keys  需要获取的exif信息tag
     * @param <T>   标签类别
     * @return 标签值，key为文件名，值为标签及值的map
     * @throws IOException io异常
     */
    public <T> Map<String,Map<Key, T>> query(File dir, Key ... keys) throws IOException {
        // exiftool.exe -T -charset filename="" -c "%+.7f" -filename -SubSecDateTimeOriginal -DateTimeOriginal -Make -Model -LensID -GPSLongitude -GPSLatitude -GPSAltitude

        List<String> argsList = new ArrayList<>();
        argsList.add(EXIFTOOL);

        argsList.add("-T");

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
        ChineseFileName cc = new ChineseFileName(dir);
        if (dir.isDirectory())  argsList.add(".");
        else argsList.add(dir.getName());
        Pair<List<String>, List<String>> result = CommandRunner.runAndFinish(argsList, dir.isDirectory() ? dir.toPath() : dir.getParentFile().toPath());
        List<String> stdOut = result.getKey();
        List<String> stdErr = result.getValue();
        return processQueryResult(cc, dir, stdOut, stdErr, keys);
    }


    private <T> Map<String,Map<Key, T>> processQueryResult(ChineseFileName cc, File dir, List<String> stdOut, List<String> stdErr, Key ... keys) {
        Map<String,Map<Key, T>> queryResult = new HashMap<>();
        if (stdErr.size() > 0) {
            throw new RuntimeException(String.join("\n", stdErr));
        }

        for (String line : stdOut) {
            List<String> lineSeparated = Arrays.asList(line.split("\t"));
            if (lineSeparated.size() < keys.length + 1) {
                continue;
            }
            Map<Key, T> oneResult = new HashMap<>();
            for (int i=0;i< keys.length; i++) {
                String value = lineSeparated.get(i+(dir.isDirectory()?1:0)).trim();
                if (!value.isEmpty() && !value.equals("-")) oneResult.put(keys[i],Key.parse(keys[i], value));
            }
            queryResult.put(dir.isDirectory() ? cc.getOrignalName(lineSeparated.get(0)) : dir.getName(),oneResult);
        }
        cc.reverse();
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
                SystemOut.println(dir.getCanonicalPath());
                /* 可能会损坏文件
                List<String> argsList = new ArrayList<>();
                argsList.add(EXIFTOOL);
                argsList.addAll(args);
                if (dir.isDirectory()) argsList.add(".");
                else argsList.add(dir.getName());
                Pair<List<String>, List<String>> result = CommandRunner.runAndFinish(argsList, dir.isDirectory() ? dir.toPath() : dir.getParentFile().toPath());
                List<String> stdOut = result.getKey();
                List<String> stdErr = result.getValue();
                if (stdOut != null && !stdOut.isEmpty()) {
                    for (String s : stdOut) SystemOut.println(s);
                }
                if (stdErr != null && !stdErr.isEmpty()) {
                    for (String s : stdErr) SystemOut.println(s);
                }
                */
                if (listener!=null) listener.after(dir);
            } catch (Exception e) {
                SystemOut.println(e.getMessage());
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
    public static class Builder {

        private Set<Feature> features = new HashSet<>();

        public Builder features(Set<Feature> features) {
            this.features = features;
            return this;
        }
        public ExifTool build() throws IOException, InterruptedException {
            return new ExifTool(features);
        }
    }

}
