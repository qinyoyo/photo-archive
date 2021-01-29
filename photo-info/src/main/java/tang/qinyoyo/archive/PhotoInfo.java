package tang.qinyoyo.archive;

import lombok.Getter;
import lombok.Setter;
import tang.qinyoyo.ArchiveUtils;
import tang.qinyoyo.exiftool.ExifTool;
import tang.qinyoyo.exiftool.Key;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class PhotoInfo implements Serializable,Cloneable {
    private String subFolder; 
    private String fileName;
    private long   fileSize;

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

    @Override
    public String toString() {
        Map<String,Object> attributes = new LinkedHashMap<>();
        if (rating!=null && rating>0) {
            String s = "";
            for (int i=0;i<rating;i++) s += "★";
            attributes.put("rating",s);
        }

        attributes.put("file",subFolder + File.separator + fileName);

        if (shootTime!=null) attributes.put("createTime",DateUtil.date2String(shootTime));
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
    public void setFile(String rootPath, File file) throws Exception {
        String dir = file.getParentFile().getCanonicalPath();
        if (!dir.startsWith(rootPath)) throw new RuntimeException(rootPath + " 不包含 " + file.getAbsolutePath());
        subFolder = dir.substring(rootPath.length());
        if (subFolder.startsWith("/") || subFolder.startsWith("\\")) subFolder = subFolder.substring(1);
        fileName = file.getName();
        fileSize = file.length();
        if (ArchiveUtils.isInWebFolder(subFolder) && fileName.equals("index.html")) {
            setMimeType("text/html");
            String subTitle = subFolder.substring(0,subFolder.length()-4);
            int pos = subTitle.lastIndexOf(File.separator);
            setSubTitle(pos>=0 ? subTitle.substring(pos+1) : subTitle);
            System.out.println("    处理游记 : " + getSubTitle());
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
    public void setPropertiesBy(Map<Key, Object> attrs) {
        if (attrs==null) return;
        for (Key k : attrs.keySet()) {
            Object v=attrs.get(k);
            if (emptyValue(v)) continue;
            String s = v.toString();
            Date dt=null;
            try {
                switch (k) {
                    case DATETIMEORIGINAL:
                        if (!s.startsWith("0000")) {
                            Object subSec = attrs.get(Key.SUB_SEC_TIME_ORIGINAL);
                            shootTime = DateUtil.string2Date(v.toString()+(
                                        emptyValue(subSec)?"":"."+(subSec.toString()+"00").substring(0,3)
                                ));
                        }
                        break;
                    case CREATEDATE:
                        if (!s.startsWith("0000")) {
                            Object subSec = attrs.get(Key.SUB_SEC_TIME_CREATE);
                            createTime = DateUtil.string2Date(v.toString()+(
                                    emptyValue(subSec)?"":"."+(subSec.toString()+"00").substring(0,3)
                                    ));
                        }
                        break;
                    case IMAGE_WIDTH:
                        width = Integer.parseInt(s);
                        break;
                    case IMAGE_HEIGHT:
                        height = Integer.parseInt(s);
                        break;
                    case DOCUMENT_ID:
                        documentId = s;
                        break;
                    case IPTCDigest:
                        digest = s;
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
                    case ORIENTATION:
                        orientation = Orientation.value(s);
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
                    case SUBJECT_CODE:
                        subjectCode = s;
                        break;
                    default:
                }
            } catch (Exception e) {}
        }
        if (shootTime==null && createTime!=null && mimeType!=null && !mimeType.toLowerCase().startsWith("image")) shootTime = createTime;
        if (shootTime==null) shootTime = DateUtil.getShootTimeFromFileName(fileName);
    }

    public void readProperties(String rootPath) {
        File f = new File(fullPath(rootPath));
        if (f.exists() && SupportFileType.isSupport(f.getName())) {
            try {
                Map<String, Map<Key, Object>>  fileInfos = ExifTool.getInstance().query(f, ArchiveInfo.NEED_KEYS);
                if (fileInfos!=null) {
                    setPropertiesBy(fileInfos.get(f.getName()));
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
    public String fullThumbPath(String root) throws IOException {
        if (mimeType==null || (!mimeType.contains("image/") && !mimeType.contains("video/"))) throw new IOException("not supported type");
        String sub = getSubFolder();
        if (sub == null || sub.isEmpty()) sub =".thumb";
        else {
            if (sub.startsWith(".delete"+File.separator)) sub=sub.substring(8);
            sub = ".thumb"+File.separator + sub;
        }
        return new File(new File(root, sub), getFileName()).getCanonicalPath() + (mimeType.contains("video/") ? ".jpg" : "");
    }

    public boolean modifyOrientation(String root, Integer newRating, Integer ... operations) {
        if (mimeType==null || !mimeType.contains("image/")) return false;
        Integer newOrientation = null;
        if (operations!=null && operations.length>0) {
            orientation = Orientation.getOrientation(new File(fullPath(root)));
            int orientation1=0;
            if (orientation==null || orientation==Orientation.NONE.getValue()) orientation1 = Orientation.by(operations);
            else {
                Integer [] ops = new Integer[operations.length+1];
                ops[0]=orientation;
                for (int i=0;i<operations.length;i++) ops[i+1] = operations[i];
                orientation1 = Orientation.by(ops);
            }
            if ((orientation==null && orientation1!=Orientation.NONE.getValue()) || (orientation!=null && orientation1!=orientation)) {
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
                return true;
            }
        } catch (IOException e) {
        }
        return false;
    }
    public boolean delete(String rootPath) {
        try {
            new File(fullThumbPath(rootPath)).delete();
        } catch (Exception e) {}
        return new File(fullPath(rootPath)).delete();
    }
    private static String nameWithoutExt(String name) {
        int p = name.lastIndexOf(".");
        if (p >= 0)
            return name.substring(0, p);
        else
            return name;
    }
    private int nameCompare(PhotoInfo p) {
        String na = (getSubFolder() + "\\" + nameWithoutExt(getFileName())).toLowerCase();
        String nb = (p.getSubFolder() + "\\" + nameWithoutExt(p.getFileName())).toLowerCase();

        if (na.length()>nb.length() && na.startsWith(nb)) return -1;
        else if (nb.length()>na.length() && nb.startsWith(na)) return 1;
        else {
            na = (getSubFolder() + "\\" + getFileName()).toLowerCase();
            nb = (p.getSubFolder() + "\\" + p.getFileName()).toLowerCase();
            return na.compareTo(nb);
        }
    }
    public int compareTo(PhotoInfo p) {
        Date d1 = getShootTime(), d2 = p.getShootTime();
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
    public String formattedAddress(boolean useCountryName) {
        if (allNull(country,province,city,location)) return subjectCode==null ? "" : subjectCode;
        boolean cc = ArchiveUtils.hasChinese(country) || ArchiveUtils.hasChinese(province) ||ArchiveUtils.hasChinese(city) ||ArchiveUtils.hasChinese(location);
        if (cc) {
            String address = ArchiveUtils.join(null,useCountryName ? country : "", province, ArchiveUtils.equals(province,city)?"":city, location);
            if (subjectCode!=null && !subjectCode.isEmpty() && !address.contains(subjectCode)) return address.isEmpty() ? subjectCode : address + "," + subjectCode;
            else return address;
        } else {
            String address = ArchiveUtils.join(",",location, city, ArchiveUtils.equals(province,city)?"":province, useCountryName ? country : "");
            if (subjectCode!=null && !subjectCode.isEmpty() && !address.contains(subjectCode)) return address.isEmpty() ? subjectCode : subjectCode + "," + address;
            else return address;
        }
    }
    public String xmlString(Key ... keys) {
        StringBuilder sb=new StringBuilder();
        for (Key key : keys) {
            String value = null;
            switch (key) {
                case DATETIMEORIGINAL:
                    if (shootTime!=null) {
                        value = DateUtil.date2String(shootTime,"yyyy:MM:dd HH:mm:ss");
                        if (shootTime.getTime() % 1000 > 0) {
                            sb.append("\t<").append(Key.getName(Key.SUB_SEC_TIME_ORIGINAL)).append(">")
                              .append(shootTime.getTime() % 1000)
                              .append("</").append(Key.getName(Key.SUB_SEC_TIME_ORIGINAL)).append( ">\n");
                        }
                    }
                    break;
                case CREATEDATE:
                    if (createTime!=null) {
                        value = DateUtil.date2String(createTime,"yyyy:MM:dd HH:mm:ss");
                        if (createTime.getTime() % 1000 > 0) {
                            sb.append("\t<").append(Key.getName(Key.SUB_SEC_TIME_ORIGINAL)).append(">")
                                    .append(shootTime.getTime() % 1000)
                                    .append("</").append(Key.getName(Key.SUB_SEC_TIME_ORIGINAL)).append( ">\n");
                        }
                    }
                    break;
                case DOCUMENT_ID:
                    value = documentId;
                    break;
                case IPTCDigest:
                    value = digest;
                    break;
                case MODEL:
                    value = model;
                    break;
                case LENS_ID:
                    value = lens;
                    break;
                case GPS_LONGITUDE:
                    if (longitude!=null) {
                        String SN = (longitude < 0 ? "S" : "N");
                        double lon = Math.abs(longitude);
                        int du = (int)lon;
                        int fen = (int)((lon - du) * 60.0);
                        double m = ((lon - du)*60.0 - fen)*60.0;
                        value = String.format("%d,%d,%.6f%s",du, fen, m, SN);
                    }
                    break;
                case GPS_LATITUDE:
                    if (latitude!=null) {
                        String EW = (latitude < 0 ? "W" : "E");
                        double lat = Math.abs(latitude);
                        int du = (int)lat;
                        int fen = (int)((lat - du) * 60.0);
                        double m = ((lat - du)*60.0 - fen)*60.0;
                        value = String.format("%d,%d,%.6f%s",du, fen, m, EW);
                    }
                    break;
                case GPS_ALTITUDE:
                    if (altitude!=null) {
                        sb.append("\t<exif:GPSAltitude>")
                                .append(String.format("%.6f",Math.abs(altitude)))
                                .append("</exif:GPSAltitude>\n");
                        sb.append("\t<exif:GPSAltitudeRef>")
                                .append(altitude>0.0 ? 1 : 0)
                                .append("</exif:GPSAltitudeRef>\n");
                    }
                    break;
                case ARTIST:
                    if (artist!=null) {
                        value = artist;
                        sb.append("\t<IPTC:By-line>")
                                .append(artist)
                                .append("</IPTC:By-line>\n");
                    }
                    break;
                case HEADLINE:
                    value = headline;
                    break;
                case DESCRIPTION:
                    value = subTitle;
                    break;
                case RATING:
                    if (rating!=null) value = String.valueOf(rating);
                    break;
                case ORIENTATION:
                    if (orientation!=null) value = Orientation.name(orientation);
                case SCENE:
                    value = scene;
                    break;
                case COUNTRY:
                    value = country;
                    break;
                case STATE:
                    value = province;
                    break;
                case CITY:
                    value = city;
                    break;
                case LOCATION:
                    value = location;
                    break;
                case SUBJECT_CODE:
                    value = subjectCode;
                    break;
                default:
            }
            if (value!=null) sb.append("\t<").append(Key.getName(key)).append(">").append(value).append("</").append(Key.getName(key)).append( ">\n");
        }
        String r = sb.toString();
        String header = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>\n" +
                "<rdf:Description rdf:about=''>\n";
        String tail = "</rdf:Description>\n</rdf:RDF>\n";
        if (r.isEmpty()) return r;
        else return header + r + tail;
    }
}
