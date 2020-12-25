package tang.qinyoyo.exiftool;

import javafx.util.Pair;

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
            System.out.println("Installed <" + EXIFTOOL + "> Version: " + result.getKey());
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
        else argsList.add(dir.getName());
        Pair<List<String>, List<String>> result = CommandRunner.runAndFinish(argsList, dir.isDirectory() ? dir.toPath() : dir.getParentFile().toPath());
        List<String> stdOut = result.getKey();
        List<String> stdErr = result.getValue();
        return processQueryResult(dir, stdOut, stdErr, keys);
    }


    private <T> Map<String,Map<Key, T>> processQueryResult(File dir, List<String> stdOut, List<String> stdErr, Key ... keys) {
        Map<String,Map<Key, T>> queryResult = new HashMap<>();
        if (stdErr.size() > 0) {
           throw new RuntimeException(String.join("\n", stdErr));
        }
        StringBuilder sb=new StringBuilder();
        for (String line : stdOut) {
            List<String> lineSeparated = Arrays.asList(line.split("\t"));
            if (lineSeparated.size() < keys.length + 1) {
            	sb.append(line).append("\n");
                continue;
            }
            Map<Key, T> oneResult = new HashMap<>();
            for (int i=0;i< keys.length; i++) {
                String value = lineSeparated.get(i+(dir.isDirectory()?1:0)).trim();
                if (!value.isEmpty() && !value.equals("-")) oneResult.put(keys[i],Key.parse(keys[i], value));
            }
            queryResult.put(dir.isDirectory() ? lineSeparated.get(0) : dir.getName(),oneResult);
        }
        String error=sb.toString();
        if (error!=null && !error.isEmpty()) {
        	Map<Key, T> emap = new HashMap<>();
        	emap.put(Key.DESCRIPTION, Key.parse(Key.DESCRIPTION,error));
        	queryResult.put(":ERROR:", emap);
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
