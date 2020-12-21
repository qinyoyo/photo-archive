package qinyoyo.archive;

import qinyoyo.SystemOut;
import qinyoyo.Utils;
import qinyoyo.exiftool.Key;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PhotoInfo implements Serializable,Cloneable {
    private String subFolder; 
    private String fileName;
    private long   fileSize;

    private String mimeType;

    private Date   shootTime;
    private Date   createTime;
    private String make;
    private String model;
    private String lens;
    private String digest;
    private String documentId;

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

    private String pathProperty(String rootPath) {
        String [] dirs = new File(rootPath,subFolder).getAbsolutePath().split(File.separator.equals("\\")?"\\\\":File.separator);
        String value="";
        if (dirs!=null && dirs.length>0) {
            for (int i=dirs.length-1; i>=0; i--) {
                String d = dirs[i].toLowerCase();
                if (d.equals("l") || d.equals("p") || d.equals("g") || d.equals("jpg") || d.equals("raw") || d.equals("nef")) continue;
                if (d.equals("landscape") || d.equals("portrait") ) continue;
                if (d.equals("camera") || d.equals("video") || d.equals("mov") || d.equals("audio")
                        || d.equals("mp4") || d.equals("mp3") || d.equals("res") || d.equals("resource")) continue;
                if (d.equals("风景") || d.equals("景物") || d.equals("人物") || d.equals("人像")) continue;
                if (d.endsWith("生活")) {
                	value="U";
                	break;
                };
                d = dirs[i].trim();
                while (!d.isEmpty() && d.charAt(0)>='0' && d.charAt(0)<='9') d=d.substring(1);
                d=d.trim();
                if (!d.isEmpty()) {
                    value = d;
                    break;
                }
            }
        }
        return value;
    }
    private String  useDefaultPatten(String rootPath, String name, String type) {
        String value = ("l".equals(type) ? location :
                ("u".equals(type) ? subLocation :
                        ("c".equals(type) ? scene : "")));
        Pattern p = Pattern.compile("%"+type+"=([^%]*)%");
        Matcher m = p.matcher(name);
        if (m.find()) {
            if (value==null || value.trim().isEmpty()) value = m.group(1);
            if (value.equals("{p}")) value = pathProperty(rootPath);
            return name.substring(0,m.start()) + value + name.substring(m.end());
        } else {
            if (value==null || value.trim().isEmpty()) value = "";
            return name.replace("%"+type,value);
        }
    }
    public void rename(String rootPath, String namePat) throws  Exception {
        String newName = namePat;
        if (newName == null) newName = Utils.RENAME_PATTERN;
        if (namePat.contains("%y")) newName = newName.replace("%y", Utils.date2String(shootTime,"yyyy"));
        if (namePat.contains("%M")) newName = newName.replace("%M",Utils.date2String(shootTime,"MM"));
        if (namePat.contains("%d")) newName = newName.replace("%d",Utils.date2String(shootTime,"dd"));
        if (namePat.contains("%h")) newName = newName.replace("%h",Utils.date2String(shootTime,"HH"));
        if (namePat.contains("%m")) newName = newName.replace("%m",Utils.date2String(shootTime,"mm"));
        if (namePat.contains("%s")) newName = newName.replace("%s",Utils.date2String(shootTime,"ss"));

        if (namePat.contains("%p")) newName = newName.replace("%p",pathProperty(rootPath));
        if (namePat.contains("%o")) newName = newName.replace("%o",model==null?"":model);

        if (namePat.contains("%l")) newName = useDefaultPatten(rootPath, newName,"l");
        if (namePat.contains("%f")) {
        	int pos = fileName.lastIndexOf(".");
        	String f = (pos>=0?fileName.substring(0,pos) : fileName);
        	String nf="";
        	for (int i=0;i<f.length();i++) {
        		char ch=f.charAt(i);
        		if ((ch>='0' && ch<='9') || ch=='-' || ch=='_') continue;
        		else nf=nf+String.valueOf(ch);
        	}
        	newName = newName.replace("%f",nf);
        }
        if (namePat.contains("%u")) newName = useDefaultPatten(rootPath, newName,"u");
        if (namePat.contains("%c")) newName = useDefaultPatten(rootPath, newName,"c");

        int pos = fileName.lastIndexOf(".");
        String ext = (pos>=0 ? fileName.substring(pos) : "");
        if (namePat.contains("%E")) {
            newName = newName.replace("%E","");
            ext = ext.toUpperCase();
        }
        else if (namePat.contains("%e")) {
            newName = newName.replace("%e","");
            ext = ext.toLowerCase();
        }
        if (fileName.toLowerCase().indexOf(newName.toLowerCase())==0) return;
        File dir = new File(rootPath,subFolder);
        File file1=new File(dir,fileName);
        if (!file1.exists()) return;
        File file2=new File(dir,newName+ext);
        pos = 0;
        while (file2.exists()) {
            pos++;
            file2=new File(dir,newName+pos+ext);
        }
        file2.getCanonicalPath();
        file1.renameTo(file2);
        fileName = file2.getName();
        
        if (file1.getAbsolutePath().startsWith("E:\\")) {
			file1=new File("H"+file1.getAbsolutePath().substring(1));
			file2=new File("H"+file2.getAbsolutePath().substring(1));
			file1.renameTo(file2);
        }
        
    }
    public  PhotoInfo(String rootPath, File file) {
        try {
            rootPath = new File(rootPath).getCanonicalPath();
            if (!file.exists()) throw new RuntimeException(file.getAbsolutePath() + " 不存在");
            String dir = file.getParentFile().getCanonicalPath();
            if (!dir.startsWith(rootPath)) throw new RuntimeException(rootPath + " 不包含 " + file.getAbsolutePath());
            subFolder = dir.substring(rootPath.length());
            if (subFolder.startsWith("/") || subFolder.startsWith("\\")) subFolder = subFolder.substring(1);
            fileName = file.getName();
            fileSize = file.length();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
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
                    case DOCUMENT_ID:
                        documentId = s;
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
        if (shootTime==null && createTime!=null && mimeType!=null && !mimeType.toLowerCase().startsWith("image")) shootTime = createTime;
        if (shootTime==null) shootTime = Utils.getShootTimeFromFileName(fileName);
    }

    public static boolean isEmpty(String s) {
        return s==null || s.trim().isEmpty();
    }
    public boolean isExifEmpty() {
        return shootTime==null && createTime==null && isEmpty(make) && isEmpty(model) && isEmpty(lens) && isEmpty(digest) && isEmpty(documentId);
    }
    public boolean exifEquals(PhotoInfo pi) {
        if (Utils.equals(shootTime,pi.shootTime)) {
            if (Utils.equals(createTime,pi.createTime)) {
                if (Utils.equals(make,pi.make)) {
                    if (Utils.equals(model,pi.model)) {
                        if (Utils.equals(lens,pi.lens)) {
                            if (Utils.equals(digest,pi.digest)) {
                                if (Utils.equals(documentId,pi.documentId)) return true;
                                else return false;
                            } else return false;
                        } else return false;
                    } else return false;
                } else return false;
            } else return false;
        } else return false;
    }
    public  boolean absoluteSameAs(PhotoInfo pi) {
        if (isExifEmpty() && pi.isExifEmpty()) return Utils.equals(fileName, pi.fileName) && (fileSize == pi.fileSize);
        else return (Utils.equals(fileName, pi.fileName) && (fileSize == pi.fileSize) && exifEquals(pi)) ;
    }
    public  boolean sameAs(PhotoInfo pi) {
        if (this == pi) return true;
        if (Utils.equals(fileName, pi.fileName) && (fileSize == pi.fileSize)) return true; // 文件名和大小一样，可能其中一个删除了exif信息
        if (!Utils.extName(fileName).toLowerCase().equals(Utils.extName(pi.getFileName()).toLowerCase())) return false; // 文件扩展名不同，不一致
        if (isExifEmpty() && pi.isExifEmpty()) return false;
        else return exifEquals(pi);
    }

    public String getSubFolder() {
        return subFolder;
    }
    public void setSubFolder(String folder) {
        this.subFolder = folder;
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
