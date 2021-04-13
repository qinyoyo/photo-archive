package qinyoyo.photoinfo;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import qinyoyo.photoinfo.archive.*;
import qinyoyo.photoinfo.exiftool.ExifTool;
import qinyoyo.photoinfo.exiftool.Key;
import qinyoyo.utils.DateUtil;
import qinyoyo.utils.FileUtil;
import qinyoyo.utils.StepHtmlUtil;
import qinyoyo.utils.Util;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ArchiveUtils {
    public static final Key[] NEED_KEYS = new Key[]{
            Key.DATETIMEORIGINAL,Key.SUB_SEC_TIME_ORIGINAL,Key.CREATEDATE,Key.SUB_SEC_TIME_CREATE,
            Key.MODEL, Key.LENS_ID, Key.ORIENTATION, Key.IMAGE_WIDTH, Key.IMAGE_HEIGHT,
            Key.DOCUMENT_ID, Key.IPTCDigest,
            Key.GPS_LONGITUDE, Key.GPS_LATITUDE, Key.GPS_ALTITUDE, Key.GPS_LONGITUDE_REF, Key.GPS_LATITUDE_REF, Key.GPS_ALTITUDE_REF, Key.GPS_DATETIME,
            Key.MIME_TYPE, Key.ARTIST, Key.HEADLINE,Key.DESCRIPTION,Key.RATING,Key.SCENE,
            Key.COUNTRY,Key.COUNTRY_CODE,Key.STATE,Key.CITY,Key.LOCATION,Key.SUBJECT_CODE };
    public static final Key[] MODIFIABLE_KEYS = new Key[]{
            Key.SUBJECT_CODE, Key.ORIENTATION,
            Key.DATETIMEORIGINAL,Key.CREATEDATE,
            Key.ARTIST, Key.HEADLINE,Key.DESCRIPTION,Key.RATING,Key.SCENE,
            Key.COUNTRY,Key.COUNTRY_CODE, Key.STATE,Key.CITY,Key.LOCATION,
            Key.GPS_LONGITUDE, Key.GPS_LATITUDE, Key.GPS_ALTITUDE, Key.GPS_DATETIME
    };
    public static final Key[] MODIFIABLE_KEYS_EXT = new Key[]{
            Key.SUB_SEC_TIME_ORIGINAL,Key.SUB_SEC_TIME_CREATE, Key.BY_LINE,
            Key.GPS_LONGITUDE_REF, Key.GPS_LATITUDE_REF, Key.GPS_ALTITUDE_REF, Key.GPS_DATESTAMP, Key.GPS_TIMESTAMP
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
    public static String VIDEO_CAPTURE_AT = "00:00:01";
    public static String THUMB = ".thumb";
    public static String DELETED = ".deleted";
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        else if (obj1 != null && obj2 != null) {
            if (obj1 instanceof Double && obj2 instanceof Double)
                return Math.abs((Double)obj1 - (Double)obj2) < 0.00001;
            else return obj1.equals(obj2);
        }
        else return false;
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
                        } catch (IOException e){ Util.printStackTrace(e);}
                    if (in2 != null)
                        try {
                            in2.close();
                        } catch (IOException e){ Util.printStackTrace(e);}
                }
            }
        }
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
            String[] dd = folder.split("\\\\|/",-1);
            for (String d : dd) if (d.endsWith(".web")) return true;
        }
        return false;
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

    public static void updateExif(List<PhotoInfo> list,ArchiveInfo archiveInfo, List<Key> keys) {
        List<Modification> modifications = new ArrayList<>();
        list.stream().reduce(modifications,(acc,pi)->{
                acc.add(new Modification(pi,keys));
                return acc;
        },(acc,pi)->null);
        Modification.setExifTags(modifications,archiveInfo,true);
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

    /* 扫描添加html文件的资源引用文件 */
    private static String scanResourceUrl(ArchiveInfo archiveInfo, File srcFile, String currentPath) {
        try {
            String rootPath=archiveInfo.getPath();
            String name = srcFile.getName();
            String dir = srcFile.getParentFile().getCanonicalPath();
            List<PhotoInfo> list = archiveInfo.getInfos().stream().filter(p -> name.equals(p.getFileName())).collect(Collectors.toList());
            if (list != null && list.size() > 0) {
                int maxMatch = 0, position=0;
                for (int i=0;i<list.size();i++) {
                    int macher = 0;
                    String path = new File(rootPath,list.get(i).getSubFolder()).getCanonicalPath();
                    while (macher<path.length() && macher<dir.length() && path.charAt(macher)==dir.charAt(macher)) macher++;
                    if (macher>maxMatch) {
                        maxMatch = macher;
                        position = i;
                    }
                }
                return list.get(position).urlPath(currentPath);
            } else System.out.println("Not found "+srcFile.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void formatStepHtml(ArchiveInfo archiveInfo,File file) {
        if (file.exists() && !file.isDirectory() && (file.getName().toLowerCase().endsWith(".html") || file.getName().toLowerCase().endsWith(".htm"))) {
            try {
                Document doc = StepHtmlUtil.formattedStepHtml(file);
                if (doc!=null) {
                    if (archiveInfo!=null) {
                        String currentPath = file.getParentFile().getCanonicalPath();
                        String rootPath = archiveInfo.getPath();
                        Elements resource = doc.select("img,video,audio");

                        resource.forEach(e -> {
                            String attr = "data-src";
                            String src = e.attr(attr);
                            if (src == null || src.isEmpty()) {
                                attr = "src";
                                src = e.attr(attr);
                            }
                            if (src != null && !src.isEmpty()) {
                                try {
                                    src = URLDecoder.decode(src, "utf-8");
                                    if (src.startsWith("/")) src = rootPath + src;
                                    else src = currentPath + "/" + src;
                                    File recFile = new File(src);
                                    if (!recFile.exists()) {
                                        String foundResource = scanResourceUrl(archiveInfo, recFile, formatterSubFolder(currentPath,rootPath));
                                        if (foundResource == null) {
                                            System.out.println("资源找不到 :" + src);
                                        } else
                                            e.attr(attr, foundResource);
                                    }
                                } catch (Exception ee) {
                                    Util.printStackTrace(ee);
                                }
                            }
                        });
                    }
                    FileUtil.writeToFile(file,StepHtmlUtil.htmlString(doc),"utf-8");
                }
                System.out.println("    格式化游记 : " + file.getPath());
            } catch (IOException e) {
                System.out.println("    格式化失败(" + e.getMessage()+"): " + file.getPath());
                Util.printStackTrace(e);
            }
        }
    }
    public static List<Key> differentOf(PhotoInfo p1, PhotoInfo p2,List<Key> range) {
        List<Key> diff = new ArrayList<>();
        for (Key k : range) {
            if(!equals(p1.getFieldByTag(k),p2.getFieldByTag(k))) diff.add(k);
        }
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
            String thumbPath = pi.fullThumbPath(rootPath);
            if (thumbPath!=null) {
                File sourceThumb = new File(thumbPath);
                if (sourceThumb.exists()) {
                    targetDir = new File(rootPath + File.separator + DELETED + File.separator + THUMB, pi.getSubFolder());
                    targetDir.mkdirs();
                    target = new File(targetDir, pi.getFileName());
                    Files.move(sourceThumb.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e){ Util.printStackTrace(e);}

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


    public static void syncExifAttributesByTime(List<PhotoInfo> sourceList, List<PhotoInfo> targetList, String targetRootPath) {
        Map<String,Map<Key,Object>> modificationList = new HashMap<>();
        int modified = 0, same = 0;
        for (int i=0;i<targetList.size();i++) {
            PhotoInfo tarPi = targetList.get(i);
            if (tarPi.getShootTime()==null || tarPi.getFileName().endsWith(".xmp")) continue;
            List<PhotoInfo> matched = sourceList.stream().filter(p->p.getShootTime()!=null && p.getShootTime().equals(tarPi.getShootTime())).collect(Collectors.toList());
            if (matched!=null && matched.size()>0) matched = matched.stream().filter(p->p.getFileName().equals(tarPi.getFileName())).collect(Collectors.toList());
            if (matched==null || matched.size()==0 || matched.size()>1) continue;
            Map<Key, Object> params = Modification.exifMap(matched.get(0), Arrays.asList(ArchiveUtils.MODIFIABLE_KEYS));
            Modification.deleteSameProperties(tarPi,params);
            if (!params.isEmpty()) {
                modified ++;
                modificationList.put(tarPi.getSubFolder() + (tarPi.getSubFolder().isEmpty()?"":File.separator) + tarPi.getFileName(),params);
            } else same ++;
        }
        if (!modificationList.isEmpty()) Modification.setExifTags(modificationList,targetRootPath);
        System.out.println("同步RAW文件数量: "+modified+"; 忽略相同文件数: "+same+"; 匹配失败文件数: "+(targetList.size()-modified-same));
    }
    public static void syncThumbOrientation(ArchiveInfo archiveInfo, String subFolder) {
        List<PhotoInfo> infos = archiveInfo.subFolderInfos(subFolder,true);
        String root = archiveInfo.getPath();
        List<Modification> modificationList = new ArrayList<>();
        for (PhotoInfo pi : infos) {
            if (pi.getMimeType()==null || !pi.getMimeType().contains("image")) continue;
            String thumbPath = pi.fullThumbPath(root);
            if (thumbPath!=null && new File(thumbPath).exists()) {
                Map<Key, Object> params = new HashMap<>();
                params.put(Key.ORIENTATION, Orientation.name(pi.getOrientation()==null ? 1 : pi.getOrientation()));
                modificationList.add(Modification.exifModified(thumbPath.substring(root.length() + 1), params));
            }
        }
        if (!modificationList.isEmpty()) {
            Modification.setExifTags(modificationList,archiveInfo, false);
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
                        FileUtil.appendToFile(new File("recovery.bat"),"copy \""+rec.getAbsolutePath()+"\" \""+img.getAbsolutePath()+"\"");
                    }
                } else System.out.println("No recovery file :"+img.getAbsolutePath());
            }
        }
    }

    public static String formatterSubFolder(String path,String rootPath) {
        if (path==null) return "";
        if (!File.separator.equals("/")) path = path.replace("/",File.separator);
        if (rootPath!=null && !rootPath.isEmpty()) {
            if (rootPath.equals(path)) return "";
            else if (path.startsWith(rootPath+File.separator)) path = path.substring(rootPath.length()+1);
        }
        if (path.startsWith(File.separator)) path=path.substring(1);
        else if (path.endsWith(File.separator)) path = path.substring(0,path.length()-1);
        return path;
    }
    public static String poiFromPath(String path) {
        if (path==null) return null;
        String [] dirs = path.split("\\\\|/",-1);
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

    public static void clearArchiveInfoFile(String path) {
        new File(path,ArchiveUtils.ARCHIVE_FILE).delete();
        new File(path,ArchiveUtils.same_photo_log).delete();
        new File(path,ArchiveUtils.manual_other_bat).delete();
        new File(path,ArchiveUtils.manual_rm_bat).delete();
        new File(path,ArchiveUtils.manual_archive_bat).delete();
        new File(path,ArchiveUtils.no_shottime_log).delete();
        new File(path,ArchiveUtils.folder_info_dat).delete();
    }
    public static ArchiveInfo getArchiveInfo(String path, boolean clearInfo, boolean removeSameFile, boolean moveOtherFile) {
        if (path==null || path.isEmpty() || "-".equals(path)) return null;
        if (clearInfo) clearArchiveInfoFile(path);
        ArchiveInfo	a = new ArchiveInfo(path);
        if (!a.isReadFromFile()) ArchiveUtils.processDir(a, removeSameFile, moveOtherFile);
        return a;
    }

    public static void copyToFolder(ArchiveInfo camera, ArchiveInfo archived, List<FolderInfo> folderInfos) {
        String sameLog = FileUtil.getFromFile(new File(camera.getPath(),ArchiveUtils.same_photo_log));
        if (sameLog==null) sameLog="";
        List<PhotoInfo> noShootTime = new ArrayList<>();
        List<PhotoInfo> notMoved = new ArrayList<>();
        String root = camera.getPath();
        for (PhotoInfo pi : camera.getInfos()) {
            Date dt = pi.getShootTime();
            if (dt == null) noShootTime.add(pi);
            else {
                long d = dt.getTime() / FolderInfo.DAY_LENGTH;
                List<FolderInfo> fis = folderInfos.stream().filter(f -> f.getDate0() <= d && f.getDate1() >= d).collect(Collectors.toList());
                if (fis == null || fis.size() == 0) notMoved.add(pi);
                else {
                    if (fis.size() > 1 && pi.getModel() != null)
                        fis = fis.stream().filter(f -> f.getModels().contains(pi.getModel())).collect(Collectors.toList());
                    if (fis == null || fis.size() == 0 || fis.size() > 1) notMoved.add(pi);
                    else {
                        File target = new File(archived.getPath(),fis.get(0).getPath().isEmpty() ? pi.getFileName() : fis.get(0).getPath() + File.separator + pi.getFileName());
                        if (!archived.moveFile(pi, camera.getPath(), target))
                            notMoved.add(pi);
                    }
                }
            }
        }
        File notMovedDir = new File(camera.getPath(),".notMoved");
        notMovedDir.mkdirs();
        for (PhotoInfo pi : notMoved) {
            File source = new File(pi.fullPath(root));
            File targetDir = null;
            if (pi.getSubFolder().isEmpty()) {
                Date dt = pi.getShootTime();
                targetDir = new File(notMovedDir, DateUtil.date2String(dt, "yyyy")
                        + File.separator + DateUtil.date2String(dt, "yyyyMM"));
                targetDir.mkdirs();
            } else {  // 使用原有的目录，合并文件夹
                targetDir = new File(notMovedDir ,pi.getSubFolder());
                targetDir.mkdirs();
            }
            try {
                Files.move(source.toPath(), new File(targetDir, pi.getFileName()).toPath(), StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                System.out.println(source.getAbsolutePath() + " 移动失败: "+e.getMessage());
            }
        }
        processDir(archived,false,false);
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
        List<FolderInfo> folderInfos = FolderInfo.seekFolderInfo(archived);
        if (folderInfos!=null) {
            System.out.println("Now :"+DateUtil.date2String(new Date()));
            System.out.println("将文件归档...");
            ArchiveUtils.copyToFolder(camera, archived, folderInfos);
            System.out.println("Now :"+DateUtil.date2String(new Date()));
            System.out.println("删除空目录");
            FileUtil.removeEmptyFolder(new File(archived.getPath()));
            FileUtil.removeEmptyFolder(new File(camera.getPath()));
            System.out.println("Now :"+DateUtil.date2String(new Date()));
            System.out.println("归档完成");
        }
    }

    public static List<PhotoInfo> stepUnderFolder(String rootPath, String folder, boolean includeSubFolder) {
        List<PhotoInfo> list = new ArrayList<>();
        File[] subDirs = new File(rootPath,folder).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory() && pathname.getName().endsWith(".web")) {
                    return new File(pathname,"index.html").exists();
                } else if (includeSubFolder) {
                    return pathname.isDirectory() && !pathname.getName().endsWith(".web") && !pathname.getName().startsWith(".");
                } else return false;
            }
        });
        if (subDirs!=null && subDirs.length>0) {
            for (File d: subDirs) {
                if (d.getName().endsWith(".web")) {
                    PhotoInfo pi = new PhotoInfo(rootPath, new File(d, "index.html"));
                    pi.setMimeType("text/html");
                    pi.setSubTitle(d.getName().substring(0, d.getName().length() - 4));
                    list.add(pi);
                } else {
                    List<PhotoInfo> subList = stepUnderFolder(rootPath, folder + (folder.isEmpty()?"":File.separator) + d.getName(), includeSubFolder);
                    if (subList!=null && subList.size()>0) list.addAll(subList);
                }
            }
        }
        return list;
    }
    public static List<PhotoInfo> seekPhotoInfosInFolder(File dir, String rootPath, boolean includeSubFolder) {
        return seekPhotoInfosInFolder(dir, rootPath,includeSubFolder, null);
    }
    public static List<PhotoInfo> seekPhotoInfosInFolder(File dir, String rootPath, boolean includeSubFolder, List<String> excludeExts) {
        List<PhotoInfo> infoList = new ArrayList<>();
        if (!dir.isDirectory() || !dir.exists()) return infoList;
        File [] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (!pathname.isDirectory() && pathname.getName().startsWith(".") && pathname.length() == 4096);
            }
        });
        if (files!=null && files.length>0) {
            System.out.println("删除 "+dir.getAbsolutePath()+" .开始的小文件 : "+files.length);
            for (File f : files) f.delete();
        }
        System.out.println("批量搜索 "+dir.getAbsolutePath());
        Map<String, Map<Key, Object>> fileInfos = null;

        int count = 0;
        try {
            fileInfos = ExifTool.getInstance().query(dir, excludeExts, ArchiveUtils.NEED_KEYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fileInfos!=null) {
            for (String file : fileInfos.keySet()) {
                if (file.equals(ExifTool.ERROR)) {
                    System.out.println(fileInfos.get(file).get(Key.DESCRIPTION));
                } else {
                    try {
                        if (SupportFileType.isSupport(file)) {
                            PhotoInfo photoInfo = new PhotoInfo(rootPath, new File(dir, file));
                            photoInfo.setPropertiesBy(fileInfos.get(file));
                            if (photoInfo.getShootTime()==null && photoInfo.getCreateTime()!=null && photoInfo.getMimeType()!=null && !photoInfo.getMimeType().toLowerCase().startsWith("image"))
                                photoInfo.setShootTime(photoInfo.getCreateTime());
                            if (photoInfo.getShootTime()==null) {
                                photoInfo.setShootTime(DateUtil.getShootTimeFromFileName(photoInfo.getFileName()));
                                if (photoInfo.getShootTime()!=null) {
                                    ExifTool.getInstance().execute(new File(photoInfo.fullPath(rootPath)), "-overwrite_original",
                                            "-DateTimeOriginal="+DateUtil.date2String(photoInfo.getShootTime()));
                                }
                            }
                            if (dir.getName().endsWith(".web") && photoInfo.getMimeType()!=null && photoInfo.getMimeType().contains("html") && !photoInfo.getFileName().equals("index.html")) {
                                System.out.println("    忽略文件 " + file);
                            } else {
                                infoList.add(photoInfo);
                                count++;
                            }
                        } else System.out.println("    忽略文件 " + file);
                    } catch (Exception e1){ Util.printStackTrace(e1);}
                }
            }
        }
        System.out.println("    处理文件数 : "+count);
        if (includeSubFolder) {
            File[] subDirs = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().startsWith(".")) return false;
                    return pathname.isDirectory();
                }
            });
            if (subDirs != null && subDirs.length > 0) {
                List<File> dirs = Arrays.asList(subDirs);
                dirs.sort((a, b) -> {
                    return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
                });
                for (File d : dirs) {
                    if (d.isDirectory()) {
                        infoList.addAll(seekPhotoInfosInFolder(d, rootPath, includeSubFolder));
                    }
                }
            }
        }
        return infoList;
    }
}
