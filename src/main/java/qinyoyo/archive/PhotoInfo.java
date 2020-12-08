package qinyoyo.archive;

import qinyoyo.Utils;
import qinyoyo.exiftool.ExifTool;
import qinyoyo.exiftool.Key;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;


public class PhotoInfo implements Serializable {
    private String folder;
    private String fileName;
    private long   fileSize;
    private Date   lastModified;
    private Date   shootTime;
    private String camera;
    private Double longitude;
    private Double latitude;
    private Double altitude;

    public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public Date getShootTime() {
		return shootTime;
	}
	public void setShootTime(Date shootTime) {
		this.shootTime = shootTime;
	}
	public String getCamera() {
		return camera;
	}
	public void setCamera(String camera) {
		this.camera = camera;
	}

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }


    public  PhotoInfo(String rootPath, File file) {
        if (!file.exists()) throw new RuntimeException(file.getAbsolutePath() + " 不存在");
        String dir = file.getParent();
        if (!dir.startsWith(rootPath)) throw new RuntimeException(rootPath + " 不包含 " + file.getAbsolutePath());
        folder = dir.substring(rootPath.length());
        if (folder.startsWith("/") || folder.startsWith("\\")) folder = folder.substring(1);
        fileName = file.getName();
        fileSize = file.length();
        lastModified = new Date(file.lastModified());
    }
    public void setPropertiesBy(Map<Key, Object> attrs) {
        if (attrs!=null) {
            Object v = attrs.get(Key.SUBSECDATETIMEORIGINAL);
            if (v != null && !v.toString().equals("-") && !v.toString().startsWith("0000")) shootTime = Utils.string2Date(v.toString());
            if (shootTime==null) {
                v = attrs.get(Key.DATETIMEORIGINAL);
                if (v != null && !v.toString().equals("-") && !v.toString().startsWith("0000")) shootTime = Utils.string2Date(v.toString());
                v = attrs.get(Key.CREATEDATE);
                Date createDate=null;
                if (v != null && !v.toString().equals("-") && !v.toString().startsWith("0000")) createDate = Utils.string2Date(v.toString());
                if (shootTime==null) shootTime = createDate;
                else if (createDate!=null && createDate.getTime()<shootTime.getTime()) shootTime = createDate;
                if (shootTime==null) shootTime = Utils.getShootTimeFromFileName(fileName);
            }
            v = attrs.get(Key.MAKE);
            if (v != null && !v.toString().equals("-")) camera = v.toString();
            else camera = "";
            v = attrs.get(Key.MODEL);
            if (v != null&& !v.toString().equals("-")) camera = camera + v.toString();
            v = attrs.get(Key.LENS_ID);
            if (v != null && !v.toString().equals("-")) camera = camera + v.toString();
            if (camera.isEmpty()) camera=null;

            v = attrs.get(Key.GPS_LONGITUDE);
            if (v != null && v instanceof Double) longitude = (Double) v;
            v = attrs.get(Key.GPS_LATITUDE);
            if (v != null && v instanceof Double) latitude = (Double) v;
            v = attrs.get(Key.GPS_ALTITUDE);
            if (v != null) {
                try {
                    String[] vv = v.toString().split(" ");
                    altitude = Double.parseDouble(vv[0]);
                    if (!v.toString().toLowerCase().contains("above")) altitude = -altitude;
                } catch (Exception e) {}
            }

        }
    }
    public  PhotoInfo(ExifTool exifTool, String rootPath, File file) throws Exception {
        this(rootPath,file);
        Map<Key, Object> attrs = exifTool.query(file, Key.DATETIMEORIGINAL, Key.MAKE, Key.MODEL, Key.LENS_ID);
        setPropertiesBy(attrs);
    }
    public  boolean absuluteSameAs(PhotoInfo pi) {
        return (Utils.equals(fileName, pi.fileName) && (fileSize == pi.fileSize)
                && shootTime!=null && Utils.equals(shootTime,pi.shootTime)
                && camera!=null && !camera.isEmpty() && Utils.equals(camera, pi.camera) ) ;
    }
    public  boolean sameAs(PhotoInfo pi) {
        if (this == pi) return true;
        if (Utils.equals(fileName, pi.fileName) && (fileSize == pi.fileSize)) return true; // 文件名和大小一样，可能其中一个删除了exif信息
        if (!Utils.extName(fileName).toLowerCase().equals(Utils.extName(pi.getFileName()).toLowerCase())) return false; // 文件扩展名不同，不一致
        if (!Utils.equals(shootTime,pi.shootTime)) return false; // 拍摄日期不同
        if (!Utils.equals(camera, pi.camera)) return false; // 拍摄设备信息不同
        if (shootTime==null || camera==null) return Utils.equals(fileName, pi.fileName); // 没有拍摄日期或设备，判断文件名
        else return true; // 拍摄日期和设备一样且不为空
    }
}
