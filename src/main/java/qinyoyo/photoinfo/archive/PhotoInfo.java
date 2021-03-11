package qinyoyo.photoinfo.archive;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;
import qinyoyo.photoinfo.ArchiveUtils;
import qinyoyo.photoinfo.exiftool.ExifTool;
import qinyoyo.photoinfo.exiftool.Key;
import qinyoyo.utils.DateUtil;
import qinyoyo.utils.FileUtil;
import qinyoyo.utils.StepHtmlUtil;
import qinyoyo.utils.Util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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
        if (allNull(country,province,city,location)) return poi==null ? "" : poi;
        boolean cc = ArchiveUtils.hasChinese(country) || ArchiveUtils.hasChinese(province) ||ArchiveUtils.hasChinese(city) ||ArchiveUtils.hasChinese(location);
        if (cc) {
            String address = ArchiveUtils.join(null,useCountryName ? country : "", province, ArchiveUtils.equals(province,city)?"":city, location);
            if (poi!=null && !poi.isEmpty() && !address.contains(poi)) return address.isEmpty() ? poi : address + "," + poi;
            else return address;
        } else {
            String address = ArchiveUtils.join(",",location, city, ArchiveUtils.equals(province,city)?"":province, useCountryName ? country : "");
            if (poi!=null && !poi.isEmpty() && !address.contains(poi)) return address.isEmpty() ? poi : poi + "," + address;
            else return address;
        }
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
            else if (o instanceof Double) sb.append(String.format("%.6f",(Double)o));
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

    public void rename(String rootPath, String namePat) throws  Exception {
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
    private boolean emptyValue(Object v) {
        return v==null || v.toString().isEmpty() || v.toString().equals("-");
    }
    public void setPropertiesBy(Map<String, Object> attrs) {
        if (attrs==null) return;
        for (String k : attrs.keySet()) {
            Object v=attrs.get(k);
            String s = emptyValue(v)?null : v.toString();
            Date dt=null;
            try {
                if (k.equals(Key.getName(Key.DATETIMEORIGINAL))) {
                    if (s==null) shootTime=null;
                    else if (!s.startsWith("0000")) {
                        Object subSec = attrs.get(Key.SUB_SEC_TIME_ORIGINAL);
                        shootTime = DateUtil.string2Date(v.toString()+(
                                    emptyValue(subSec)?"":"."+(subSec.toString()+"00").substring(0,3)
                            ));
                    }
                }
                else if (k.equals(Key.getName(Key.CREATEDATE))) {
                    if (s==null) createTime=null;
                    else if (!s.startsWith("0000")) {
                        Object subSec = attrs.get(Key.SUB_SEC_TIME_CREATE);
                        createTime = DateUtil.string2Date(v.toString()+(
                                emptyValue(subSec)?"":"."+(subSec.toString()+"00").substring(0,3)
                                ));
                    }
                }
                else if (k.equals(Key.getName(Key.IMAGE_WIDTH))) {
                    if (s==null) width=null;
                    else width = Integer.parseInt(s);
                }
                else if (k.equals(Key.getName(Key.IMAGE_HEIGHT))) {
                    if (s==null) height=null;
                    else height = Integer.parseInt(s);
                }
                else if (k.equals(Key.getName(Key.DOCUMENT_ID))) {
                    documentId = s;
                }
                else if (k.equals(Key.getName(Key.IPTCDigest))) {
                    digest = s;
                }
                else if (k.equals(Key.getName(Key.MODEL))) {
                    model = s;
                }
                else if (k.equals(Key.getName(Key.LENS_ID))) {
                    lens = s;
                }
                else if (k.equals(Key.getName(Key.GPS_LONGITUDE))) {
                    longitude=Modification.fromDFM(s);
                }
                else if (k.equals(Key.getName(Key.GPS_LATITUDE))) {
                    latitude=Modification.fromDFM(s);
                }
                else if (k.equals(Key.getName(Key.GPS_ALTITUDE))) {
                    if (s==null) latitude=null;
                    else {
                        String[] vv = s.split(" ");
                        altitude = Double.parseDouble(vv[0]);
                        if (v.toString().toLowerCase().contains("below")) altitude = -altitude;
                        else {
                            Object ref = attrs.get(Key.getName(Key.GPS_ALTITUDE_REF));
                            if (ref!=null && ref.toString().equals("0")) altitude = -altitude;
                        }
                    }
                }
                else if (k.equals(Key.getName(Key.MIME_TYPE))) {
                    mimeType = s;
                }
                else if (k.equals(Key.getName(Key.ARTIST))) {
                    artist = s;
                }
                else if (k.equals(Key.getName(Key.HEADLINE))) {
                    headline = s;
                }
                else if (k.equals(Key.getName(Key.DESCRIPTION))) {
                    subTitle = s;
                }
                else if (k.equals(Key.getName(Key.RATING))) {
                    if (s==null) rating=null;
                    else  rating = Integer.parseInt(s);
                }
                else if (k.equals(Key.getName(Key.ORIENTATION))) {
                    if (s==null) rating=null;
                    else orientation = Orientation.value(s);
                }
                else if (k.equals(Key.getName(Key.SCENE))) {
                    scene = s;
                }
                else if (k.equals(Key.getName(Key.COUNTRY))) {
                    country = s;
                }
                else if (k.equals(Key.getName(Key.STATE))) {
                    province = s;
                }
                else if (k.equals(Key.getName(Key.CITY))) {
                    city = s;
                }
                else if (k.equals(Key.getName(Key.LOCATION))) {
                    location = s;
                }
                else if (k.equals(Key.getName(Key.SUBJECT_CODE))) {
                    subjectCode = s;
                }
            } catch (Exception e){ Util.printStackTrace(e);}
        }
    }

    public void readProperties(String rootPath) {
        File f = new File(fullPath(rootPath));
        if (f.exists() && SupportFileType.isSupport(f.getName())) {
            try {
                setFile(rootPath,f);
                Map<String, Map<String, Object>>  fileInfos = ExifTool.getInstance().query(f, null, ArchiveUtils.NEED_KEYS);
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

    public String fullThumbPath(String root) throws IOException {
        if (mimeType==null || (!mimeType.contains("image/") && !mimeType.contains("video/"))) throw new IOException("not supported type");
        String sub = getSubFolder();
        if (sub == null || sub.isEmpty()) sub =ArchiveUtils.THUMB;
        else {
            if (sub.startsWith(ArchiveUtils.DELETED+File.separator)) sub=sub.substring(8);
            sub = ArchiveUtils.THUMB+File.separator + sub;
        }
        return new File(new File(root, sub), getFileName()).getCanonicalPath() + (mimeType.contains("video/") ? ".jpg" : "");
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
                if (!Orientation.setOrientationAndRating(new File(thumbPath), newOrientation, newRating)) {
                    System.out.println("Orientation and rating of thumbnail error: "+thumbPath);
                }
                getFileProperties(new File(imgPath));
                return true;
            }
        } catch (Exception e){ Util.printStackTrace(e);}
        return false;
    }
    public boolean delete(String rootPath) {
        try {
            new File(fullThumbPath(rootPath)).delete();
        } catch (Exception e){ Util.printStackTrace(e);}
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
}
