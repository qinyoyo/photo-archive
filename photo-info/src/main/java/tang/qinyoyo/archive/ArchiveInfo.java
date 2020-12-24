package tang.qinyoyo.archive;
import tang.qinyoyo.exiftool.ExifTool;
import tang.qinyoyo.exiftool.Key;

import java.io.*;
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
            throw new RuntimeException(e.getMessage());
        }
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
    }

    private void seekPhotoInfosInFolder(File dir) {
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
        files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (!pathname.isDirectory() && !pathname.getName().startsWith("."));
            }
        });
        Set<String> processedFiles = new HashSet<String>();
        System.out.println("批量搜索 "+dir.getAbsolutePath());
        Map<String, Map<Key, Object>> fileInfos = null;
        Key[] keys = new Key[]{Key.SUBSECDATETIMEORIGINAL, Key.DATETIMEORIGINAL,Key.CREATEDATE,
                Key.MAKE, Key.MODEL, Key.LENS_ID,
                Key.DOCUMENT_ID, Key.IPTCDigest,
                Key.GPS_LONGITUDE, Key.GPS_LATITUDE, Key.GPS_ALTITUDE,
                Key.MIME_TYPE, Key.ARTIST, Key.HEADLINE,Key.DESCRIPTION,Key.RATING,Key.SCENE,
                Key.COUNTRY,Key.STATE,Key.CITY,Key.LOCATION,Key.SUBJECT_CODE};
        int count = 0; 
        try {
            fileInfos = exifTool.query(dir, keys);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fileInfos!=null) {
            for (String file : fileInfos.keySet()) {
            	if (file.equals(":ERROR:")) {
            		System.out.println(fileInfos.get(file).get(Key.DESCRIPTION));
            	} else {
	                try {
	                    if (SupportFileType.isSupport(file)) {
	                        PhotoInfo photoInfo = new PhotoInfo(path, new File(dir, file));
	                        photoInfo.setPropertiesBy(fileInfos.get(file));
	                        infos.add(photoInfo);
	                        count++;
	                    } else System.out.println("    忽略文件 " + file);
	                    processedFiles.add(file);
	                } catch (Exception e1) {
	                }
            	}
            }
        }
        System.out.println("    处理文件数 : "+count);
        /*
        if (files!=null && processedFiles.size()<files.length) {
            count=0;
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (processedFiles.contains(f.getName())) continue;
                try {
                    if (SupportFileType.isSupport(f.getName())) {
                        fileInfos = exifTool.query(f, keys);

                        PhotoInfo photoInfo = new PhotoInfo(path, f);
                        photoInfo.setPropertiesBy(fileInfos.get(f.getName()));
                        infos.add(photoInfo);
                        count++;
                    } else System.out.println("    忽略文件 " + f.getName());
                } catch (Exception e1) {
                    System.out.println("    忽略文件 " + f.getName());
                }
            }
            if (count > 0) System.out.println("    中文搜索 " + dir.getAbsolutePath() + " 处理文件数 : " + count);
        }
        */
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
        System.out.println("从 "+af.getAbsolutePath()+" 读取数据");
        Object obj = readObj(af);
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
    private static String nameWithoutExt(String name) {
        int p = name.lastIndexOf(".");
        if (p >= 0)
            return name.substring(0, p);
        else
            return name;
    }
    private static int nameCompare(PhotoInfo a,PhotoInfo b) {
        String na = (a.getSubFolder() + "\\" + nameWithoutExt(a.getFileName())).toLowerCase();
        String nb = (b.getSubFolder() + "\\" + nameWithoutExt(b.getFileName())).toLowerCase();

        if (na.length()>nb.length() && na.startsWith(nb)) return -1;
        else if (nb.length()>na.length() && nb.startsWith(na)) return 1;
        else {
            na = (a.getSubFolder() + "\\" + a.getFileName()).toLowerCase();
            nb = (b.getSubFolder() + "\\" + b.getFileName()).toLowerCase();
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
        System.out.println("向 "+af.getAbsolutePath()+" 写入数据");
        saveObj(af, infos);
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
}
