package tang.qinyoyo.archive;
import javafx.util.Pair;
import tang.qinyoyo.ArchiveUtils;
import tang.qinyoyo.exiftool.CommandRunner;
import tang.qinyoyo.exiftool.ExifTool;
import tang.qinyoyo.exiftool.Key;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;

public class ArchiveInfo {
    public static final Key[] NEED_KEYS = new Key[]{
            Key.DATETIMEORIGINAL,Key.SUB_SEC_TIME_ORIGINAL,Key.CREATEDATE,Key.SUB_SEC_TIME_CREATE,
            Key.MODEL, Key.LENS_ID, Key.ORIENTATION, Key.IMAGE_WIDTH, Key.IMAGE_HEIGHT,
            Key.DOCUMENT_ID, Key.IPTCDigest,
            Key.GPS_LONGITUDE, Key.GPS_LATITUDE, Key.GPS_ALTITUDE,
            Key.MIME_TYPE, Key.ARTIST, Key.HEADLINE,Key.DESCRIPTION,Key.RATING,Key.SCENE,
            Key.COUNTRY,Key.STATE,Key.CITY,Key.LOCATION,Key.SUBJECT_CODE};
    public static final String no_shottime_log = ".no_shottime.log";
    public static final String manual_other_bat = ".manual_other.bat";
    public static final String same_photo_log = ".same_photo.log";
    public static final String manual_rm_bat = ".manual_rm.bat";
    public static final String ARCHIVE_FILE = ".archive_info.dat";
    public static final String manual_archive_bat = ".manual_archive.bat";
    public static final String folder_info_dat = ".folder_info.dat";
    public static final String folder_info_lost_log = ".folder_info_lost.log";

    public static String FFMPEG = "ffmpeg";
    private static String FFMPEG_VERSION = null;
    private String path; // 末尾不带分隔符
    private List<PhotoInfo> infos;
    private ExifTool exifTool;
    private boolean readFromFile = false;

    public boolean isReadFromFile() {
        return readFromFile;
    }

    public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<PhotoInfo> getInfos() {
		return infos;
	}
	public void setInfos(List<PhotoInfo> infos) {
		this.infos = infos;
	}
	public ExifTool getExifTool() {
		return exifTool;
	}
	public void setExifTool(ExifTool exifTool) {
		this.exifTool = exifTool;
	}
	public void checkFfmpeg() {
        while (FFMPEG_VERSION==null) {
            List<String> argsList = new ArrayList<>();
            argsList.add(FFMPEG);
            argsList.add("-version");
            try {
                Pair<List<String>, List<String>> result = CommandRunner.runWithResult(false, argsList);
                if (result.getKey().size() == 0) {
                    throw new RuntimeException("Could not get version of <" + FFMPEG + ">.");
                }
                Pattern p = Pattern.compile("version\\s+(\\S+)",Pattern.CASE_INSENSITIVE);
                for (String s : result.getKey()) {
                    Matcher m = p.matcher(s);
                    if (m.find()) {
                        FFMPEG_VERSION = m.group(1);
                        break;
                    }
                }
                if (FFMPEG_VERSION==null) FFMPEG_VERSION = result.getKey().get(0);
                System.out.println("Installed <" + FFMPEG + "> Version: " + FFMPEG_VERSION);
                return;
            } catch (Exception e) {
                System.out.println(e.getMessage()+" Where is ffmpeg installed or 'q' for skip?");
                try {
                    Scanner in = new Scanner(System.in);
                    String input = in.nextLine().trim();
                    if (input.equals("q")) {
                        FFMPEG = null;
                        FFMPEG_VERSION="";
                        return;
                    }
                    FFMPEG = new File(input, "ffmpeg").getCanonicalPath();
                } catch (IOException ex) {
                }
            }
        }
    }
    public ArchiveInfo() {
        exifTool = ExifTool.getInstance();
        checkFfmpeg();
        infos = new ArrayList<>();
    }
	public ArchiveInfo(String dir) {
        exifTool = ExifTool.getInstance();
        checkFfmpeg();
        File d = new File(dir);
        if (!d.exists()) d.mkdirs();
        try {
            if (d.isDirectory()) path = d.getCanonicalPath();
            else throw new RuntimeException("必须指定目录而不是文件");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (path.endsWith(File.separator)) path=path.substring(0,path.length()-1);
        File af = new File(d, ARCHIVE_FILE);
        if (af.exists()) readInfos();
        else seekPhotoInfo();
        if (infos==null)  infos = new ArrayList<>();
    }

    public void addFile(File f) {
        try {
            if (!f.exists()) return;
            String p = f.getCanonicalPath();
            if (!p.startsWith(path)) return;
            if (f.isFile()) {
                if (find(f)==null) {
                    PhotoInfo pi = new PhotoInfo(path, f);
                    pi.readProperties(path);
                    infos.add(pi);
                    sortInfos();
                }
            } else {
                List<PhotoInfo> list = new ArrayList<>();
                seekPhotoInfosInFolder(f,list);
                if (list!=null && list.size()>1) {
                    list.sort((a, b) -> a.compareTo(b));
                    int count = 0;
                    for (int i=0;i<list.size();i++) {
                        if (find(new File(list.get(i).fullPath(path)))==null) {
                            infos.add(list.get(i));
                            count++;
                        }
                    }
                    if (count>0) sortInfos();
                }
            }
        } catch (Exception e) {}
    }
    public void seekPhotoInfosInFolder(File dir, List<PhotoInfo> infoList) {
        if (!dir.isDirectory() || !dir.exists()) return;
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
            fileInfos = exifTool.query(dir, NEED_KEYS);
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
	                        PhotoInfo photoInfo = new PhotoInfo(path, new File(dir, file));
	                        photoInfo.setPropertiesBy(fileInfos.get(file));
	                        if (dir.getName().endsWith(".web") && photoInfo.getMimeType()!=null && photoInfo.getMimeType().contains("html") && !photoInfo.getFileName().equals("index.html")) {
                                System.out.println("    忽略文件 " + file);
                            } else {
                                infoList.add(photoInfo);
                                count++;
                            }
	                    } else System.out.println("    忽略文件 " + file);
	                } catch (Exception e1) {
	                }
            	}
            }
        }
        System.out.println("    处理文件数 : "+count);
        File[] subDirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith(".")) return false;
                return pathname.isDirectory();
            }
        });
        if (subDirs!=null && subDirs.length>0) {
            List<File> dirs = Arrays.asList(subDirs);
            dirs.sort((a,b)->{
                return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
            });
            for (File d : dirs) {
                if (d.isDirectory()) {
                    seekPhotoInfosInFolder(d,infoList);
                }
            }
        }
    }
    public void seekPhotoInfo() {
        File dir = new File(path);
        infos = new ArrayList<>();
        seekPhotoInfosInFolder(dir, infos);
    }
    public void readInfos() {
        File af = new File(path, ARCHIVE_FILE);
        System.out.println("从 "+af.getAbsolutePath()+" 读取数据");
        Object obj = readObj(af);
        if (obj==null) seekPhotoInfo();
        else {
            try {
                infos = (ArrayList<PhotoInfo>) obj;
                readFromFile = true;
                File oldFile = new File(path, ARCHIVE_FILE+".old");
                if (oldFile.exists()) {
                    Object oldObj = readObj(oldFile);
                    if (oldObj!=null) {
                        ArrayList<PhotoInfo> oldInfos = (ArrayList<PhotoInfo>) oldObj;
                        Iterator<PhotoInfo> iter = oldInfos.iterator();
                        int success = 0, failed = 0;
                        for (PhotoInfo pi: infos) {
                            while (iter.hasNext()) {
                                PhotoInfo oldPi = iter.next();
                                if (oldPi.getFileName().equals(pi.getFileName()) && oldPi.getSubFolder().equals(pi.getSubFolder())) {
                                    Integer orientation = null;
                                    if (!ArchiveUtils.equals(pi.getOrientation(),oldPi.getOrientation())) {
                                        orientation = pi.getOrientation();
                                        if (orientation == null) orientation = Orientation.NONE.getValue();
                                    }
                                    Integer rating = null;
                                    if (!ArchiveUtils.equals(pi.getRating(),oldPi.getRating())) {
                                        rating = pi.getRating();
                                        if (rating == null) rating = 0;
                                    }
                                    if (orientation!=null || rating!=null) {
                                        String filePath = pi.fullPath(path);
                                        if (!Orientation.setOrientationAndRating(new File(filePath), orientation, rating)) {
                                            System.out.println("Sync orientation , rating failed: " + filePath);
                                            failed++;
                                        } else success++;
                                    }
                                    break;
                                }
                            }
                            if (!iter.hasNext()) break;
                        }
                        if (success>0 || failed>0) {
                            System.out.println(String.format("Sync orientation , rating success %d files, failed %d files ",success,failed));
                        }
                    }
                }
            } catch (Exception e) {
                seekPhotoInfo();
            }
        }
    }

    public void sortInfos() {
    	if (infos!=null && infos.size()>1)
	        infos.sort((a,b)->a.compareTo(b));
    }
    public void createThumbFiles(PhotoInfo p) {
        try {
            String thumbPath = p.fullThumbPath(getPath());
            File thumbFile = new File(thumbPath);
            String imgPath = p.fullPath(getPath());
            File imgFile = new File(imgPath);

            if (!imgFile.exists() || p.getMimeType()==null) return;
            if( thumbFile.exists() && thumbFile.lastModified() >= imgFile.lastModified()) return;
            thumbFile.getParentFile().mkdirs();
            if (p.getMimeType().contains("image/")) {
                System.out.println("Create thumbnail of " + imgPath);
                ImageUtil.compressImage(imgPath, thumbPath, 300, 200, p.getOrientation());
            } else if (FFMPEG!=null && p.getMimeType().contains("video/")) {
                System.out.println("Create thumbnail of " + imgPath);
                CommandRunner.run(FFMPEG,"-i", imgPath, "-y", "-f", "image2", "-t", "0.001",
                        // "-s", size,
                        thumbPath);
            }
        } catch (IOException e) {
        }
    }
    public void createThumbFiles() {
        String subFolder = "";
        if (infos!=null && infos.size()>0) {
            for (PhotoInfo p : infos) {
                if (!subFolder.equals(p.getSubFolder())) {
                    subFolder = p.getSubFolder();
                    System.out.println("创建缩略图: "+subFolder);
                }
                createThumbFiles(p);
            }
        }
    }
    public int indexOf(File file) {
        try {
            String path = file.getCanonicalPath();
            if (path.startsWith(path)) {
                String fileName = file.getName();
                String subFolder = path.substring(path.length()+1,path.length()-fileName.length()-1);
                for (int i=0;i<infos.size();i++) {
                    PhotoInfo p = infos.get(i);
                    if (subFolder.equals(p.getSubFolder()) && fileName.equals(p.getFileName())) {
                        return i;
                    }
                }
                return -1;
            } else return -1;
        } catch (Exception e) {
            return -1;
        }
    }
    public boolean deleteFile(String subFolder,String fileName) {
        File file = new File(new File(getPath(),subFolder),fileName);
        File thumb = new File(new File(new File(getPath(),".thumb"),subFolder),fileName);
        if (file.delete()) {
            thumb.delete();
            for (int i=0;i<infos.size();i++) {
                PhotoInfo p = infos.get(i);
                if (subFolder.equals(p.getSubFolder()) && fileName.equals(p.getFileName())) {
                    infos.remove(i);
                    return true;
                }
            }
            return true;
        } else return false;
    }
    public boolean deleteFile(PhotoInfo p) {
        return deleteFile(p.getSubFolder(),p.getFileName());
    }
    public void insert2SortedInfos(PhotoInfo p) {
        for (int i=0;i<infos.size();i++) {
            if (p.compareTo(infos.get(i))<=0) {
                infos.add(i,p);
                return;
            }
        }
        infos.add(p);
    }
    public boolean moveFile(File source,File target) {
        try {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            int index1 = indexOf(source);
            int index2 = indexOf(target);

            if (index1==-1 && index2== -1) {
                PhotoInfo p = new PhotoInfo(path,target);
                p.readProperties(path);
                insert2SortedInfos(p);
            } else if (index1==-1) {
                PhotoInfo p = infos.remove(index2);  // 需要重新排序和读取属性
                p.readProperties(path);
                insert2SortedInfos(p);
            } else if (index2 == -1) {
                PhotoInfo p = infos.remove(index1);
                p.setFile(path,target);
                insert2SortedInfos(p);
            } else {
                PhotoInfo p1 = infos.remove(index1);
                infos.remove(index2);
                p1.setFile(path,target);
                insert2SortedInfos(p1);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean moveFile(PhotoInfo pi,String sourceRootPath, File target) {
        try {
            Files.move(new File(pi.fullPath(sourceRootPath)).toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            File sourceThumb = new File(pi.fullThumbPath(sourceRootPath));
            if (sourceThumb.exists()) {
                try {
                    String fullP = path + File.separator + ".thumb" + target.getCanonicalPath().substring(path.length());
                    File targetThumb = new File(fullP);
                    Files.move(sourceThumb.toPath(), targetThumb.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {}
            }
            int index1 = path.equals(sourceRootPath) ? infos.indexOf(pi) : -1;
            int index2 = indexOf(target);

            if (index1!=-1) infos.remove(index1);
            if (index2!=-1) infos.remove(index2);

            PhotoInfo p1 = pi.cloneObject();
            p1.setFile(path,target);
            insert2SortedInfos(p1);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public void saveInfos() {
    	if (infos==null) return;
        File af = new File(path, ARCHIVE_FILE);
        System.out.println("向 "+af.getAbsolutePath()+" 写入数据");
        saveObj(af, infos);
    }
    public long lastModified() {
        File af = new File(path, ARCHIVE_FILE);
        if (af.exists()) return af.lastModified();
        else return new Date().getTime();
    }
    private static void saveObj(File file, Object object) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);

            objectOutputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static Object readObj(File file) {
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
    public void moveNoShootTimeFiles() {
        try {
            List<PhotoInfo> all = getInfos();
            System.out.println("照片数量 : " + all.size());
            List<PhotoInfo> other = all.stream().filter(a -> a.getShootTime() == null).collect(Collectors.toList());
            if (other.size() > 0) {
                System.out.println("没有拍摄日期照片数量 : " + other.size());
                File rmf = new File(new File(getPath()), ".other");
                rmf.mkdirs();
                ArchiveInfo rma = new ArchiveInfo();
                rma.setPath(rmf.getCanonicalPath());
                rma.setExifTool(getExifTool());
                rma.setInfos(new ArrayList<>(other));
                rma.saveInfos();
                ArchiveUtils.removeAll(all, other);
                StringBuilder sb = new StringBuilder();
                String rootName = getPath();
                String sub = rmf.getCanonicalPath();
                for (PhotoInfo p : other) {
                    File source = new File(p.fullPath(rootName));
                    try {
                        Files.move(source.toPath(), new File(rmf, source.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    } catch (Exception e) {
                        sb.append("move \"").append(p.fullPath(rootName)).append("\" \"").append(ArchiveUtils.newFile(sub, p))
                                .append("\"\r\n");
                    }
                }
                String batcmd = sb.toString().trim();
                if (!batcmd.isEmpty()) ArchiveUtils.writeToFile(new File(getPath(), manual_other_bat), batcmd);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    // 记录必须排序好
    public void scanSameFiles(boolean removeSameFile) {
        List<PhotoInfo> rm = new ArrayList<>();
        List<PhotoInfo> sameAs = new ArrayList<>();
        List<PhotoInfo> all = getInfos();
        System.out.println("照片数量 : " + all.size());
        for (int i = 0; i < all.size(); i++) {
            PhotoInfo pii = all.get(i);
            if (ArchiveUtils.isInWebFolder(pii.getSubFolder())) continue;
            Date dt0 = pii.getShootTime();
            for (int j = i + 1; j < all.size(); j++) {
                PhotoInfo pij = all.get(j);
                if (ArchiveUtils.isInWebFolder(pij.getSubFolder())) continue;
                Date dt1 = pij.getShootTime();
                if (!ArchiveUtils.equals(dt0, dt1))
                    break;
                boolean same = false;
                try {
                    same = pii.sameAs(pij);
                } catch (Exception e) {
                }
                if (same) {
                    rm.add(pii);
                    sameAs.add(pij);
                    break;
                }
            }
        }
        if (removeSameFile) doDeleteSameFiles(rm,sameAs,null);
        else listSameFiles(rm,sameAs);
    }
    // 扫描相同文件，ref为参照结构。两个记录必须排序好
    public void scanSameFilesWith(ArchiveInfo ref) {
        String signFile = "." + ref.getPath().replace(":", "_").replace(File.separator, "_");
        if (new File(getPath(),signFile).exists()) return;
        List<PhotoInfo> rm = new ArrayList<>();
        List<PhotoInfo> sameAs = new ArrayList<>();
        List<PhotoInfo> all = getInfos();
        List<PhotoInfo> refInfos = ref.getInfos();
        System.out.println("照片数量 : " + all.size());
        int j0 = 0, jstart = 0; // 0 - jstart : 没有拍摄日期
        for (int i = 0; i < refInfos.size(); i++) {
            if (refInfos.get(i).getShootTime() != null) {
                jstart = i;
                break;
            }
        }
        j0 = jstart;
        for (int i = 0; i < all.size(); i++) {
            PhotoInfo pii = all.get(i);
            if (ArchiveUtils.isInWebFolder(pii.getSubFolder())) continue;
            Date dt0 = pii.getShootTime();
            if (dt0 == null) { // 没有拍摄日期的互相比较
                for (int j = j0; j < jstart; j++) {
                    PhotoInfo pij = refInfos.get(j);
                    if (ArchiveUtils.isInWebFolder(pij.getSubFolder())) continue;
                    boolean same = false;
                    try {
                        same = pii.sameAs(pij);
                    } catch (Exception e) {
                        same = ArchiveUtils.contentCompare(pii.fullPath(getPath()),pij.fullPath(ref.getPath())) == 0;
                    }
                    if (same) {
                        rm.add(pii);
                        sameAs.add(pij);
                        break;
                    }
                }
            } else {
                boolean j0set = false;
                for (int j = j0; j < refInfos.size(); j++) {
                    PhotoInfo pij = refInfos.get(j);
                    if (ArchiveUtils.isInWebFolder(pij.getSubFolder())) continue;
                    Date dt1 = pij.getShootTime();
                    if (dt1.getTime() == dt0.getTime()) {
                        if (!j0set) {
                            j0 = j;
                            j0set = true;
                        }
                        boolean same = false;
                        try {
                            same = pii.sameAs(pij);
                        } catch (Exception e) {
                            same = ArchiveUtils.contentCompare(pii.fullPath(getPath()),pij.fullPath(ref.getPath())) == 0;
                        }
                        if (same) {
                            rm.add(pii);
                            sameAs.add(pij);
                            break;
                        }
                    } else if (dt1.getTime() > dt0.getTime()) {
                        break;
                    }

                }
            }
        }
        doDeleteSameFiles(rm,sameAs,ref);
        ArchiveUtils.writeToFile(new File(getPath(),signFile),ref.getPath());
    }
    private void listSameFiles(List<PhotoInfo> rm,List<PhotoInfo> sameAs) {
        try {
            if (rm.size() > 0) {
                List<PhotoInfo> all = getInfos();
                File logFile = new File(getPath(), same_photo_log);
                System.out.println("重复照片数量 : " + rm.size());
                StringBuilder sb = new StringBuilder();
                String rootName = getPath();
                for (int i = 0; i < rm.size(); i++) {
                    File one = new File(rm.get(i).fullPath(rootName));
                    File two = new File(sameAs.get(i).fullPath(rootName));
                    if (one.exists() && two.exists()) {
                        if (ArchiveUtils.contentCompare(rm.get(i).fullPath(rootName),sameAs.get(i).fullPath(rootName))==0) {  // 完全相同，删除一个
                            System.out.println("删除完全一致文件 : " + rm.get(i).fullPath(rootName));
                            rm.get(i).delete(rootName);
                            infos.remove(rm.get(i));
                        } else sb.append(one.getCanonicalPath() + " <-> " + two.getCanonicalPath()).append("\r\n");
                    }
                }
                if (sb.length()==0) logFile.delete();
                else ArchiveUtils.writeToFile(logFile,sb.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    // 完全一致的文件将被删除
    private void doDeleteSameFiles(List<PhotoInfo> rm,List<PhotoInfo> sameAs, ArchiveInfo ref) {
        try {
            if (rm.size() > 0) {
                List<PhotoInfo> all = getInfos();
                File logFile = new File(getPath(), same_photo_log);
                System.out.println("重复照片数量 : " + rm.size());
                File rmf = new File(new File(getPath()), ".delete");
                rmf.mkdirs();
                ArchiveInfo rma = new ArchiveInfo();
                rma.setPath(rmf.getCanonicalPath());
                rma.setExifTool(getExifTool());
                rma.setInfos(rm);
                rma.saveInfos();
                ArchiveUtils.removeAll(all, rm);
                StringBuilder sb = new StringBuilder();
                String rootName = getPath();
                String sub = rmf.getCanonicalPath();
                for (int i = 0; i < rm.size(); i++) {
                    PhotoInfo p = rm.get(i);
                    File source = new File(p.fullPath(rootName));
                    try {
                        if (ArchiveUtils.contentCompare(p.fullPath(rootName),sameAs.get(i).fullPath(ref == null ? rootName : ref.getPath()))==0) {  // 完全相同，删除一个
                            System.out.println("删除完全一致文件 : " + p.fullPath(rootName));
                            p.delete(rootName);
                            infos.remove(p);
                        } else {
                            File targetDir = p.getSubFolder() == null || p.getSubFolder().isEmpty() ? rmf : new File(rmf, p.getSubFolder());
                            targetDir.mkdirs();
                            Files.move(source.toPath(), new File(targetDir, source.getName()).toPath());
                            ArchiveUtils.appendToFile(logFile, new File(targetDir, source.getName()).getCanonicalPath() + " <-> "
                                    + sameAs.get(i).fullPath(ref == null ? rootName : ref.getPath()));
                        }
                    } catch (Exception e) {
                        sb.append("move \"").append(p.fullPath(rootName)).append("\" \"").append(ArchiveUtils.newFile(sub, p))
                                .append("\"\r\n");
                        if (!p.absoluteSameAs(sameAs.get(i)))
                            ArchiveUtils.appendToFile(logFile, p.fullPath(rootName) + " <-> " + sameAs.get(i).fullPath(ref == null ? rootName : ref.getPath()));
                    }
                }
                String cmd = sb.toString().trim();
                if (!cmd.isEmpty()) ArchiveUtils.appendToFile(new File(getPath(), manual_rm_bat), sb.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public PhotoInfo find(File f) {
        try {
            String fullPath = f.getCanonicalPath();
            if (!fullPath.startsWith(path)) return null;
            String name = f.getName();
            String subFolder = fullPath.substring(path.length()+1,fullPath.length()-name.length()-1);
            PhotoInfo photoInfo = infos.stream().filter(p -> p.getSubFolder().equals(subFolder) && p.getFileName().equals(name)).findFirst().get();
            return photoInfo;
        } catch (Exception e) {
            return null;
        }
    }

}
