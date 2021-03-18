package qinyoyo.utils;


import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import qinyoyo.photoinfo.ArchiveUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtil {
    public static String getFromFile(File file) {
        return getFromFile(file, "GBK");
    }

    public static String getFromFile(File file, String charset) {
        FileInputStream s = null;
        try {
            s = new FileInputStream(file);
            return getFromStream(s,charset);
        } catch (IOException e) {
            Util.printStackTrace(e);
            return null;
        }
    }

    public static String getFromStream(InputStream s, String charset) {
        InputStreamReader r = null;
        BufferedReader in = null;
        try {
            r = new InputStreamReader(s, charset);
            in = new BufferedReader(r);
            StringBuilder sb = new StringBuilder();
            String str;
            boolean firstLine = true;
            while ((str = in.readLine()) != null) {
                if (firstLine)
                    firstLine = false;
                else
                    sb.append("\r\n");
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            Util.printStackTrace(e);
            return null;
        } finally {
            try {
                if (in != null) in.close();
                if (r != null) r.close();
                if (s != null) s.close();
            } catch (IOException e){ Util.printStackTrace(e);}
        }
    }
    public static String getFromResource(String url) {
        try {
            ClassLoader loader = FileUtil.class.getClassLoader();
            if (loader==null) return getFromResource1(url);
            InputStream stream = loader.getResourceAsStream(url);
            if (stream==null) return getFromResource1(url);
            return getFromStream(stream, "UTF-8");
        } catch (Exception e) {
            Util.printStackTrace(e);
            return null;
        }
    }
    private static String getFromResource1(String url) {
        try {
            ResourceLoader loader = new DefaultResourceLoader();
            if (loader==null) return null;
            Resource resource = loader.getResource(url);
            if (resource==null) return null;
            File file = resource.getFile();
            if (file==null) return null;
            return getFromFile(file,"UTF-8");
        } catch (Exception e) {
            Util.printStackTrace(e);
            return null;
        }
    }
    public static void writeToFile(File file, String string) {
        writeToFile(file, string, "GBK");
    }

    public static void writeToFile(File file, String string, String charset) {
        FileOutputStream s = null;
        OutputStreamWriter w = null;
        PrintWriter pw = null;
        try {
            s = new FileOutputStream(file);
            w = new OutputStreamWriter(s, charset);
            pw = new PrintWriter(w);
            pw.write(string);
            pw.flush();
            pw.close();
            w.close();
            s.close();
        } catch (Exception e) {
            Util.printStackTrace(e);
        } finally {
            try {
                if (pw != null) pw.close();
                if (w != null) w.close();
                if (s != null) s.close();
            } catch (IOException e){ Util.printStackTrace(e);}
        }
    }

    public static void appendToFile(File file, String string) {
        appendToFile(file, string, "GBK");
    }

    public static void appendToFile(File file, String string, String charset) {
        try {
            String s = getFromFile(file, charset);
            if (s == null || s.isEmpty()) s = string;
            else s = s.trim() + "\r\n" + string;
            writeToFile(file, s, charset);
        } catch (Exception e) {
            Util.printStackTrace(e);
        }
    }

    static public String getPath(Class<?> clazz) {
        ApplicationHome home = new ApplicationHome(clazz == null ? File.class : clazz);
        File file = home.getDir();
        if (file != null) return file.getPath();
        else return null;
    }

    public static void removeEmptyFolder(File dir) {
        if (!dir.isDirectory()) return;
        File[] subDirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().equals(".") || pathname.getName().equals("..")) return false;
                else return pathname.isDirectory();
            }
        });

        if (subDirs != null && subDirs.length > 0) {
            for (File d : subDirs) {
                removeEmptyFolder(d);
            }
        }
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().equals(".") || pathname.getName().equals("..")) return false;
                else return true;
            }
        });
        if (files != null && files.length == 0) dir.delete();
    }
    static public String nameUseExt(String newExt, String name) {
        int pos = name.lastIndexOf(".");
        if (pos<0) return name+newExt;
        else return name.substring(0,pos) + newExt;
    }
    public static void removeFilesInDir(File dir, boolean removeDir) {
        File[] files = dir.listFiles(f->f.isFile());
        if (files!=null && files.length>0) for (File f : files) f.delete();
        if (removeDir) dir.delete();
    }
    public static void moveDirectory(Path source, Path target, CopyOption... options) throws IOException {
        source = source.toAbsolutePath();
        final Path targetPath = target.toAbsolutePath();
        final String src = source.toString(),tar = targetPath.toString();
        try {
            Files.walkFileTree(source,new SimpleFileVisitor<Path>() {
                String move2;
                String getTargetPath(Path dir) {
                    String dirPath = dir.toAbsolutePath().toString();
                    if (dirPath.length()>src.length()) {
                        return tar + File.separator + dirPath.substring(src.length() + 1);
                    } else return tar;
                }
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
                {
                    move2 = getTargetPath(dir);
                    Files.createDirectories(Paths.get(move2));
                    System.out.println("移动文件 "+dir.toString() + " 到 "+ move2);
                    return super.preVisitDirectory(dir,attrs);
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.move(file,Paths.get(move2,file.getFileName().toString()),options);
                    return super.visitFile(file,attrs);
                }
            });
            System.out.println("删除空目录");
            removeEmptyFolder(source.toFile());
            removeEmptyFolder(target.toFile());
        } catch (IOException e) {
            Util.printStackTrace(e);
        }
    }
    public static String getCurrentPath() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException e) {
            return ".";
        }
    }
}
