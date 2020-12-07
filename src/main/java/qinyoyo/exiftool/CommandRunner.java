package qinyoyo.exiftool;

import javafx.util.Pair;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 执行操作系统命令
 */
public final class CommandRunner {
    public static Process run(List<String> args) throws IOException {
        return run(args, FileSystems.getDefault().getPath("."));
    }

    public static Process run(List<String> args, Path workingDir) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(args);
        processBuilder.directory(workingDir.toFile());
        return processBuilder.start();
    }


    public static Pair<List<String>, List<String>> runAndFinish(List<String> args) throws IOException {
        return runAndFinish(args, FileSystems.getDefault().getPath("."));
    }

    private static void bufferProcess(InputStream ins,List<String> out) {
        try {
            BufferedReader br1 = new BufferedReader(new InputStreamReader(ins,"GBK"));
            String line;
            while ((line = br1.readLine()) != null) {
                if (line != null) out.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            try {
                ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static Pair<List<String>, List<String>> runAndFinish(List<String> args, Path workingDir) throws IOException {
        Process process = run(args, workingDir);
        List<String> stdout=new ArrayList<>();
        List<String> stderr=new ArrayList<>();
        try {
            final InputStream is1 = process.getInputStream();
            final InputStream is2 = process.getErrorStream();
            new Thread(() -> {
                bufferProcess(is1,stdout);
            }).start();

            new Thread(() -> {
                bufferProcess(is2,stderr);
            }).start();
            process.waitFor();
            process.destroy();
        } catch (Exception e) {
            try{
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
            }
            catch(Exception ee){}
        }
        return new Pair<>(stdout, stderr);
    }
}
