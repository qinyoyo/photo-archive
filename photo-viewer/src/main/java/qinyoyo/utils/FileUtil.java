package qinyoyo.utils;


import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.web.server.MimeMappings;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

public class FileUtil {
    /************************
     * 将字符串写入文件
     * @param s : 字符串
     * @param file : 文件
     * @throws IOException 出错抛出异常
     */
    static public void writeToFile(String s, File file) throws IOException {
        if (file.exists()) {
            file.delete();
        } else {
            File dir = file.getParentFile();
            if (!dir.exists()) dir.mkdirs();
        }
        PrintWriter fw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8")));
        fw.write(s);
        fw.close();
    }

    /************************
     * 将字符串添加到文件
     * @param s ： 字符
     * @param file : 文件
     * @throws IOException 出错抛出异常
     */
    static public void appendToFile(String s, File file) throws IOException {
        if (!file.exists()) {
            File dir = file.getParentFile();
            if (!dir.exists()) dir.mkdirs();
        }
        PrintWriter fw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8")));
        fw.write(s);
        fw.close();
    }


    /**
     * 下载指定的文件
     *
     * @param file        需要下载的文件
     * @param response    response
     * @param browserOpen true: 支持浏览器打开
     * @throws IOException 异常
     */
    static public void downloadFile(File file, HttpServletResponse response, boolean browserOpen) throws IOException {
        if (file == null || !file.exists())
            throw new FileNotFoundException("File not exists");
        String fileName = file.getName();
        if (browserOpen) {
            int p = fileName.lastIndexOf(".");
            String ct = null;
            if (p >= 0) {
                ct = MimeMappings.DEFAULT.get(fileName.substring(p + 1).toLowerCase());
            }
            response.setHeader("content-type", ct == null ? "application/octet-stream" : ct);
        } else response.setHeader("content-type", "application/octet-stream");
        // 下载文件能正常显示中文
        response.setHeader("Content-Disposition", (browserOpen ? "inline" : "attachment") + ";filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setHeader("Content-Length", String.valueOf(file.length()));

        byte[] buffer = new byte[1024];
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static File getWebappFile(String path) throws IOException {
        File webapp = new File(SpringContextUtil.getProjectHomeDirection(),"/webapp");
        if (Util.isEmpty(path)) return webapp;
        else {
            File file=new File(webapp, path);
            if (file.exists()) return file;
            else throw new FileNotFoundException(file.getAbsolutePath());
        }
    }
    static public String getPath(Class<?> clazz) {
        ApplicationHome home = new ApplicationHome(clazz == null ? File.class : clazz);
        File file = home.getDir();
        if (file != null) return file.getPath();
        else return null;
    }

    static public String getSource(Class<?> clazz) {
        ApplicationHome home = new ApplicationHome(clazz == null ? File.class : clazz);
        File file = home.getSource();
        if (file != null) return file.getPath();
        else return null;
    }
}
