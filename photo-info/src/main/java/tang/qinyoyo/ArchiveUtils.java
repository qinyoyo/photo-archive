package tang.qinyoyo;

import tang.qinyoyo.archive.*;
import tang.qinyoyo.exiftool.Key;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchiveUtils {
    public static final Key[] NEED_KEYS = new Key[]{
            Key.DATETIMEORIGINAL,Key.SUB_SEC_TIME_ORIGINAL,Key.CREATEDATE,Key.SUB_SEC_TIME_CREATE,
            Key.MODEL, Key.LENS_ID, Key.ORIENTATION, Key.IMAGE_WIDTH, Key.IMAGE_HEIGHT,
            Key.DOCUMENT_ID, Key.IPTCDigest,
            Key.GPS_LONGITUDE, Key.GPS_LATITUDE, Key.GPS_ALTITUDE,
            Key.MIME_TYPE, Key.ARTIST, Key.HEADLINE,Key.DESCRIPTION,Key.RATING,Key.SCENE,
            Key.COUNTRY,Key.STATE,Key.CITY,Key.LOCATION,Key.SUBJECT_CODE};
    public static final Key[] MODIFIABLE_KEYS = new Key[]{
            Key.SUBJECT_CODE,
            Key.DATETIMEORIGINAL,Key.SUB_SEC_TIME_ORIGINAL,Key.CREATEDATE,
            Key.MODEL, Key.LENS_ID,
            Key.ARTIST, Key.HEADLINE,Key.DESCRIPTION,Key.SCENE,
            Key.COUNTRY,Key.STATE,Key.CITY,Key.LOCATION,
            Key.GPS_LONGITUDE, Key.GPS_LATITUDE, Key.GPS_ALTITUDE,
    };
    public static final String no_shottime_log = ".no_shottime.log";
    public static final String manual_other_bat = ".manual_other.bat";
    public static final String same_photo_log = ".same_photo.log";
    public static final String manual_rm_bat = ".manual_rm.bat";
    public static final String ARCHIVE_FILE = ".archive_info.dat";
    public static final String manual_archive_bat = ".manual_archive.bat";
    public static final String folder_info_dat = ".folder_info.dat";
    public static final String folder_info_lost_log = ".folder_info_lost.log";
    public static final String DELETED_FILES = ".deleted.dat";
    public static String FFMPEG = "ffmpeg";
    public static String FFMPEG_VERSION = null;
    public static String VIDEO_CAPTURE_AT = "00:00:01";
    public static String THUMB = ".thumb";
    public static String DELETED = ".deleted";
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        else if (obj1 != null && obj2 != null) return obj1.equals(obj2);
        else return false;
    }

    public static String getFromFile(File file) {
        return getFromFile(file, "GBK");
    }

    public static String getFromFile(File file, String charset) {
        FileInputStream s = null;
        InputStreamReader r = null;
        BufferedReader in = null;
        try {
            s = new FileInputStream(file);
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
            return null;
        } finally {
            try {
                if (in != null) in.close();
                if (r != null) r.close();
                if (s != null) s.close();
            } catch (IOException e) {}
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
            e.printStackTrace();
        } finally {
            try {
                if (pw != null) pw.close();
                if (w != null) w.close();
                if (s != null) s.close();
            } catch (IOException e) {}
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
    public static void saveObj(File file, Object object) {
        FileOutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (objectOutputStream!=null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                }
            }
            if (outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
    public static Object readObj(File file) {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            objectInputStream = new ObjectInputStream(fileInputStream);
            return objectInputStream.readObject();
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            if (objectInputStream != null)
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
        }
        return null;
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

    /*
    // 在子目录下生成xml文件
    private static void writeXmpExif(PhotoInfo p, String rootPath, List<Key> keys) {
        File xmpDir = new File(rootPath,p.getSubFolder()+File.separator+"_g_s_t_");
        xmpDir.mkdirs();
        File xmpFile = new File(xmpDir, nameUseExt(".xmp", p.getFileName()));
        String xmlContent = p.xmlString(keys);
        if (xmlContent!=null && !xmlContent.isEmpty()) writeToFile(xmpFile, xmlContent,"UTF-8");
    }

    // 根据子目录下的xml文件来更新exif
    private static void updatePathExif(String path,String rootPath) {
        File xmpDir = new File(rootPath,path+File.separator+"_g_s_t_");
        if (!xmpDir.exists()) return;
        File photoDir = new File(rootPath,path);
        try {
            Map<String, List<String>> result = ExifTool.getInstance().excute(photoDir, "-overwrite_original", "-m",
                    "-charset", "IPTC=UTF8", "-charset", "EXIF=UTF8",
                    "-tagsfromfile", "_g_s_t_" + File.separator + "%f.xmp");
            List<String> error = result.get(ExifTool.ERROR);
            if (error != null && error.size() > 0) {
                for (String err : error)
                    if (err.indexOf("Error opening file") < 0)
                        System.out.println(err);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        removeFilesInDir(xmpDir, true);
    }
    */
    public static void updateExif(List<PhotoInfo> list,ArchiveInfo archiveInfo, List<Key> keys) {
        List<Modification> modifications = new ArrayList<>();
        list.stream().reduce(modifications,(acc,pi)->{
                acc.add(new Modification(pi,keys));
                return acc;
        },(acc,pi)->null);
        Modification.execute(modifications,archiveInfo);
        /*
        if (list!=null && list.size()>0) {
            nameSort(list);
            String path=list.get(0).getSubFolder();

            int subList = 0;
            for (PhotoInfo pi : list) {
                if (pi.getSubFolder().equals(path)) {
                    writeXmpExif(pi,rootPath,keys);
                    subList++;
                    continue;
                } else {
                    if (subList>0) updatePathExif(path,rootPath);
                    subList=1;
                    path = pi.getSubFolder();
                    writeXmpExif(pi,rootPath,keys);
                }
            }
            if (subList>0) updatePathExif(path,rootPath);
        }

         */
    }
    public static void writeAddress(List<PhotoInfo> list,ArchiveInfo archiveInfo) {
        updateExif(list, archiveInfo, new ArrayList<Key>() {{
            add(Key.COUNTRY);
            add(Key.STATE);
            add(Key.CITY);
            add(Key.LOCATION);
            add(Key.SUBJECT_CODE);
        }});
    }
    public static List<Key> differentOf(PhotoInfo p1, PhotoInfo p2) {
        List<Key> diff = new ArrayList<>();
        if(!equals(p1.getOrientation(),p2.getOrientation())) diff.add(Key.ORIENTATION);
        if(!equals(p1.getRating(),p2.getRating())) diff.add(Key.RATING);
        if(!equals(p1.getCountry(),p2.getCountry())) diff.add(Key.COUNTRY);
        if(!equals(p1.getProvince(),p2.getProvince())) diff.add(Key.STATE);
        if(!equals(p1.getCity(),p2.getCity())) diff.add(Key.CITY);
        if(!equals(p1.getLocation(),p2.getLocation())) diff.add(Key.LOCATION);
        if(!equals(p1.getSubjectCode(),p2.getSubjectCode())) diff.add(Key.SUBJECT_CODE);
        if(!equals(p1.getShootTime(),p2.getShootTime())) diff.add(Key.DATETIMEORIGINAL);
        if(!equals(p1.getCreateTime(),p2.getCreateTime())) diff.add(Key.CREATEDATE);
        if(!equals(p1.getDocumentId(),p2.getDocumentId())) diff.add(Key.DOCUMENT_ID);
        if(!equals(p1.getDigest(),p2.getDigest())) diff.add(Key.IPTCDigest);
        if(!equals(p1.getModel(),p2.getModel())) diff.add(Key.MODEL);
        if(!equals(p1.getLens(),p2.getLens())) diff.add(Key.LENS_ID);
        if(!equals(p1.getLongitude(),p2.getLongitude())) diff.add(Key.GPS_LONGITUDE);
        if(!equals(p1.getLatitude(),p2.getLatitude())) diff.add(Key.GPS_LATITUDE);
        if(!equals(p1.getAltitude(),p2.getAltitude())) diff.add(Key.GPS_ALTITUDE);
        if(!equals(p1.getArtist(),p2.getArtist())) diff.add(Key.ARTIST);
        if(!equals(p1.getHeadline(),p2.getHeadline())) diff.add(Key.HEADLINE);
        if(!equals(p1.getSubTitle(),p2.getSubTitle())) diff.add(Key.DESCRIPTION);
        if(!equals(p1.getScene(),p2.getScene())) diff.add(Key.SCENE);
        return diff;
    }
    private static void deleteFile(PhotoInfo pi,String rootPath) {
        try {
            File targetDir = new File(rootPath + File.separator + DELETED + File.separator, pi.getSubFolder());
            targetDir.mkdirs();
            File target = new File(targetDir, pi.getFileName());
            File source = new File(pi.fullPath(rootPath));
            if (source.exists())
                Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File sourceThumb = new File(pi.fullThumbPath(rootPath));
            if (sourceThumb.exists()) {
                targetDir = new File(rootPath + File.separator + DELETED + File.separator + THUMB, pi.getSubFolder());
                targetDir.mkdirs();
                target = new File(targetDir, pi.getFileName());
                Files.move(sourceThumb.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {}

    }
    private static int sync2List(List<PhotoInfo> list1, List<PhotoInfo> list2, String rootPath) {
        Iterator<PhotoInfo> iter = list1.iterator();
        int index = 0;
        int removed = 0;
        while (iter.hasNext()) {
            PhotoInfo pi = iter.next();
            for (int i=index;i<list2.size();i++) {
                PhotoInfo p = list2.get(i);
                index = i;
                int pc = p.getSubFolder().compareTo(pi.getSubFolder());
                if ( pc < 0) continue;
                else if (pc > 0) {
                    if (rootPath!=null) deleteFile(pi,rootPath);
                    iter.remove();
                    removed++;
                    break;
                }
                else {
                    int nc = p.getFileName().compareTo(pi.getFileName());
                    if (nc < 0) continue;
                    else if (nc==0) {
                        index++;
                        break;
                    }
                    else {
                        if (rootPath!=null) deleteFile(pi,rootPath);
                        iter.remove();
                        removed++;
                        break;
                    }
                }
            }
        }
        return removed;
    }
    /*
    同步 A，B服务器数据：
    A修改后，将 A的 .archived.dat 更名为 .archived.dat.sync 拷贝到 B, 重启B的服务即可
     */
    public static void syncExifAttributes(ArchiveInfo archiveInfo) {
        String rootPath = archiveInfo.getPath();
        List<PhotoInfo> infos = archiveInfo.getInfos();
        Iterator<PhotoInfo> iter = infos.iterator();

        while (iter.hasNext()) {
            PhotoInfo pi = iter.next();
            if (!new File(pi.fullPath(rootPath)).exists()) iter.remove();
        }
        File syncFile = new File(rootPath, ARCHIVE_FILE+".sync");
        if (syncFile.exists()) {
            Object syncObj = readObj(syncFile);
            if (syncObj != null) {
                ArrayList<PhotoInfo> syncInfos = (ArrayList<PhotoInfo>) syncObj;
                nameSort(syncInfos);
                nameSort(infos);
                int removed = sync2List(syncInfos,infos,null);
                if (removed>0) System.out.println("Sync : deleted files1 = "+removed);

                removed = sync2List(infos,syncInfos,rootPath);
                if (removed>0) System.out.println("Sync : deleted files2 = "+removed);

                List<Modification> modifications = new ArrayList<>();
                for (int i=0; i< syncInfos.size(); i++) {
                    PhotoInfo pi = syncInfos.get(i);
                    PhotoInfo oldPi = infos.get(i);
                    if (oldPi.getFileName().equals(pi.getFileName()) && oldPi.getSubFolder().equals(pi.getSubFolder())) {
                        List<Key> diff = differentOf(pi,oldPi);
                        if (!diff.isEmpty()) {
                            modifications.add(new Modification(pi,diff));
                        }
                    }
                }
                if (modifications.size()>0) Modification.execute(modifications,archiveInfo);

                archiveInfo.setInfos(syncInfos);
                syncFile.delete();

                archiveInfo.sortInfos();
                archiveInfo.saveInfos();
            }
        }
    }

    public static void syncExifAttributesByTime(ArchiveInfo source, ArchiveInfo target) {
        List<PhotoInfo> sourceList = source.getInfos(), targetList = target.getInfos();
        Iterator<PhotoInfo> iter = sourceList.iterator();
        int index = 0;
        List<Modification> modificationList = new ArrayList<>();
        int modified = 0, same = 0;
        while (iter.hasNext()) {
            PhotoInfo srcPi = iter.next();
            if (srcPi.getShootTime()==null) continue;
            for (int i=index;i<targetList.size();i++) {
                PhotoInfo tarPi = targetList.get(i);
                index = i;
                if (tarPi.getShootTime()==null || tarPi.getFileName().endsWith(".xmp")) continue;
                long pc = tarPi.getShootTime().getTime() - srcPi.getShootTime().getTime();
                if ( pc < 0) continue;
                else if (pc > 0) break;
                else {
                    index++;
                    Map<String, Object> params = Modification.exifMap(srcPi, Arrays.asList(ArchiveUtils.MODIFIABLE_KEYS), true);
                    Modification.deleteSameProperties(tarPi,params);
                    if (!params.isEmpty()) {
                        modified ++;
                        modificationList.add(new Modification(Modification.Exif,
                                tarPi.getSubFolder() + (tarPi.getSubFolder().isEmpty()?"":File.separator) + tarPi.getFileName(),
                                params));
                    } else same ++;
                }
            }
        }
        if (!modificationList.isEmpty()) Modification.execute(modificationList,target);
        System.out.println("同步RAW文件数量: "+modified+"; 忽略相同文件数: "+same+"; 匹配失败文件数: "+(targetList.size()-modified-same));
    }
    public static void syncThumbOrientation(ArchiveInfo archiveInfo, String subFolder) {
        List<PhotoInfo> infos = archiveInfo.subFolderInfos(subFolder);
        String root = archiveInfo.getPath();
        List<Modification> modificationList = new ArrayList<>();
        for (PhotoInfo pi : infos) {
            if (pi.getMimeType()==null || !pi.getMimeType().contains("image")) continue;
            try {
                String thumbPath = pi.fullThumbPath(root);
                if (new File(thumbPath).exists()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put(Key.getName(Key.ORIENTATION), Orientation.name(pi.getOrientation()==null ? 1 : pi.getOrientation()));
                    modificationList.add(new Modification(Modification.Exif,
                            thumbPath.substring(root.length() + 1), params));
                }
            } catch (Exception e){}

        }
        if (!modificationList.isEmpty()) {
            Modification.execute(modificationList,archiveInfo);
            System.out.println("同步修改缩略图数量: "+modificationList.size());
        } else System.out.println("没有需要同步修改缩略图");
    }
    public static boolean deletePhoto(ArchiveInfo archiveInfo,String path) {
        if (path==null) return false;
        PhotoInfo pi = archiveInfo.find(new File(archiveInfo.getPath() , path));
        if (pi!=null) {
            pi.delete(archiveInfo.getPath());
            archiveInfo.getInfos().remove(pi);
            return true;
        }
        return false;
    }
    public static void defaultSort(List<PhotoInfo> infos) {
        if (infos!=null && infos.size()>1)
            infos.sort((a,b)->a.compareTo(b));
    }
    public static void nameSort(List<PhotoInfo> infos) {
        if (infos!=null && infos.size()>1)
            infos.sort((a,b)->(a.getSubFolder()+File.separator+a.getFileName()).compareTo(b.getSubFolder()+File.separator+b.getFileName()));
    }
    public static int xcopy(String source,String target) {
        File [] files = new File(source).listFiles(f->!f.getName().equals(".") && !f.getName().equals(".."));
        if (files==null || files.length==0) return 0;
        int count = 0;
        for (File f: files) {
            if (f.isFile()) {
                File tf=new File(target, f.getName());
                if (!tf.exists()) {
                    tf.getParentFile().mkdirs();
                    try {
                        Files.copy(f.toPath(),tf.toPath());
                        count++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else count += xcopy(source+File.separator+f.getName(),target+File.separator+f.getName());
        }
        if (count>0) System.out.println("copy "+count +" files from "+source +" to "+target);
        return count;
    }

    public static  Map<String,String> dirContainer(String source) {
        Map<String,String> map=new HashMap<>();
        File [] files = new File(source).listFiles(f->!f.getName().equals(".") && !f.getName().equals(".."));
        if (files==null || files.length==0) return map;
        for (File f: files) {
            if (f.isFile()) {
                int pos = f.getName().lastIndexOf(".");
                if (pos>=0) map.put(f.getName().substring(0,pos),f.getName().substring(pos));
                else map.put(f.getName(),"");
            } else map.putAll( dirContainer(source+File.separator+f.getName()));
        }
        return map;
    }
    public static int deleteFilesInMap(String source,Map<String,String> map) {
        File [] files = new File(source).listFiles(f->!f.getName().equals(".") && !f.getName().equals(".."));
        if (files==null || files.length==0) return 0;
        int count = 0;
        for (File f: files) {
            if (f.isFile()) {
                int pos = f.getName().lastIndexOf(".");
                String name = (pos>=0 ? f.getName().substring(0,pos) : f.getName());
                if (map.containsKey(name) && f.delete()) count++;
            } else count += deleteFilesInMap( source+File.separator+f.getName(),map);
        }
        return count;
    }
    public static void setOutput(Class clazz,String stdoutFilePath) {
        new File(stdoutFilePath).delete();
        if (System.console()==null && !("file".equals(clazz.getResource("").getProtocol()))) {
            try {
                System.setOut(new PrintStream(new File(stdoutFilePath)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }
    public static void recoveryFrom(String path,ArchiveInfo archiveInfo) {
        String rootPath = archiveInfo.getPath();
        List<PhotoInfo> infos = archiveInfo.getInfos();
        Iterator<PhotoInfo> iter = infos.iterator();
        while (iter.hasNext()) {
            PhotoInfo pi = iter.next();
            File img = new File(pi.fullPath(rootPath));
            if (!img.exists() || img.length()==0) {
                File rec = new File(pi.fullPath("H:\\Photo\\Archived"));
                if (rec.exists() && rec.length()>0) {
                    try {
                        Files.copy(rec.toPath(),img.toPath(),StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Recovery file :"+img.getAbsolutePath());
                    } catch (IOException e) {
                        pi.delete(rootPath);
                        iter.remove();
                        appendToFile(new File("recovery.bat"),"copy \""+rec.getAbsolutePath()+"\" \""+img.getAbsolutePath()+"\"");
                    }
                } else System.out.println("No recovery file :"+img.getAbsolutePath());
            }
        }
    }
    public static String formatterSubFolder(String path) {
        if (path==null) return "";
        if (!File.separator.equals("/")) path = path.replace("/",File.separator);
        if (path.startsWith(File.separator)) path=path.substring(1);
        else if (path.endsWith(File.separator)) path = path.substring(0,path.length()-1);
        return path;
    }
    public static String poiFromPath(String path) {
        if (path==null) return null;
        String [] dirs = path.split("\\\\|/");
        for (int i=dirs.length-1; i>=0; i--) {
            String d = dirs[i].toLowerCase();
            if (d.equals("l") || d.equals("p") || d.equals("g") || d.equals("jpg") || d.equals("raw") || d.equals("nef")) continue;
            if (d.equals("landscape") || d.equals("portrait") ) continue;
            if (d.equals("camera") || d.equals("video") || d.equals("mov") || d.equals("audio")
                    || d.equals("mp4") || d.equals("mp3") || d.equals("res") || d.equals("resource")) continue;
            if (d.contains("风景") || d.contains("景物") || d.contains("人物") || d.contains("人像") || d.contains("生活")) continue;
            d = dirs[i].trim();
            while (!d.isEmpty() && d.charAt(0)>='0' && d.charAt(0)<='9') d=d.substring(1);
            d=d.trim();
            if (!d.isEmpty()) return d;
        }
        return null;
    }

    public static ArchiveInfo getArchiveInfo(String path, boolean clearInfo, boolean removeSameFile, boolean moveOtherFile) {
        if (path==null || path.isEmpty() || "-".equals(path)) return null;
        if (clearInfo) {
            new File(path,ArchiveUtils.ARCHIVE_FILE).delete();
            new File(path,ArchiveUtils.ARCHIVE_FILE+ ".sorted.dat").delete();
            new File(path,ArchiveUtils.same_photo_log).delete();
            new File(path,ArchiveUtils.manual_other_bat).delete();
            new File(path,ArchiveUtils.manual_rm_bat).delete();
            new File(path,ArchiveUtils.manual_archive_bat).delete();
            new File(path,ArchiveUtils.no_shottime_log).delete();
            new File(path,ArchiveUtils.folder_info_dat).delete();
        }
        ArchiveInfo	a = new ArchiveInfo(path);
        if (!a.isReadFromFile()) ArchiveUtils.processDir(a, removeSameFile, moveOtherFile);
        return a;
    }

    public static void copyToFolder(ArchiveInfo camera, ArchiveInfo archived, List<FolderInfo> folderInfos) {
        for (FolderInfo fi: folderInfos) {
            new File(fi.getPath(),fi.getCamera()).mkdirs();
        }
        String sameLog = ArchiveUtils.getFromFile(new File(camera.getPath(),ArchiveUtils.same_photo_log));
        if (sameLog==null) sameLog="";
        StringBuilder mvfailed=new StringBuilder();
        StringBuilder notarchived = new StringBuilder();
        String root = camera.getPath();
        for (PhotoInfo pi : camera.getInfos()) {
            Date dt = pi.getShootTime();
            if (dt==null) notarchived.append(pi.fullPath(root)).append("\r\n");
            else {
                FolderInfo fi = FolderInfo.findFolder(dt,folderInfos);
                File source = new File(pi.fullPath(root));
                File targetDir = null;
                if (fi==null) { // 没有发现目录，新建目录
                    if (pi.getSubFolder().isEmpty()) {
                        targetDir = new File(archived.getPath() + File.separator + DateUtil.date2String(dt, "yyyy")
                                + File.separator + DateUtil.date2String(dt, "yyyyMM") + File.separator + FolderInfo.DEFPATH);
                        targetDir.mkdirs();
                    } else {  // 使用原有的目录，合并文件夹
                        targetDir = new File(archived.getPath() + File.separator + pi.getSubFolder());
                        targetDir.mkdirs();
                    }
                }
                else targetDir = new File(fi.getPath()+File.separator+fi.getCamera());
                try {
                    archived.moveFile(pi, camera.getPath(), new File(targetDir,source.getName()));
                    sameLog.replace(source.getCanonicalPath(),new File(targetDir,source.getName()).getCanonicalPath());
                } catch (Exception e) {
                    mvfailed.append("move \"").append(pi.fullPath(root)).append("\" ").append("\"")
                            .append(fi.getPath()).append("\\").append(fi.getCamera()).append("\"\r\n");
                }
            }
        }
        processDir(archived,false,false);
        if (!sameLog.isEmpty()) ArchiveUtils.writeToFile(new File(camera.getPath(),ArchiveUtils.same_photo_log),sameLog.trim());
        ArchiveUtils.writeToFile(new File(root,ArchiveUtils.no_shottime_log),notarchived.toString());
        ArchiveUtils.writeToFile(new File(root,ArchiveUtils.manual_archive_bat),mvfailed.toString());
    }

    public static void processDir(ArchiveInfo a, boolean removeSameFile, boolean moveOtherFiles) {
        System.out.println("排序 " + a.getPath() + " 文件，共有 : " + a.getInfos().size() + " ...");
        a.sortInfos();

        System.out.println(removeSameFile?"删除重复文件..." : "扫描重复文件...");
        a.scanSameFiles(removeSameFile);

        if (moveOtherFiles) {
            System.out.println("移动无拍摄日期文件...");
            a.moveNoShootTimeFiles(false);
        }
        System.out.println("保存处理后文件...");
        a.saveInfos();
    }
    public static void executeArchive(ArchiveInfo camera, ArchiveInfo archived) {
        List<FolderInfo> folderInfos = FolderInfo.scanFolderInfo(archived);
        if (folderInfos!=null) {
            System.out.println("Now :"+DateUtil.date2String(new Date()));
            System.out.println("将文件归档...");
            ArchiveUtils.copyToFolder(camera, archived, folderInfos);
            System.out.println("Now :"+DateUtil.date2String(new Date()));
            System.out.println("删除空目录");
            ArchiveUtils.removeEmptyFolder(new File(archived.getPath()));
            ArchiveUtils.removeEmptyFolder(new File(camera.getPath()));
            System.out.println("Now :"+DateUtil.date2String(new Date()));
            System.out.println("归档完成");
        }
    }
}
