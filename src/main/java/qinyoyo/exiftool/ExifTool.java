package qinyoyo.exiftool;

import javafx.util.Pair;
import qinyoyo.SystemOut;

import java.io.*;
import java.util.*;

import static java.lang.Double.parseDouble;

public class ExifTool {
    public  static String EXIFTOOL = "D:\\Download\\exiftool.exe";
    public  static Double INSTALLED_VERSION;

    private final Set<Feature> features;

    private Process longRunningProcess;

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

    public void startLongRunningProcess() throws IOException, InterruptedException {
        if (features.contains(Feature.STAY_OPEN)) {
            List<String> argsList = new ArrayList<>();
            argsList.add(EXIFTOOL);
            argsList.add(Feature.getFlag(Feature.STAY_OPEN));
            argsList.add("True");
            argsList.add("-@");
            argsList.add("-");
            longRunningProcess = CommandRunner.run(argsList);
        }
    }

    public void cancelLongRunningProcess() throws InterruptedException {
        longRunningProcess.destroyForcibly().waitFor();
        assert !longRunningProcess.isAlive() : "Long running process is still alive.";
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

    public <T> Map<Key, T> query(File file, Key ... keys) throws IOException, InterruptedException {
        return (longRunningProcess != null && longRunningProcess.isAlive())
                ? queryLongRunning(file, keys)
                : queryShortLived(file, keys);
    }

    public <T> Map<String,Map<Key, T>> batchQuery(File dir, Key ... keys) throws IOException, InterruptedException {
        // exiftool.exe -T -charset filename="" -c "%+.7f" -filename -SubSecDateTimeOriginal -DateTimeOriginal -Make -Model -LensID -GPSLongitude -GPSLatitude -GPSAltitude

        List<String> argsList = new ArrayList<>();
        argsList.add(EXIFTOOL);
        argsList.add("-T");
        if (dir.isDirectory()) {
	        argsList.add("-charset");
	        argsList.add("filename=\"\"");
        }
        argsList.add("-c");
        argsList.add("\"%+.7f\"");
        if (dir.isDirectory()) argsList.add("-filename");
        for (Key key : keys) {
            argsList.add(String.format("-%s", Key.getName(key)));
        }

        argsList.add(dir.getAbsolutePath());
        Pair<List<String>, List<String>> result = CommandRunner.runAndFinish(argsList);
        List<String> stdOut = result.getKey();
        List<String> stdErr = result.getValue();

        return processBatchQueryResult(dir, stdOut, stdErr, keys);

    }


    private <T> Map<Key, T> queryLongRunning(File file, Key... keys) throws IOException, InterruptedException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(longRunningProcess.getOutputStream());
        List<String> argsList = new ArrayList<>();
        argsList.add("-S");
        for (Key key : keys) {
            argsList.add(String.format("-%s", Key.getName(key)));
        }
        argsList.add(file.getAbsolutePath());
        argsList.add("-execute\n");
        outputStreamWriter.write(String.join("\n", argsList));
        outputStreamWriter.flush();

        String line;
        BufferedReader stdOutStreamReader = new BufferedReader(new InputStreamReader(longRunningProcess.getInputStream()));
        List<String> stdOut = new ArrayList<>();
        while ((line = stdOutStreamReader.readLine()) != null) {
            if (line.equals("{ready}")) {
                break;
            }
            stdOut.add(line);
        }

        BufferedReader stdErrStreamReader = new BufferedReader(new InputStreamReader(longRunningProcess.getErrorStream()));
        List<String> stdErr = new ArrayList<>();
        while (stdErrStreamReader.ready() && (line = stdErrStreamReader.readLine()) != null) {
            stdErr.add(line);
        }

        return processQueryResult(stdOut, stdErr);
    }

    private <T> Map<Key, T> queryShortLived(File file, Key ... keys) throws IOException, InterruptedException {
        List<String> argsList = new ArrayList<>();
        argsList.add(EXIFTOOL);
        argsList.add("-S");
        for (Key key : keys) {
            argsList.add(String.format("-%s", Key.getName(key)));
        }
        argsList.add(file.getAbsolutePath());
        Pair<List<String>, List<String>> result = CommandRunner.runAndFinish(argsList);
        List<String> stdOut = result.getKey();
        List<String> stdErr = result.getValue();

        return processQueryResult(stdOut, stdErr);
    }

    private <T> Map<Key, T> processQueryResult(List<String> stdOut, List<String> stdErr) {
        Map<Key, T> queryResult = new HashMap<>();
        if (stdErr.size() > 0) {
            throw new RuntimeException(String.join("\n", stdErr));
        }

        for (String line : stdOut) {
            List<String> lineSeparated = Arrays.asList(line.split(":"));
            if (lineSeparated.size() < 2) {
                continue;
            }
            String name = lineSeparated.get(0).trim();
            String value = String.join(":", lineSeparated.subList(1, lineSeparated.size())).trim();
            Optional<Key> maybeKey = Key.findKeyWithName(name);
            maybeKey.ifPresent(key -> queryResult.put(key, Key.parse(key, value)));
        }
        return queryResult;
    }

    private <T> Map<String,Map<Key, T>> processBatchQueryResult(File dir, List<String> stdOut, List<String> stdErr, Key ... keys) {
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
            queryResult.put(dir.isDirectory() ? lineSeparated.get(0).trim() : dir.getName(),oneResult);
        }
        return queryResult;
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
