package tang.qinyoyo;

import tang.qinyoyo.archive.PhotoInfo;
import tang.qinyoyo.exiftool.ExifTool;
import tang.qinyoyo.exiftool.Key;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArchiveUtils {
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        else if (obj1 != null && obj2 != null) return obj1.equals(obj2);
        else return false;
    }

    public static String getFromFile(File file) {
        return getFromFile(file, "GBK");
    }

    public static String getFromFile(File file, String charset) {
        try {
            FileInputStream s = new FileInputStream(file);
            InputStreamReader r = new InputStreamReader(s, charset);
            BufferedReader in = new BufferedReader(r);
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
            return null;
        }
    }

    public static void writeToFile(File file, String string) {
        writeToFile(file, string, "GBK");
    }

    public static void writeToFile(File file, String string, String charset) {
        try {
            FileOutputStream s = new FileOutputStream(file);
            OutputStreamWriter w = new OutputStreamWriter(s, charset);
            PrintWriter pw = new PrintWriter(w);
            pw.write(string);
            pw.flush();
            pw.close();
            w.close();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public static int contentCompare(String path1, String path2) {
        if (path1 == null && path2 == null) return 0;
        else if (path1 == null) return -1;
        else if (path2 == null) return 1;
        else {
            File file1 = new File(path1), file2 = new File(path2);
            if (!file1.exists() && !file2.exists()) return 0;
            else if (!file1.exists()) return -1;
            else if (!file2.exists()) return 1;
            else if (file1.length() < file2.length()) return -1;
            else if (file1.length() > file2.length()) return 1;
            else {
                FileInputStream in1 = null, in2 = null;
                try {
                    in1 = new FileInputStream(file1);
                    in2 = new FileInputStream(file2);
                    byte[] buf1 = new byte[10240], buf2 = new byte[10240];
                    long passLength = Math.max(0, file1.length() / 10 - 10240);
                    int len1 = in1.read(buf1), len2 = in2.read(buf2);
                    do {
                        if (len1 < len2) return -1;
                        else if (len1 > len2) return 1;
                        else if (!Arrays.equals(buf1, buf2)) return -1;
                        if (passLength > 0) {
                            in1.skip(passLength);
                            in2.skip(passLength);
                        }
                        len1 = in1.read(buf1);
                        len2 = in2.read(buf2);
                    } while (len1 > 0);
                    return 0;
                } catch (Exception e) {
                    return -1;
                } finally {
                    if (in1 != null)
                        try {
                            in1.close();
                        } catch (IOException e) {
                        }
                    if (in2 != null)
                        try {
                            in2.close();
                        } catch (IOException e) {
                        }
                }
            }
        }
    }

    public static File bakNameOf(File file) {
        if (!file.exists()) return file;
        String path = file.getAbsolutePath();
        int pos = path.lastIndexOf(".");
        String ext = (pos >= 0 ? path.substring(pos) : "");
        String name = (pos >= 0 ? path.substring(0, pos) : path);
        Pattern p = Pattern.compile("_(\\d+)$");
        Matcher m = p.matcher(name);
        int index = 0;
        if (m.find()) {
            name = name.substring(0, m.start());
            index = Integer.parseInt(m.group(1));
        }
        index++;
        File f = new File(name + "_" + index + ext);
        while (f.exists()) {
            index++;
            f = new File(name + "_" + index + ext);
        }
        return f;
    }

    public static String newFile(String root, PhotoInfo p) {
        try {
            String sub = p.getSubFolder();
            if (sub == null || sub.isEmpty() || sub.equals(".")) {
                return new File(root, p.getFileName()).getCanonicalPath();
            } else
                return new File(root, sub.replace("\\", " ") + p.getFileName()).getCanonicalPath();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> void removeAll(List<T> all, List<T> rm) {
        Iterator iter = all.iterator();
        while (iter.hasNext()) {
            if (rm.contains(iter.next())) {
                iter.remove();
            }
        }
        System.out.println("剩余照片数量 : " + all.size());
    }

    public static boolean isInWebFolder(String folder) {
        if (folder != null && !folder.isEmpty()) {
            String[] dd = folder.split("\\\\|/");
            for (String d : dd) if (d.endsWith(".web")) return true;
        }
        return false;
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

    public static boolean hasChinese(String value) {
        if (value == null) return false;
        String regex = "[\u4e00-\u9fa5]";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(value);
        return match.find();
    }
    public static boolean startWithLetter(String value) {
        if (value == null || value.isEmpty()) return false;
        char ch = value.charAt(0);
        return (ch>='0' && ch<='9') || (ch>='a' && ch<='z') || (ch>='A' && ch<='Z');
    }
    public static String join(String separator,String ... strings) {
        StringBuilder sb=new StringBuilder();
        boolean first = true;
        for (String s : strings) {
            if (s!=null && !s.isEmpty()) {
                if (first) {
                    sb.append(s);
                    first = false;
                } else if (separator!=null && !separator.isEmpty()) sb.append(separator).append(s);
                else sb.append(startWithLetter(s) ? " ":"").append(s);
            }
        }
        return sb.toString();
    }
    public static void removeFilesInDir(File dir, boolean removeDir) {
        File[] files = dir.listFiles(f->f.isFile());
        if (files!=null && files.length>0) for (File f : files) f.delete();
        if (removeDir) dir.delete();
    }
    public static void writeAddress(List<PhotoInfo> list,String rootPath) {
        if (list!=null && list.size()>0) {
            list.sort((a, b) -> {
                int r = a.getSubFolder().compareTo(b.getSubFolder());
                return r == 0 ? a.compareTo(b) : r;
            });
            List<String> pathList = new ArrayList<>();
            list.stream().reduce(pathList,(acc,p)->{
                    if (!acc.contains(p.getSubFolder())) acc.add(p.getSubFolder());
                    return acc;
                },(acc,p)->null);
            for (String folder : pathList) {
                List<PhotoInfo> subList = list.stream().filter(p->folder.equals(p.getSubFolder())).collect(Collectors.toList());
                File xmpDir = new File(rootPath,folder+File.separator+"_g_s_t_");
                xmpDir.mkdirs();
                removeFilesInDir(xmpDir, false);
                for (PhotoInfo p: subList) {
                    File xmpFile = new File(xmpDir, nameUseExt(".xmp", p.getFileName()));
                    String xmlContent = p.xmlString(Key.COUNTRY,Key.STATE,Key.CITY,Key.LOCATION,Key.SUBJECT_CODE);
                    if (xmlContent!=null && !xmlContent.isEmpty()) writeToFile(xmpFile, xmlContent,"UTF-8");
                }
                try {
                    Map<String, List<String>> result = ExifTool.getInstance().excute(xmpDir.getParentFile(), "-overwrite_original",
                            "-charset", "IPTC=UTF8", "-charset", "EXIF=UTF8",
                            "-tagsfromfile", "_g_s_t_" + File.separator + "%f.xmp");
                    List<String> error = result.get(ExifTool.ERROR);
                    if (error!=null && error.size()>0) for (String err : error) if (!error.contains("Error opening file")) System.out.println(err);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                removeFilesInDir(xmpDir, true);
            }
        }
    }
}
