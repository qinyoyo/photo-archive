package tang.qinyoyo.exiftool;

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
        return run(args, FileSystems.getDefault().getPath("."),null);
    }

    public static Process run(List<String> args, Path workingDir,File redirectOutput) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(args);
        processBuilder.directory(workingDir.toFile());
        processBuilder.redirectErrorStream(true);
        if (redirectOutput!=null) processBuilder.redirectOutput(redirectOutput);
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
    	File redirectOutput = File.createTempFile("_p_a_", ".tmp");    	
        Process process = run(args, workingDir, redirectOutput);
        List<String> stdout=new ArrayList<>();
        List<String> stderr=new ArrayList<>();
        try {
        	if (redirectOutput==null) {
        		final InputStream is1 = process.getInputStream();
        		new Thread(() -> {
                    bufferProcess(is1,stdout);
                }).start();
        	}
            
            final InputStream is2 = process.getErrorStream();
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
        if (redirectOutput!=null && redirectOutput.exists()) {
        	try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(redirectOutput),"UTF8"));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line != null) stdout.add(line);
                }
                br.close();
                redirectOutput.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        return new Pair<>(stdout, stderr);
    }
    public static void shutdown(int delay) {
        List<String> cmd = new ArrayList<>();
        cmd.add("shutdown");
        cmd.add("-s");
        cmd.add("-f");
        cmd.add("-t");
        cmd.add(String.valueOf(delay));
        try {
            CommandRunner.run(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
