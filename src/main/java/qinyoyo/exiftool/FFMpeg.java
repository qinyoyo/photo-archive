package qinyoyo.exiftool;

import javafx.util.Pair;
import qinyoyo.SystemOut;
import qinyoyo.Utils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFMpeg {
    public static String FFMPEG_VERSION;
    public static String FFMPEG = "D:\\Software\\Green Software Files\\ffmpeg\\bin\\ffmpeg.exe";
    public static String getFfmpegVersion() throws Exception{
        if (FFMPEG_VERSION == null) {
            List<String> argsList = new ArrayList<>();
            argsList.add(FFMPEG);
            argsList.add("-version");
            Pair<List<String>, List<String>> result = CommandRunner.runAndFinish(argsList);
            List<String> stdOut = result.getKey();
            List<String> stdErr = result.getValue();
            stdOut.addAll(stdErr);
            if (stdErr != null) {
                Pattern p = Pattern.compile("ffmpeg\\s+version\\s+(\\S+)",Pattern.CASE_INSENSITIVE);
                for (String s : stdOut) {
                    Matcher m = p.matcher(s);
                    if (m.find()) {
                        SystemOut.println("Installed <" + FFMPEG + "> Version: " + m.group(1));
                        FFMPEG_VERSION = m.group(1);
                        return m.group(1);
                    }
                }
            }
            throw new RuntimeException("Could not get version of <" + FFMPEG + ">. Where is it installed?");
        } else return FFMPEG_VERSION;
    }
    public static Date getMediaCreateTime(File mf) {
        try {
            if (mf != null && mf.exists() && !mf.isDirectory()) {
                // ffmpeg -i "filepath" -f null -
                List<String> argsList = new ArrayList<>();
                argsList.add(FFMPEG);
                argsList.add("-i");
                argsList.add("\"" + mf.getAbsolutePath() + "\"");
                argsList.add("-f");
                argsList.add("null");
                argsList.add("-");
                Pair<List<String>, List<String>> result = CommandRunner.runAndFinish(argsList);
                List<String> stdOut = result.getKey();
                List<String> stdErr = result.getValue();
                stdOut.addAll(stdErr);
                if (stdOut.size()>0) {
                    Pattern p = Pattern.compile("creation_time\\s*\\:\\s*(\\d{4}[^0-9]\\d{2}[^0-9]\\d{2}[^0-9]\\d{2}[^0-9]\\d{2}[^0-9]\\d{2}(\\.\\d+)?\\S*)",Pattern.CASE_INSENSITIVE);
                    for (String s : stdOut) {
                        Matcher m = p.matcher(s);
                        if (m.find()) return Utils.string2Date(m.group(1));
                    }
                }
            }
        } catch (Exception e) {}
        return null;
    }
}
