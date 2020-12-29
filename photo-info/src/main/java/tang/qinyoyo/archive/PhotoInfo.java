package tang.qinyoyo.archive;

import lombok.Getter;
import lombok.Setter;
import tang.qinyoyo.exiftool.ExifTool;
import tang.qinyoyo.exiftool.Key;
import java.io.File;
import java.io.Serializable;
import java.util.Date;
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
    private String make;
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
    private String orientation;

    private String headline;
    private String subTitle;     // 题注

    private Double longitude;
    private Double latitude;
    private Double altitude;

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
                    case SUBSECDATETIMEORIGINAL:
                        if (!s.startsWith("0000")) dt = DateUtil.string2Date(v.toString());
                        if (dt!=null) shootTime=dt;
                        break;
                    case DATETIMEORIGINAL:
                        if (shootTime==null) {
                            if (!s.startsWith("0000")) dt = DateUtil.string2Date(v.toString());
                            if (dt != null) shootTime = dt;
                        }
                        break;
                    case CREATEDATE:
                        if (!s.startsWith("0000")) createTime = DateUtil.string2Date(v.toString());
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
                Map<String, Map<Key, Object>>  fileInfos = new ExifTool.Builder().build().query(f, ArchiveInfo.NEED_KEYS);
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
        return shootTime==null && createTime==null && isEmpty(make) && isEmpty(model) && isEmpty(lens) && isEmpty(documentId);
        // 网络图片 digest 很多一样，不可靠
    }
    private static boolean equals(Date s1, Date s2) {
        if (s1 == null && s2 == null)
            return true;
        else if (s1 != null && s2 != null)
            return s1.getTime() == s2.getTime();
        else
            return false;
    }
    private static boolean equals(String s1, String s2) {
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
    public boolean exifEquals(PhotoInfo pi) {
        if (equals(shootTime,pi.shootTime)) {
            if (equals(createTime,pi.createTime)) {
                if (equals(make,pi.make)) {
                    if (equals(model,pi.model)) {
                        if (equals(lens,pi.lens)) {
                            if (equals(digest,pi.digest)) {
                                if (equals(documentId,pi.documentId)) return true;
                                else return false;
                            } else return false;
                        } else return false;
                    } else return false;
                } else return false;
            } else return false;
        } else return false;
    }
    public  boolean absoluteSameAs(PhotoInfo pi) {
        if (isExifEmpty() && pi.isExifEmpty()) return equals(fileName, pi.fileName) && (fileSize == pi.fileSize);
        else return (equals(fileName, pi.fileName) && (fileSize == pi.fileSize) && exifEquals(pi)) ;
    }
    public  boolean sameAs(PhotoInfo pi) throws Exception {
        if (this == pi) return true;
        if (equals(fileName, pi.fileName) && (fileSize == pi.fileSize)) return true; // 文件名和大小一样，可能其中一个删除了exif信息
        if (!extName(fileName).toLowerCase().equals(extName(pi.getFileName()).toLowerCase())) return false; // 文件扩展名不同，不一致
        if (isExifEmpty() && pi.isExifEmpty()) {
            if (!equals(digest,pi.digest)) return false;
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
        try {
            String sub = getSubFolder();
            if (sub == null || sub.isEmpty()) {
                return new File(new File(root, ".thumb"),getFileName()).getCanonicalPath();
            } else
                return new File(new File(root, ".thumb"+File.separator + sub), getFileName()).getCanonicalPath();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
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
    int compareTo(PhotoInfo p) {
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
}
