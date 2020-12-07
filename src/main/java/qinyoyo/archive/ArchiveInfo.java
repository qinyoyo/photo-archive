package qinyoyo.archive;

import qinyoyo.SystemOut;
import qinyoyo.Utils;
import qinyoyo.exiftool.ExifTool;
import qinyoyo.exiftool.Key;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public class ArchiveInfo {
    public static final String ARCHIVE_FILE = ".archive_info.dat";
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
	public ArchiveInfo() {}
	public ArchiveInfo(String dir) {
        try {
            exifTool = new ExifTool.Builder().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File d = new File(dir);
        if (!d.exists()) d.mkdirs();
        if (d.isDirectory()) path = d.getAbsolutePath();
        else throw new RuntimeException("必须指定目录而不是文件");
        if (path.endsWith(File.separator)) path=path.substring(0,path.length()-1);
        File af = new File(d, ARCHIVE_FILE);
        if (af.exists()) readInfos();
        else seekPhotoInfo();
    }
	private String asciiCharsOf(String str) {
		String s="";
		for (int i=0;i<str.length();i++) {
			char ch=str.charAt(i);
			if (ch>0 && ch<128) s=s+ch;
		}
		return s;
	}
	private String aiFileName(File dir, String file) {
		String asciis=asciiCharsOf(file);
        File [] ff = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return asciis.equals(asciiCharsOf(pathname.getName()));
            }
        });
        if (ff.length==1) return ff[0].getName();
        else return null;
	}
    private void seekPhotoInfosInFolder(File dir) {
        if (!dir.isDirectory()) return;
        File [] dotFile = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (!pathname.isDirectory() && pathname.getName().startsWith(".") && pathname.length() == 4096);
            }
        });
        if (dotFile.length>0) {
            SystemOut.println("删除 "+dir.getAbsolutePath()+" .开始的小文件 : "+dotFile.length);
            for (File f : dotFile) f.delete();
        }
        try {
            Map<String, Map<Key, Object>> fileInfos = exifTool.batchQuery(dir, Key.SUBSECDATETIMEORIGINAL, Key.DATETIMEORIGINAL,
                    Key.MAKE, Key.MODEL, Key.LENS_ID,
                    Key.GPS_LONGITUDE, Key.GPS_LATITUDE, Key.GPS_ALTITUDE);
            SystemOut.println("搜索 "+dir.getAbsolutePath()+" 文件数 : "+fileInfos.size());
            for (String file : fileInfos.keySet()) {
            	try {
	                PhotoInfo photoInfo = null;
	                try {
	                	photoInfo = new PhotoInfo(path, new File(dir, file));
	                } catch (Exception e) {
	                	String nf = aiFileName(dir, file);
	                	if (nf!=null) {
	                	   photoInfo = new PhotoInfo(path, new File(dir, nf));
	                	} else throw e;
	                }	                
	                photoInfo.setPropertiesBy(fileInfos.get(file));
	                infos.add(photoInfo);
            	} catch(Exception e1) {           		
            		SystemOut.println("忽略文件 "+new File(dir, file).getAbsolutePath());
            		e1.printStackTrace();
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        File[] subDirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith(".")) return false;
                return pathname.isDirectory();
            }
        });
        if (subDirs.length>0) {
            List<File> files = Arrays.asList(subDirs);
            files.sort((a,b)->{
                return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
            });
            for (File d : files) {
                if (d.isDirectory()) seekPhotoInfosInFolder(d);
            }
        }
    }
    public void seekPhotoInfo() {
        File dir = new File(path);
        infos = new ArrayList<>();
        seekPhotoInfosInFolder(dir);
    }
    public void readInfos() {
        File af = new File(path, ARCHIVE_FILE);
        SystemOut.println("从 "+af.getAbsolutePath()+" 读取数据");
        Object obj = Utils.readObj(af);
        if (obj==null) seekPhotoInfo();
        else {
            try {
                infos = (ArrayList<PhotoInfo>) obj;
                readFromFile = true;
            } catch (Exception e) {
                seekPhotoInfo();
            }
        }
    }
    private static int nameCompare(PhotoInfo a,PhotoInfo b) {
        String na = (a.getFolder() + "\\" + Utils.nameWithoutExt(a.getFileName())).toLowerCase();
        String nb = (b.getFolder() + "\\" + Utils.nameWithoutExt(b.getFileName())).toLowerCase();

        if (na.length()>nb.length() && na.startsWith(nb)) return -1;
        else if (nb.length()>na.length() && nb.startsWith(na)) return 1;
        else {
            na = (a.getFolder() + "\\" + a.getFileName()).toLowerCase();
            nb = (b.getFolder() + "\\" + b.getFileName()).toLowerCase();
            return na.compareTo(nb);
        }
    }
    public void sortInfos() {
    	if (infos!=null && infos.size()>1)
	        infos.sort((a,b)->{
                Date d1 = a.getShootTime(), d2 = b.getShootTime();
                if (d1==null && d2==null) return nameCompare(a,b);
                else if (d1==null)
                    return -1;
                else if (d2==null)
                    return 1;
                else if (d1.getTime() > d2.getTime())
                    return 1;
                else if (d1.getTime() < d2.getTime())
                    return -1;
                else return nameCompare(a,b);
	        });
    }
    public void saveInfos() {
    	if (infos==null) return;
        File af = new File(path, ARCHIVE_FILE);
        SystemOut.println("向 "+af.getAbsolutePath()+" 写入数据");
        Utils.saveObj(af, infos);
    }
}
