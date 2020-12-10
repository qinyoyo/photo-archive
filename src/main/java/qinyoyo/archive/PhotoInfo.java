package qinyoyo.archive;

import qinyoyo.Utils;
import qinyoyo.exiftool.Key;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;


public class PhotoInfo implements Serializable,Cloneable {
    private String folder;
    private String fileName;
    private long   fileSize;

    private String mimeType;

    private Date   shootTime;
    private Date   createTime;
    private String make;
    private String model;
    private String lens;
    private String digest;

    private String country;
    private String province;
    private String city;
    private String location;
    private String subLocation;  // 地标位置
    private String scene;        // 场景代码，用于标注照片分类, landscape，portrait，group等
    private String artist;
    private Integer rating;      // 星级

    private String headline;
    private String subTitle;     // 题注

    private Double longitude;
    private Double latitude;
    private Double altitude;

    public PhotoInfo cloneObject() throws CloneNotSupportedException {
       return (PhotoInfo)super.clone();
    }

    public  PhotoInfo(String rootPath, File file) {
        if (!file.exists()) throw new RuntimeException(file.getAbsolutePath() + " 不存在");
        String dir = file.getParent();
        if (!dir.startsWith(rootPath)) throw new RuntimeException(rootPath + " 不包含 " + file.getAbsolutePath());
        folder = dir.substring(rootPath.length());
        if (folder.startsWith("/") || folder.startsWith("\\")) folder = folder.substring(1);
        fileName = file.getName();
        fileSize = file.length();

    }
    private boolean emptyValue(Object v) {
        return v==null || v.toString().isEmpty() || v.toString().equals("-");
    }
    public void setPropertiesBy(Map<Key, Object> attrs) {
        if (attrs==null) return;
        for (Key k : attrs.keySet()) {
            Object v=attrs.get(k);
            if (emptyValue(v)) continue;
            String s = v.toString();
            Date dt=null;
            try {
                switch (k) {
                    case SUBSECDATETIMEORIGINAL:
                        if (!s.startsWith("0000")) dt = Utils.string2Date(v.toString());
                        if (dt!=null) shootTime=dt;
                        break;
                    case DATETIMEORIGINAL:
                        if (shootTime==null) {
                            if (!s.startsWith("0000")) dt = Utils.string2Date(v.toString());
                            if (dt != null) shootTime = dt;
                        }
                        break;
                    case CREATEDATE:
                        if (!s.startsWith("0000")) createTime = Utils.string2Date(v.toString());
                        break;
                    case IPTCDigest:
                        digest = s;
                        break;
                    case MAKE:
                        make = s;
                        break;
                    case MODEL:
                        model = s;
                        break;
                    case LENS_ID:
                        lens = s;
                        break;
                    case GPS_LONGITUDE:
                        longitude = Double.parseDouble(s);
                        break;
                    case GPS_LATITUDE:
                        latitude = Double.parseDouble(s);
                        break;
                    case GPS_ALTITUDE:
                        String[] vv = s.split(" ");
                        altitude = Double.parseDouble(vv[0]);
                        if (v.toString().toLowerCase().contains("below")) altitude = -altitude;
                        break;
                    case MIME_TYPE:
                        mimeType = s;
                        break;
                    case ARTIST:
                        artist = s;
                        break;
                    case HEADLINE:
                        headline = s;
                        break;
                    case DESCRIPTION:
                        subTitle = s;
                        break;
                    case RATING:
                        rating = Integer.parseInt(s);
                        break;
                    case SCENE:
                        scene = s;
                        break;
                    case COUNTRY:
                        country = s;
                        break;
                    case STATE:
                        province = s;
                        break;
                    case CITY:
                        city = s;
                        break;
                    case LOCATION:
                        location = s;
                        break;
                    case SUB_LOCATION:
                        subLocation = s;
                        break;
                    default:
                }
            } catch (Exception e) {}
        }
    }

    public boolean exifEquals(PhotoInfo pi) {
        if (Utils.equals(shootTime,pi.shootTime)) {
            if (Utils.equals(createTime,pi.createTime)) {
                if (Utils.equals(make,pi.make)) {
                    if (Utils.equals(model,pi.model)) {
                        if (Utils.equals(lens,pi.lens)) {
                            if (Utils.equals(digest,pi.digest)) return true;
                            else return false;
                        } else return false;
                    } else return false;
                } else return false;
            } else return false;
        } else return false;
    }
    public  boolean absuluteSameAs(PhotoInfo pi) {
        return (Utils.equals(fileName, pi.fileName) && (fileSize == pi.fileSize) && exifEquals(pi)) ;
    }
    public  boolean sameAs(PhotoInfo pi) {
        if (this == pi) return true;
        if (Utils.equals(fileName, pi.fileName) && (fileSize == pi.fileSize)) return true; // 文件名和大小一样，可能其中一个删除了exif信息
        if (!Utils.extName(fileName).toLowerCase().equals(Utils.extName(pi.getFileName()).toLowerCase())) return false; // 文件扩展名不同，不一致
        return exifEquals(pi);
    }

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

    public Date getShootTime() {
        return shootTime;
    }
    public void setShootTime(Date shootTime) {
        this.shootTime = shootTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLens() {
        return lens;
    }

    public void setLens(String lens) {
        this.lens = lens;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSubLocation() {
        return subLocation;
    }

    public void setSubLocation(String subLocation) {
        this.subLocation = subLocation;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }
}
