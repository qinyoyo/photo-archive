package qinyoyo.photoinfo.archive;

import lombok.Getter;
import lombok.Setter;
import qinyoyo.photoinfo.ArchiveUtils;
import qinyoyo.photoinfo.exiftool.ExifTool;
import qinyoyo.photoinfo.exiftool.Key;
import qinyoyo.utils.*;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class PhotoInfo implements Serializable,Cloneable {
    private String subFolder; 
    private String fileName;
    private long   fileSize;
    private long   lastModified;

    private String mimeType;

    private Date   shootTime;
    private Date   createTime;
    private String model;
    private String lens;
    private String digest;
    private String documentId;

    private String country;
    private String countryCode;
    private String province;
    private String city;
    private String location;
    private String subjectCode;  // IPTC 主题代码，用以记录 step poi
    private String scene;        // 场景代码，用于标注照片分类, landscape，portrait，group等
    private String artist;
    private Integer rating;      // 星级
    private Integer width;
    private Integer height;
    private Integer orientation;

    private String headline;
    private String subTitle;     // 题注

    private Double longitude;
    private Double latitude;
    private Double altitude;
    private Date   gpsDatetime;

    public static final Map<String,Key> FIELD_TAG = new HashMap<String,Key>() {{
        put("mimeType",Key.MIME_TYPE);
        put("shootTime",Key.DATETIMEORIGINAL);
        put("createTime",Key.CREATEDATE);
        put("model",Key.MODEL);
        put("lens",Key.LENS_ID);
        put("digest",Key.IPTCDigest);
        put("documentId",Key.DOCUMENT_ID);
        put("country",Key.COUNTRY);
        put("countryCode",Key.COUNTRY_CODE);
        put("province",Key.STATE);
        put("city",Key.CITY);
        put("location",Key.LOCATION);
        put("subjectCode",Key.SUBJECT_CODE);
        put("scene",Key.SCENE);
        put("artist",Key.ARTIST);
        put("rating",Key.RATING.RATING);
        put("width",Key.IMAGE_WIDTH);
        put("height",Key.IMAGE_HEIGHT);
        put("orientation",Key.ORIENTATION);
        put("headline",Key.HEADLINE);
        put("subTitle",Key.DESCRIPTION);
        put("longitude",Key.GPS_LONGITUDE);
        put("latitude",Key.GPS_LATITUDE);
        put("altitude",Key.GPS_ALTITUDE);
        put("gpsDatetime",Key.GPS_DATETIME);
    }};
    public void setFieldByTag(Key tag,Object value) {
        for (String fieldName : FIELD_TAG.keySet()) {
            if (FIELD_TAG.get(fieldName).equals(tag)) {
                try {
                    Class<?> clazz = this.getClass();
                    Field field = clazz.getDeclaredField(fieldName);
                    if (value==null || field.getType().isAssignableFrom(value.getClass())) {
                        field.setAccessible(true);
                        field.set(this, value);
                        return;
                    } else {
                        Object v = Key.parse(tag, value.toString());
                        field.setAccessible(true);
                        field.set(this, v);
                        return;
                    }
                } catch (Exception e) {
                    Util.printStackTrace(e);
                }
                return;
            }
        }
    }
    public Object getFieldByTag(Key tag) {
        for (String field : FIELD_TAG.keySet()) {
            if (FIELD_TAG.get(field).equals(tag)) {
                try {
                    return Util.getPrivateField(this,field);
                } catch (Exception e) {
                    Util.printStackTrace(e);
                }
                return null;
            }
        }
        return null;
    }
    String lengString(long l) {
        String s = String.valueOf(l);
        String r = "";
        for (int i=0; i<s.length();i++) {
            if (i%3==0 && i>0) r = ',' + r;
            r=s.charAt(s.length()-1-i) + r;
        }
        return r;
    }
    /* some functions used in ftl resource file */
    public String gpsString(double v, boolean isLatitude) {
        double d=Math.abs(v);
        String r="";
        int du = (int) d;
        int fen = (int) ((d - (double) du) * 60.0);
        double miao = (d - (double) du - (double) fen / 60.0) * 3600.0;
        r = du + "°" + fen + "′" + String.format("%.3f", miao) + "″";
        if (isLatitude) {
            return (v>=0.0?"N":"S")+r;
        }  return (v>=0.0?"E":"W")+r;
    }
    // this,与 currentPath均为 归档目录下的path，不包含归档目录名，为相对目录
    public String urlPath(String currentPath) {
        String sub = getSubFolder();
        if (sub == null || sub.isEmpty()) {
            return '/'+getFileName();
        } else {
            int uplevel = 0;
            String url='/'+sub.replaceAll("\\\\","/") + '/' + getFileName();
            currentPath = currentPath.replaceAll("\\\\","/");
            if (currentPath.isEmpty() || currentPath.equals('/')) return url;
            if (!currentPath.startsWith("/")) currentPath = "/" + currentPath;
            if (!currentPath.endsWith("/")) currentPath = currentPath + '/';
            while (!url.startsWith(currentPath)) {
                uplevel ++;
                int pos = currentPath.substring(0,currentPath.length()-1).lastIndexOf("/");
                currentPath = currentPath.substring(0,pos+1);
            }
            url = url.substring(currentPath.length());
            while (uplevel-- > 0) url = "../"+url;
            return url;
        }
    }
    public String formattedAddress(boolean useCountryName) {
        String poi = subjectCode;
        if (poi==null || poi.isEmpty()) poi=ArchiveUtils.poiFromPath(subFolder);
        return TimeZoneTable.formatAddress(useCountryName ? country : "",province,city,location,poi);
    }
    @Override
    public String toString() {
        Map<String,Object> attributes = new LinkedHashMap<>();
        if (rating!=null && rating>0) {
            String s = "";
            for (int i=0;i<rating;i++) s += "★";
            attributes.put("rating",s);
        }

        attributes.put("file",subFolder + File.separator + fileName);

        if (shootTime!=null) attributes.put("createTime", DateUtil.date2String(shootTime));
        else if (createTime!=null) attributes.put("createTime",DateUtil.date2String(createTime));

        if (headline!=null && !headline.isEmpty()) attributes.put("title",headline);

        if (subTitle!=null && !subTitle.isEmpty()) attributes.put("subTitle",subTitle);

        if (width!=null) attributes.put("width",width);
        if (height!=null) attributes.put("height",height);
        attributes.put("fileSize",lengString(fileSize));

        if (orientation!=null) attributes.put("orientation",Orientation.name(orientation));
        if (artist!=null && !artist.isEmpty()) attributes.put("artist",artist);

        if (model!=null && !model.isEmpty()) {
            String s = model;
            if (lens!=null && !lens.isEmpty()) s += (" - " + lens);
            attributes.put("device",s);
        }

        if (subjectCode!=null && !subjectCode.isEmpty()) attributes.put("poi",subjectCode);

        if (country!=null) attributes.put("country",country);
        if (province!=null) attributes.put("province",province);
        if (city!=null) attributes.put("city",city);
        if (location!=null) attributes.put("location",location);

        if (longitude!=null) attributes.put("longitude",longitude);
        if (latitude!=null) attributes.put("latitude",latitude);
        if (altitude!=null) attributes.put("altitude",Math.round(altitude));

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0, size = attributes.size();
        for (String k : attributes.keySet()) {
            sb.append("\"").append(k).append("\"").append(": ");
            Object o = attributes.get(k);
            if (o instanceof String) sb.append("\"").append(o).append("\"");
            else if (o instanceof Double) sb.append(String.format("%.7f",(Double)o));
            else sb.append(o.toString());
            if (i < size-1) sb.append(",\n");
        }
        sb.append("}");
        return sb.toString();
    }
    public PhotoInfo cloneObject() throws CloneNotSupportedException {
       return (PhotoInfo)super.clone();
    }
    public static final String RENAME_PATTERN = "%y%M%d-%h%m%s_%u={p}%%E";
    private String pathProperty(String rootPath) {
        return ArchiveUtils.poiFromPath(subFolder);
    }
    private String fileProperty() {
        int pos = fileName.lastIndexOf(".");
        String f = (pos>=0?fileName.substring(0,pos) : fileName);
        String nf="";
        for (int i=0;i<f.length();i++) {
            char ch=f.charAt(i);
            if ((ch>='0' && ch<='9') || ch=='-' || ch=='_') continue;
            else nf=nf+String.valueOf(ch);
        }
        return nf;
    }
    private static final String VALID_TYPE = "yMdhmsolucpf";

    private String getNameStringBy(String rootPath, char type) {

        switch (type) {
            case 'y': return DateUtil.date2String(shootTime,"yyyy");
            case 'M': return DateUtil.date2String(shootTime,"MM");
            case 'd': return DateUtil.date2String(shootTime,"dd");
            case 'h': return DateUtil.date2String(shootTime,"HH");
            case 'm': return DateUtil.date2String(shootTime,"mm");
            case 's': return DateUtil.date2String(shootTime,"ss");

            case 'o': return model;
            case 'l': return location;
            case 'u': return subjectCode;
            case 'c': return scene;

            case 'p': return pathProperty(rootPath);
            case 'f': return fileProperty();
        }
        return "";
    }

    private String replacePatten(String rootPath, String name, int start, int end, char type) {
        String value = getNameStringBy(rootPath,type);
        Pattern p = Pattern.compile("%"+type+"=([^%]*)%");
        Matcher m = p.matcher(name);
        if (m.find() && m.start()==start) {
            if (value == null || value.trim().isEmpty()) {
                value = m.group(1);
                if (value.matches("\\{([" + VALID_TYPE + "])\\}")) value = getNameStringBy(rootPath, value.charAt(1));
                if (value == null) value = "";
            }
            return name.substring(0,m.start()) + value + name.substring(m.end());
        } else {
            if (value==null || value.trim().isEmpty()) value = "";
            return name.substring(0,start) + value + name.substring(end);
        }
    }

    public boolean rename(String rootPath, String namePat) throws  Exception {
        String newName = namePat;
        if (newName == null) newName = RENAME_PATTERN;

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
        Pattern p = Pattern.compile("%([" +VALID_TYPE + "])");
        Matcher m = p.matcher(newName);
        while (m.find()) {
            newName =  replacePatten(rootPath, newName, m.start(), m.end(), m.group(1).charAt(0));
            m = p.matcher(newName);
        }

        File dir = new File(rootPath,subFolder);
        File file1=new File(dir,fileName);
        if (!file1.exists()) return false;
        File thumb = new File(fullThumbPath(rootPath));
        File file2=new File(dir,newName+ext);
        if (file2.compareTo(file1)==0) return false;
        pos = 0;
        while (file2.exists()) {
            pos++;
            file2=new File(dir,newName+"_"+String.format("%03d",pos)+ext);
        }
        file1.renameTo(file2);
        fileName = file2.getName();
        if (thumb.exists()) thumb.renameTo(new File(fullThumbPath(rootPath)));
        return true;
    }
    public void getFileProperties(String root) {
        getFileProperties(new File(fullPath(root)));
    }
    public void getFileProperties(File file) {
        if (file!=null && file.exists()) {
            fileName = file.getName();
            fileSize = file.length();
            lastModified = file.lastModified();
        }
    }

    public void setFile(String rootPath, File file) throws Exception {
        String dir = file.getParentFile().getCanonicalPath();
        if (!dir.startsWith(rootPath)) throw new RuntimeException(rootPath + " 不包含 " + file.getAbsolutePath());
        subFolder = dir.substring(rootPath.length());
        if (subFolder.startsWith("/") || subFolder.startsWith("\\")) subFolder = subFolder.substring(1);
        getFileProperties(file);

        if (ArchiveUtils.isInWebFolder(subFolder) && fileName.equals("index.html")) {
            setMimeType("text/html");
            String subTitle = subFolder.substring(0,subFolder.length()-4);
            int pos = subTitle.lastIndexOf(File.separator);
            setSubTitle(pos>=0 ? subTitle.substring(pos+1) : subTitle);
            ArchiveUtils.formatStepHtml(null,file);
        }
    }
    // 用于接受post数据
    public PhotoInfo() {
    }
    // 不读取exif信息，便于快速读取
    public  PhotoInfo(String rootPath, File file) {
        try {
            rootPath = new File(rootPath).getCanonicalPath();
            if (!file.exists()) throw new RuntimeException(file.getAbsolutePath() + " 不存在");
            setFile(rootPath,file);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public void setPropertiesBy(Map<Key, Object> attrs) {
        if (attrs==null) return;
        for (Key k : attrs.keySet()) {
            Object v = attrs.get(k);
            setFieldByTag(k, v);
        }
        Object extObj = attrs.get(Key.SUB_SEC_TIME_ORIGINAL);
        if (extObj!=null && shootTime!=null) {
           int ms = Integer.parseInt(extObj.toString());
           shootTime.setTime(shootTime.getTime() + ms);
        }
        extObj = attrs.get(Key.SUB_SEC_TIME_CREATE);
        if (extObj!=null && createTime!=null) {
            int ms = Integer.parseInt(extObj.toString());
            createTime.setTime(createTime.getTime() + ms);
        }
        extObj = attrs.get(Key.GPS_LONGITUDE_REF);
        if (extObj!=null && longitude!=null) {
            boolean west = extObj.toString().toLowerCase().startsWith("w");
            if ((west && longitude > 0) || (!west && longitude < 0)) longitude = -longitude;
        }
        extObj = attrs.get(Key.GPS_LATITUDE_REF);
        if (extObj!=null && latitude!=null) {
            boolean south = extObj.toString().toLowerCase().startsWith("s");
            if ((south && latitude > 0) || (!south && latitude < 0)) latitude = -latitude;
        }
        extObj = attrs.get(Key.GPS_ALTITUDE_REF);
        if (extObj!=null && altitude!=null) {
            boolean below = ((Integer)extObj) == 1;
            altitude = below ? -Math.abs(altitude) : Math.abs(altitude);
        }
    }

    public void readProperties(String rootPath) {
        File f = new File(fullPath(rootPath));
        if (f.exists() && SupportFileType.isSupport(f.getName())) {
            try {
                setFile(rootPath,f);
                Map<String, Map<Key, Object>>  fileInfos = ExifTool.getInstance().query(f,  ArchiveUtils.NEED_KEYS);
                if (fileInfos!=null) {
                    setPropertiesBy(fileInfos.get(f.getName()));
                    if (getShootTime()==null && getCreateTime()!=null && getMimeType()!=null && !getMimeType().toLowerCase().startsWith("image"))
                        setShootTime(getCreateTime());
                    if (getShootTime()==null) {
                        setShootTime(DateUtil.getShootTimeFromFileName(getFileName()));
                        if (getShootTime()!=null) {
                            ExifTool.getInstance().execute(f, "-overwrite_original",
                                    "-DateTimeOriginal="+DateUtil.date2String(getShootTime()));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static boolean isEmpty(String s) {
        return s==null || s.trim().isEmpty();
    }
    private boolean isExifEmpty() {
        return shootTime==null && createTime==null && isEmpty(model) && isEmpty(lens) && isEmpty(documentId);
        // 网络图片 digest 很多一样，不可靠
    }
    private static boolean same(Date s1, Date s2) {
        if (s1 == null && s2 == null)
            return true;
        else if (s1 != null && s2 != null)
            return s1.getTime() == s2.getTime();
        else
            return false;
    }
    private static boolean same(String s1, String s2) {
        if (s1 == null && s2 == null)
            return true;
        else if (s1 != null && s2 != null)
            return s1.toLowerCase().equals(s2.toLowerCase());
        else
            return false;
    }
    private static String extName(String name) {
        int p = name.lastIndexOf(".");
        if (p >= 0)
            return name.substring(p);
        else
            return "";
    }
    boolean allNull(Object ...objs) {
        for (Object o:objs) if (o!=null) return false;
        return true;
    }
    public boolean exifEquals(PhotoInfo pi) {
        if (same(shootTime,pi.shootTime)) {
            if (same(createTime,pi.createTime)) {
                if (same(model,pi.model)) {
                    if (same(lens,pi.lens)) {
                        if (!allNull(shootTime,createTime) && !allNull(model,lens)) return true;
                        if (same(digest,pi.digest)) {
                            if (same(documentId,pi.documentId)) return true;
                            else return false;
                        } else return false;
                    } else return false;
                } else return false;
            } else return false;
        } else return false;
    }
    public  boolean absoluteSameAs(PhotoInfo pi) {
        if (isExifEmpty() && pi.isExifEmpty()) return same(fileName, pi.fileName) && (fileSize == pi.fileSize);
        else return (same(fileName, pi.fileName) && (fileSize == pi.fileSize) && exifEquals(pi)) ;
    }
    public  boolean sameAs(PhotoInfo pi) throws Exception {
        if (this == pi) return true;
        if (same(fileName, pi.fileName) && (fileSize == pi.fileSize)) return true; // 文件名和大小一样，可能其中一个删除了exif信息
        if (!extName(fileName).toLowerCase().equals(extName(pi.getFileName()).toLowerCase())) return false; // 文件扩展名不同，不一致
        if (isExifEmpty() && pi.isExifEmpty()) {
            if (!same(digest,pi.digest)) return false;
            else if (fileSize == pi.fileSize) throw new Exception("unknown");  // 不可知
            else return false;
        }
        else return exifEquals(pi);
    }
    public String fullPath(String root) {
        try {
            String sub = getSubFolder();
            if (sub == null || sub.isEmpty()) {
                return new File(root, getFileName()).getCanonicalPath();
            } else
                return new File(new File(root, sub), getFileName()).getCanonicalPath();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String fullThumbPath(String root) {
        if (mimeType==null || (!mimeType.contains("image/") && !mimeType.contains("video/"))) return null;
        String sub = getSubFolder();
        if (sub == null || sub.isEmpty()) sub =ArchiveUtils.THUMB;
        else {
            if (sub.startsWith(ArchiveUtils.DELETED+File.separator)) sub=sub.substring(8);
            sub = ArchiveUtils.THUMB+File.separator + sub;
        }
        try {
            return new File(new File(root, sub), getFileName()).getCanonicalPath() + (mimeType.contains("video/") ? ".jpg" : "");
        } catch (Exception e) {
            return null;
        }
        }

    public boolean modifyOrientation(String root, Integer newRating, Integer ... operations) {
        if (mimeType==null || !mimeType.contains("image/")) return false;
        Integer newOrientation = null;
        if (operations!=null && operations.length>0) {
            Integer orientation0 = orientation;
            Integer orientation1=null;
            if (orientation0==null || orientation0==Orientation.NONE.getValue()) orientation1 = Orientation.by(operations);
            else {
                Integer [] ops = new Integer[operations.length+1];
                ops[0]=orientation0;
                for (int i=0;i<operations.length;i++) ops[i+1] = operations[i];
                orientation1 = Orientation.by(ops);
            }
            if (!Orientation.equals(orientation0,orientation1)) {
                newOrientation = orientation1;
            }
        }
        if (newRating==null && newOrientation==null) return false;
        String imgPath = fullPath(root);
        try {
            if (Orientation.setOrientationAndRating(new File(imgPath), newOrientation, newRating)) {
                if (newOrientation!=null) orientation = newOrientation;
                if (newRating!=null) rating = newRating;
                String thumbPath = fullThumbPath(root);
                if (thumbPath!=null && !Orientation.setOrientationAndRating(new File(thumbPath), newOrientation, newRating)) {
                    System.out.println("Orientation and rating of thumbnail error: "+thumbPath);
                }
                getFileProperties(new File(imgPath));
                return true;
            }
        } catch (Exception e){ Util.printStackTrace(e);}
        return false;
    }
    public boolean delete(String rootPath) {
        String thumbPath = fullThumbPath(rootPath);
        if (thumbPath!=null) new File(thumbPath).delete();
        return (new File(fullPath(rootPath)).delete());
    }

    private int nameCompare(PhotoInfo p) {
        int cp = getSubFolder().compareTo(p.getSubFolder());
        if (cp==0) return getFileName().compareTo(p.getFileName());
        else return cp;
    }
    public int compareTo(PhotoInfo p) {
        Date d1 = getShootTime(), d2 = p.getShootTime();
        if (d1==null) d1=getCreateTime();
        if (d2==null) d2=p.getCreateTime();
        if (d1==null && d2==null) return nameCompare(p);
        else if (d1==null)
            return -1;
        else if (d2==null)
            return 1;
        else if (d1.getTime() > d2.getTime())
            return 1;
        else if (d1.getTime() < d2.getTime())
            return -1;
        else return nameCompare(p);
    }

    public TimeZone getTimeZone() {
        String ctr = countryCode;
        if (ctr==null || ctr.trim().isEmpty()) ctr=country;
        TimeZone zone = TimeZoneTable.getTimeZone(ctr,province,city,longitude);
        if (zone!=null) return zone;
        if (shootTime!=null && gpsDatetime!=null) {
            String fmt = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            String s=sdf.format(shootTime);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                long delta = (sdf.parse(s).getTime() - gpsDatetime.getTime())/1000/60;
                long hour = delta / 60, minute = delta % 60;
                if (minute > 30) hour += (hour>=0?1:-1);
                if (hour>=-12 && hour<=12) {
                    return TimeZone.getTimeZone("GMT"+(hour>0?"+":"")+hour);
                }
            } catch (ParseException e) {
            }
        }
        return TimeZone.getDefault();
    }
    public PositionUtil.LatLng getPoint(String type) {
        if (latitude!=null && longitude!=null) {
            if (PositionUtil.BD09.equals(type)) return PositionUtil.wgs84ToBd09(latitude,longitude);
            else if (PositionUtil.GCJ02.equals(type)) return PositionUtil.wgs84ToGcj02(latitude,longitude);
            else return new PositionUtil.LatLng(latitude,longitude,PositionUtil.WGS84);
        } else return null;
    }
    public Map<String,String> getPointMap(String type) {
        Map<String,String> map = new HashMap<>();
        PositionUtil.LatLng latLng = getPoint(type);
        if (latLng!=null) {
            map.put("lng",String.format("%.7f",latLng.longitude));
            map.put("lat",String.format("%.7f",latLng.latitude));
        }
        return map;
    }
}
