package qinyoyo.photoinfo.archive;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import qinyoyo.photoinfo.ArchiveUtils;
import qinyoyo.photoinfo.exiftool.CommandRunner;
import qinyoyo.photoinfo.exiftool.ExifTool;
import qinyoyo.utils.BaiduGeo;
import qinyoyo.utils.FileUtil;
import qinyoyo.utils.ImageUtil;
import qinyoyo.utils.Util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class ArchiveInfo {
    private String path; // 末尾不带分隔符
    private List<PhotoInfo> infos;
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

    public ArchiveInfo() {
        //exifTool = ExifTool.getInstance();
        infos = new ArrayList<>();
    }
    public ArchiveInfo(String dir) {
        //exifTool = ExifTool.getInstance();
        File d = new File(dir);
        if (!d.exists()) d.mkdirs();
        try {
            if (d.isDirectory()) path = d.getCanonicalPath();
            else throw new RuntimeException("必须指定目录而不是文件");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (path.endsWith(File.separator)) path=path.substring(0,path.length()-1);
        File af = new File(d, ArchiveUtils.ARCHIVE_FILE);
        if (af.exists()) readInfos();
        else seekPhotoInfo();
        if (infos==null)  infos = new ArrayList<>();
        infos.stream().filter(p->p.getLatitude()!=null && p.getLongitude()!=null && p.getCountry()!=null && !p.getCountry().trim().isEmpty()).forEach(p->{
            BaiduGeo.setGeoInfoIntoDatabase(p);
        });
    }

    /**
     * 重新扫描文件或目录。文件或目录在归档目录之下
     * @param f 文件或目录
     */
    public void rescanFile(File f) {
        try {
            if (!f.exists()) return;
            String p = f.getCanonicalPath();
            if (!p.startsWith(path+File.separator) && !p.equals(path)) {
                System.out.println("文件或目录必须在归档目录之下");
                return;
            }
            if (f.isFile()) {
                PhotoInfo pi = find(f);
                Integer orientation = null;
                if (pi==null) {
                    pi = new PhotoInfo(path, f);
                    infos.add(pi);
                } else {
                    orientation = pi.getOrientation();
                    if (orientation==null) orientation = Orientation.NONE.getValue();
                }
                pi.readProperties(path);
                sortInfos();
                String thumbPath = pi.fullThumbPath(path);
                if (thumbPath!=null && !new File(thumbPath).exists()) {
                    System.out.println("创建缩略图 "+thumbPath + " ...");
                    createThumbFiles(pi);
                }
                System.out.println("成功扫描文件 "+p);
            } else {
                List<PhotoInfo> list = ArchiveUtils.seekPhotoInfosInFolder(f,path, true);
                final String seekPath = f.getCanonicalPath().length() == path.length() ? "" : f.getCanonicalPath().substring(path.length()+1);
                final String subSeekPath = seekPath.isEmpty() ? "" : seekPath + File.separator;
                List<PhotoInfo> existedList = infos.stream().filter(
                        pi->seekPath.isEmpty() || pi.getSubFolder().equals(seekPath) || pi.getSubFolder().startsWith(subSeekPath)).collect(Collectors.toList());
                infos.removeAll(existedList);
                Set<String> addFilesPath = new HashSet<>();
                if (list!=null && list.size()>0) {
                    infos.addAll(list);
                    for (int i = 0; i < list.size(); i++) {
                        String thumbPath = list.get(i).fullThumbPath(path);
                        if (thumbPath!=null) {
                            addFilesPath.add(thumbPath);
                            if (!new File(thumbPath).exists()) {
                                System.out.println("创建缩略图 " + thumbPath + " ...");
                                createThumbFiles(list.get(i));
                            }
                        }
                    }
                }
                sortInfos();
                existedList.stream().filter(pi->{
                    String thumbPath = pi.fullThumbPath(path);
                    if (thumbPath==null) return false;
                    else {
                        return !addFilesPath.contains(thumbPath);
                    }
                }).forEach(pi->{
                    String thumbPath = pi.fullThumbPath(path);
                    if (thumbPath!=null) new File(thumbPath).delete();
                });
                System.out.println("成功扫描文件数: "+list.size());
            }
        } catch (Exception e){ Util.printStackTrace(e);}
    }

    /**
     * 添加目录。
     * @param dir 文件或目录
     */
    public void addDirectory(File dir) {
        try {
            if (!dir.exists() || dir.isFile()) {
                System.out.println("必须指定一个存在的目录");
                return;
            }
            String p = dir.getCanonicalPath();
            if (p.startsWith(path+File.separator) || p.equals(path)) {
                rescanFile(dir);
            }
            ArchiveInfo archiveInfo = new ArchiveInfo(p);
            if (archiveInfo.getInfos().size()>0) {
                for (PhotoInfo pi : archiveInfo.getInfos()) {
                    File f = new File(pi.fullPath(path));
                    PhotoInfo pe = this.find(f);
                    if (pe != null) infos.remove(pe);
                }
                infos.addAll(archiveInfo.getInfos());
                FileUtil.moveDirectory(dir.toPath(),Paths.get(path),StandardCopyOption.REPLACE_EXISTING);
                sortInfos();
                saveInfos();
                System.out.println("成功添加文件数: "+archiveInfo.getInfos().size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seekPhotoInfo() {
        File dir = new File(path);
        infos = ArchiveUtils.seekPhotoInfosInFolder(dir, path, true);
        sortInfos();
        saveInfos();
    }
    private void readInfos() {
        File af = new File(path, ArchiveUtils.ARCHIVE_FILE);
        System.out.println("从 "+af.getAbsolutePath()+" 读取数据");
        String json = FileUtil.getFromFile(af,"UTF-8");
        try {
            if (json != null) {
                Gson gson = new GsonBuilder()
                        .setLenient()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                        .create();
                Type type = new TypeToken<ArrayList<PhotoInfo>>() {}.getType();
                infos = gson.fromJson(json, type);
                readFromFile = true;
                return;
            }
        } catch (Exception e){ Util.printStackTrace(e);}
        seekPhotoInfo();
    }

    public int removeNotExistInfo() {
        int removed = 0;
        if (infos!=null) {
            Iterator<PhotoInfo> iterator = infos.iterator();
            while (iterator.hasNext()) {
                PhotoInfo p = iterator.next();
                if (!new File(p.fullPath(path)).exists()) {
                    iterator.remove();
                    removed ++;
                }
            }
        }
        return removed;
    }
    public synchronized void sortInfos() {
    	ArchiveUtils.defaultSort(infos);
    }
    public List<PhotoInfo> subFolderInfos(String subFolder,boolean incSubFolder) {
        final String standardFubFolder = ArchiveUtils.formatterSubFolder(subFolder, path);
        if (standardFubFolder.isEmpty() && incSubFolder) return infos;
        return infos.stream().filter(p->
                    standardFubFolder.equals(p.getSubFolder()) ||
                    (incSubFolder && p.getSubFolder().startsWith(standardFubFolder+File.separator))
        ).collect(Collectors.toList());
    }
    public void createThumbFiles(PhotoInfo p) {
        try {
            String thumbPath = p.fullThumbPath(getPath());
            if (thumbPath!=null) {
                File thumbFile = new File(thumbPath);
                String imgPath = p.fullPath(getPath());
                File imgFile = new File(imgPath);

                if (!imgFile.exists() || p.getMimeType() == null) return;
                if (thumbFile.exists()) return;
                thumbFile.getParentFile().mkdirs();
                if (p.getMimeType().contains("image/")) {
                    ImageUtil.compressImage(imgPath, thumbPath, 300, 200, p.getOrientation());
                } else if (ExifTool.FFMPEG != null && p.getMimeType().contains("video/")) {
                    CommandRunner.run(ExifTool.FFMPEG, "-i", imgPath, "-y", "-f", "image2",
                            // "-t","0.0001",
                            "-frames:v", "1", "-ss", ArchiveUtils.VIDEO_CAPTURE_AT,
                            // "-s", size,
                            thumbPath);
                }
            }
        } catch (IOException e){ Util.printStackTrace(e);}
    }
    public void createThumbFiles(String subFolder) {
        List<PhotoInfo> list = subFolderInfos(subFolder,true);
        if (list!=null && list.size()>0) {
            System.out.println("重建缩略图数量 :"+list.size() + " ...");
            for (PhotoInfo p : list) {
                createThumbFiles(p);
            }
        }
    }
    public void createThumbFiles() {
        createThumbFiles("");
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
        File thumb = new File(new File(new File(getPath(),ArchiveUtils.THUMB),subFolder),fileName);
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
    public boolean moveFile(List<PhotoInfo> list, String newFolder) {
        if (list==null || list.isEmpty()) return false;
        final String subFolder = ArchiveUtils.formatterSubFolder(newFolder,path);
        list=list.stream().filter(p->!subFolder.equals(p.getSubFolder())).collect(Collectors.toList());
        if (list==null || list.isEmpty()) return false;
        File targetDir = new File(path,subFolder);
        targetDir.mkdirs();
        File targetThumbDir = new File(new File(path,ArchiveUtils.THUMB),subFolder);
        targetThumbDir.mkdirs();
        for (PhotoInfo p: list) {
            try {
                File target = new File(targetDir, p.getFileName());
                Files.move(new File(p.fullPath(path)).toPath(), target.toPath());
                try {
                    Files.move(new File(p.fullThumbPath(path)).toPath(), new File(targetThumbDir, p.getFileName()).toPath());
                } catch (Exception e1) {}
                p.setSubFolder(subFolder);
                p.setFile(path,target);
            } catch (Exception e) {
                Util.printStackTrace(e);
                return false;
            }
        }
        sortInfos();
        saveInfos();
        return true;
    }
    public boolean moveFile(PhotoInfo pi,String sourceRootPath, File target) {
        try {
            target.getParentFile().mkdirs();
            Files.move(new File(pi.fullPath(sourceRootPath)).toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            String thumbPath=pi.fullThumbPath(sourceRootPath);
            if (thumbPath!=null) {
                File sourceThumb = new File(thumbPath);
                if (sourceThumb.exists()) {
                    try {
                        String fullP = path + File.separator + ArchiveUtils.THUMB + target.getCanonicalPath().substring(path.length());
                        File targetThumb = new File(fullP);
                        Files.move(sourceThumb.toPath(), targetThumb.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        Util.printStackTrace(e);
                    }
                }
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
    public synchronized void saveInfos() {
    	if (infos==null) return;
        File bak = new File(path, ArchiveUtils.ARCHIVE_FILE+".bak");
        bak.delete();
        File af = new File(path, ArchiveUtils.ARCHIVE_FILE);
        af.renameTo(bak);
        System.out.println("向 "+af.getAbsolutePath()+" 写入数据");
        // ArchiveUtils.saveObj(af, infos);
        Gson gson = new GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .create();

        FileUtil.writeToFile(af,gson.toJson(infos),"UTF-8");
    }

    public void moveNoShootTimeFiles(boolean copyTo) {
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
                rma.setInfos(new ArrayList<>(other));
                rma.saveInfos();
                if(!copyTo) ArchiveUtils.removeAll(all, other);
                StringBuilder sb = new StringBuilder();
                String rootName = getPath();
                String sub = rmf.getCanonicalPath();
                for (PhotoInfo p : other) {
                    File source = new File(p.fullPath(rootName));
                    File target = new File(p.fullPath(rootName+File.separator+".other"));
                    target.getParentFile().mkdirs();
                    try {
                        if (copyTo) Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        else Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        sb.append(copyTo ? "copy \"":"move \"").append(p.fullPath(rootName)).append("\" \"").append(ArchiveUtils.newFile(sub, p))
                                .append("\"\r\n");
                    }
                }
                String batcmd = sb.toString().trim();
                if (!batcmd.isEmpty()) FileUtil.writeToGbkFile(new File(getPath(), ArchiveUtils.manual_other_bat), batcmd);
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
                } catch (Exception e){ Util.printStackTrace(e);}
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
                    if (dt1!=null && dt1.getTime() == dt0.getTime()) {
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
                    } else if (dt1!=null && dt1.getTime() > dt0.getTime()) {
                        break;
                    }

                }
            }
        }
        doDeleteSameFiles(rm,sameAs,ref);
    }
    private void listSameFiles(List<PhotoInfo> rm,List<PhotoInfo> sameAs) {
        try {
            if (rm.size() > 0) {
                List<PhotoInfo> all = getInfos();
                File logFile = new File(getPath(), ArchiveUtils.same_photo_log);
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
                else FileUtil.writeToGbkFile(logFile,sb.toString());
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
                File logFile = new File(getPath(), ArchiveUtils.same_photo_log);
                System.out.println("重复照片数量 : " + rm.size());
                File rmf = new File(new File(getPath()), ArchiveUtils.DELETED);
                rmf.mkdirs();
                ArchiveInfo rma = new ArchiveInfo();
                rma.setPath(rmf.getCanonicalPath());
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
                            FileUtil.appendToGbkFile(logFile, new File(targetDir, source.getName()).getCanonicalPath() + " <-> "
                                    + sameAs.get(i).fullPath(ref == null ? rootName : ref.getPath()));
                        }
                    } catch (Exception e) {
                        sb.append("move \"").append(p.fullPath(rootName)).append("\" \"").append(ArchiveUtils.newFile(sub, p))
                                .append("\"\r\n");
                        if (!p.absoluteSameAs(sameAs.get(i)))
                            FileUtil.appendToGbkFile(logFile, p.fullPath(rootName) + " <-> " + sameAs.get(i).fullPath(ref == null ? rootName : ref.getPath()));
                    }
                }
                String cmd = sb.toString().trim();
                if (!cmd.isEmpty()) FileUtil.appendToGbkFile(new File(getPath(), ArchiveUtils.manual_rm_bat), sb.toString());
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
            int start = path.length()+1, end = fullPath.length()-name.length()-1;
            String subFolder = start < end ? fullPath.substring(start,end) : "";
            return find(subFolder,name);
        } catch (Exception e) {
            return null;
        }
    }
    public PhotoInfo find(String subFolder,String name) {
        Optional<PhotoInfo> photoInfo = infos.stream().filter(p -> p.getSubFolder().equals(subFolder) && p.getFileName().equals(name)).findFirst();
        if (photoInfo.isPresent()) return photoInfo.get();
        else return null;
    }

}
