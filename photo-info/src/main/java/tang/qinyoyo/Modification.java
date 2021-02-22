package tang.qinyoyo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import tang.qinyoyo.archive.ArchiveInfo;
import tang.qinyoyo.archive.DateUtil;
import tang.qinyoyo.archive.Orientation;
import tang.qinyoyo.archive.PhotoInfo;
import tang.qinyoyo.exiftool.ExifTool;
import tang.qinyoyo.exiftool.Key;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Getter
@Setter
public class Modification {
    public static final int Exif        = 1;
    public static final int Remove      = 2;
    public static final int Scan        = 3;
    public static final String modification_dat = ".modification.dat";
    public static final String temp_path = "._g_s_t_";
    public static final String XMP = "xmp";
    public static final String start_time_key = "--start-time-long--";
    public static final String end_time_key = "--end-time-long--";
    int action;
    String path;
    Map<String,Object> params;
    public Modification(int action,String path,Map<String,Object> params) {
        this.action = action;
        this.path = path;
        this.params = params;
    }
    public Modification(PhotoInfo pi,Key ... keys)
    {
        this(pi,Arrays.asList(keys));
    }
    public Modification(PhotoInfo pi,List<Key> keys) {
        this.action = Exif;
        this.path = pi.getSubFolder().isEmpty() ? pi.getFileName() : (pi.getSubFolder() + File.separator + pi.getFileName());
        this.params = exifMap(pi,keys);
    }
    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
    public static void save(Modification mod,String rootPath) {
        File bak = new File(rootPath, modification_dat +".bak");
        File src = new File(rootPath, modification_dat);
        try {
            Files.copy(src.toPath(),bak.toPath(),StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArchiveUtils.appendToFile(src, mod.toString());
    }
    public static List<Modification> read(String rootPath) {
        String actions = ArchiveUtils.getFromFile(new File(rootPath, modification_dat +".sync"));
        if (actions==null) return null;
        String [] aa = actions.split("\n");
        List<Modification> list=new ArrayList<>();
        for (String s: aa) {
            s=s.trim();
            Gson gson=new GsonBuilder()
                    .registerTypeAdapter(new TypeToken<Map<String, Object>>() {}.getType(), new MapTypeAdapter())
                    .create();
            try {
                Modification m = gson.fromJson(s,Modification.class);
                list.add(m);
            } catch (Exception e) {}
        }
        return list;
    }
    public static Double fromDFM(String s) {
        try {
            if (s==null) return null;
            String [] dfm = s.split(",");
            if (dfm.length==1) {
                return Double.parseDouble(s);
            }
            if (dfm.length>=3) {
                int d = Integer.parseInt(dfm[0]),
                    f=Integer.parseInt(dfm[1]);
                dfm[2]=dfm[2].toUpperCase();
                dfm[2] = dfm[2].substring(0,dfm[2].length()-1);
                double m = Double.parseDouble(dfm[2]);
                return (d + f/60.0 + m/3600.0)*(dfm[2].endsWith("W") || dfm[2].endsWith("S")?-1:1);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    public static Map<String,Object> exifMap(PhotoInfo pi, List<Key> keys) {
        Map<String,Object> map = new HashMap<>();
        if (pi==null || keys==null || keys.size()==0) return map;
        for (Key key : keys) {
            Object value = null;
            switch (key) {
                case DATETIMEORIGINAL:
                    Date shootTime = pi.getShootTime();
                    if (shootTime!=null) {
                        value = DateUtil.date2String(shootTime,"yyyy:MM:dd HH:mm:ss"+(shootTime.getTime() % 1000 > 0 ? ".SSS":""));
                    }
                    break;
                case CREATEDATE:
                    Date createTime = pi.getCreateTime();
                    if (createTime!=null) {
                        value = DateUtil.date2String(createTime,"yyyy:MM:dd HH:mm:ss"+(createTime.getTime() % 1000 > 0 ? ".SSS":""));
                    }
                    break;
                case DOCUMENT_ID:
                    value = pi.getDocumentId();
                    break;
                case IPTCDigest:
                    value = pi.getDigest();
                    break;
                case MODEL:
                    value = pi.getModel();
                    break;
                case LENS_ID:
                    value = pi.getLens();
                    break;
                case GPS_LONGITUDE:
                    Double longitude = pi.getLongitude();
                    if (longitude!=null) {
                        value = String.format("%.7f",longitude);
                    }
                    break;
                case GPS_LATITUDE:
                    Double latitude = pi.getLatitude();
                    if (latitude!=null) {
                        value = String.format("%.7f",latitude);
                    }
                    break;
                case GPS_ALTITUDE:
                    Double altitude = pi.getAltitude();
                    if (altitude!=null) {
                        value = String.format("%.6f",altitude);
                    }
                    break;
                case ARTIST:
                    value = pi.getArtist();
                    break;
                case HEADLINE:
                    value = pi.getHeadline();
                    break;
                case DESCRIPTION:
                    value = pi.getSubTitle();
                    break;
                case RATING:
                    if (pi.getRating()!=null) value = String.valueOf(pi.getRating());
                    break;
                case ORIENTATION:
                    if (pi.getOrientation()!=null) value = Orientation.name(pi.getOrientation());
                case SCENE:
                    value = pi.getScene();
                    break;
                case COUNTRY:
                    value = pi.getCountry();
                    break;
                case STATE:
                    value = pi.getProvince();
                    break;
                case CITY:
                    value = pi.getCity();
                    break;
                case LOCATION:
                    value = pi.getLocation();
                    break;
                case SUBJECT_CODE:
                    value = pi.getSubjectCode();
                    break;
                default:
            }
            map.put(Key.getName(key),value);
        }
        return map;
    }
    public static void deleteSameProperties(PhotoInfo p, Map<String, Object> attrs) {
        if (p==null || attrs==null) return;
        List<Key> keys = new ArrayList<>();
        for (String k : attrs.keySet()) {
            Optional<Key> key = Key.findKeyWithName(k);
            if (key.isPresent()) keys.add(key.get());
        }
        Map<String,Object> values = exifMap(p,keys);
        for (String k : values.keySet()) {
            Object v1 = attrs.get(k);
            Object v2 = values.get(k);
            if (ArchiveUtils.equals(v1,v2)) attrs.remove(k);
        }
    }

    public static String xmlString(Map<String,Object> map) {
        if (map==null || map.isEmpty()) return "";
        StringBuilder sb=new StringBuilder();
        for (String key : map.keySet()) {
            if (key.equals(start_time_key) || key.equals(end_time_key)) continue;
            Object value = map.get(key);
            if (value!=null || !value.toString().isEmpty()) {
                if (key.equals(Key.getName((Key.DATETIMEORIGINAL))) && value.toString().indexOf(".")>0) {
                    String dt = value.toString();
                    int pos = dt.indexOf(".");
                    value = dt.substring(0,pos);
                    sb.append("\t<").append(Key.getName(Key.SUB_SEC_TIME_ORIGINAL)).append(">")
                            .append(dt.substring(pos+1))
                            .append("</").append(Key.getName(Key.SUB_SEC_TIME_ORIGINAL)).append(">\n");
                } else if (key.equals(Key.getName((Key.CREATEDATE)))  && value.toString().indexOf(".")>0) {
                    String dt = value.toString();
                    int pos = dt.indexOf(".");
                    value = dt.substring(0,pos);
                    sb.append("\t<").append(Key.getName(Key.SUB_SEC_TIME_ORIGINAL)).append(">")
                            .append(dt.substring(pos+1))
                            .append("</").append(Key.getName(Key.SUB_SEC_TIME_ORIGINAL)).append(">\n");
                } else if (key.equals(Key.getName((Key.GPS_LONGITUDE)))) {
                    double longitude = Double.parseDouble(value.toString());
                    String SN = (longitude < 0 ? "S" : "N");
                    double lon = Math.abs(longitude);
                    int du = (int)lon;
                    int fen = (int)((lon - du) * 60.0);
                    double m = ((lon - du)*60.0 - fen)*60.0;
                    value = String.format("%d,%d,%.6f%s",du, fen, m, SN);
                } else if (key.equals(Key.getName((Key.GPS_LATITUDE)))) {
                    double latitude = Double.parseDouble(value.toString());
                    String EW = (latitude < 0 ? "W" : "E");
                    double lat = Math.abs(latitude);
                    int du = (int)lat;
                    int fen = (int)((lat - du) * 60.0);
                    double m = ((lat - du)*60.0 - fen)*60.0;
                    value = String.format("%d,%d,%.6f%s",du, fen, m, EW);
                } else if (key.equals(Key.getName((Key.GPS_ALTITUDE)))) {
                    double altitude = Double.parseDouble(value.toString());
                    sb.append("\t<").append(Key.getName(Key.GPS_ALTITUDE_REF)).append(">")
                            .append(altitude>0.0 ? 1 : 0)
                            .append("</").append(Key.getName(Key.GPS_ALTITUDE_REF)).append(">\n");
                    value = String.format("%.6f",Math.abs(altitude));
                } else if (key.equals(Key.getName((Key.ARTIST)))) {
                    sb.append("<IPTC:By-line>")
                            .append(value.toString())
                            .append("</IPTC:By-line>\n");
                }
            }
            sb.append("\t<").append(key).append(">").append(value == null ? "" : value.toString()).append("</").append(key).append(">\n");
        }
        String r = sb.toString();
        if (r.isEmpty()) return r;
        else {
            String header = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                    "<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>\n" +
                    "<rdf:Description rdf:about=''>\n";
            String tail = "</rdf:Description>\n</rdf:RDF>\n";
            return header + r + tail;
        }
    }
    private static void executeExiftool(File xmpDir, File imgDir, Map<String,File> files) {
        try {
            Map<String, List<String>> result = ExifTool.getInstance().execute(imgDir, "-m",
                    "-charset", "IPTC=UTF8", "-charset", "EXIF=UTF8",
                    "-tagsfromfile", XMP + File.separator + "%f.xmp");
            List<String> error = result.get(ExifTool.ERROR);
            if (error != null && error.size() > 0) {
                for (String err : error)
                    System.out.println(err);
            }
            for (String link : files.keySet()) {
                File modified = new File(imgDir, link);
                File f = files.get(link);
                if (modified.exists() && modified.length()>0) {
                    try {
                        Files.move(modified.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            ArchiveUtils.removeFilesInDir(xmpDir, false);
            ArchiveUtils.removeFilesInDir(imgDir,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean removeAction(String path,ArchiveInfo archiveInfo) {
        String rootPath = archiveInfo.getPath();
        if (ArchiveUtils.deletePhoto(archiveInfo,path)) {
            return true;
        }
        return false;
    }
    public static boolean scanAction(String path, ArchiveInfo archiveInfo) {
        String rootPath = archiveInfo.getPath();
        File dir = new File(rootPath, path);
        if (dir.exists() && dir.isDirectory()) {
            ArchiveUtils.removeEmptyFolder(dir);
            if (dir.exists()) {
                new File(rootPath+File.separator+path+File.separator+".need-scan").delete();
                archiveInfo.addFile(dir, true);
                archiveInfo.getInfos().stream().filter(p->p.getSubFolder().startsWith(path)).forEach(p->archiveInfo.createThumbFiles(p));
                archiveInfo.sortInfos();
                archiveInfo.saveInfos();
                return true;
            }
        }
        return false;
    }
    public static boolean rangeExifAction(String path, ArchiveInfo archiveInfo, Date start, Date end, Map<String,Object> params) {
        if (start==null || end==null) return false;
        params.put(start_time_key,start.getTime());
        params.put(end_time_key,end.getTime());
        execute(new ArrayList<Modification>(){{
            add(new Modification(Exif,path,params));
        }},archiveInfo);
        return true;
    }
    public static void makeLink(String source,String targetLink) throws IOException {
        Path srcPath = Paths.get(source);
        Path linkPath = Paths.get(targetLink);
        Files.createSymbolicLink(linkPath,srcPath);
    }
    public static void execute(List<Modification> list, ArchiveInfo archiveInfo) {
        String rootPath = archiveInfo.getPath();

        list.stream().filter(m->m.action==Scan).reduce(new HashSet<String>(),(acc,m)-> {
                if (!acc.contains(m.path)) acc.add(m.path);
                return acc;
            },(acc,m)->null).forEach(path->scanAction(path,archiveInfo));

        Set<String> removedPaths = new HashSet<>();
        list.stream().filter(m->m.action==Remove).reduce(removedPaths,(acc,m)-> {
            if (!acc.contains(m.path)) acc.add(m.path);
            return acc;
        },(acc,m)->null);
        removedPaths.forEach(path->{
            removeAction(path, archiveInfo);
        });
        Map<String,Map<String,Object>> exifMap = new HashMap<>();
        list.stream().filter(m->m.action==Exif && !removedPaths.contains(m.path))
            .reduce(exifMap,(acc,m)->{
                if (m.path==null || m.params==null || m.params.isEmpty()) return acc;
                String key = m.path;
                if (m.params.containsKey(start_time_key) && m.params.containsKey(end_time_key)) {
                    key = key + "," + m.params.get(start_time_key).toString() + "," + m.params.get(end_time_key).toString();
                    m.params.remove(start_time_key);
                    m.params.remove(end_time_key);
                }
                if (acc.containsKey(key)) {
                    acc.get(key).putAll(m.params);
                } else acc.put(key,m.params);
                return acc;
            },(acc,m)->null);

        File imgDir = new File(rootPath,temp_path);
        File xmpDir = new File(imgDir,XMP);
        xmpDir.mkdirs();
        ArchiveUtils.removeFilesInDir(xmpDir, false);
        ArchiveUtils.removeFilesInDir(imgDir,false);
        int count = 0;
        Map<String,File> files = new HashMap<>();
        for (String key : exifMap.keySet()) {
            Map<String,Object> params = exifMap.get(key);
            String [] keys = key.split(",");
            String path = keys[0];
            List<String> pathList = new ArrayList<>();
            if (keys.length==3) {
                try {
                    long start = Long.parseLong(keys[1]),
                         end = Long.parseLong(keys[2]);
                    archiveInfo.getInfos().stream().filter(p ->
                               p.getSubFolder().startsWith(path) && p.getShootTime() != null
                            && p.getShootTime().getTime() >= start && p.getShootTime().getTime() <= end
                    ).reduce(pathList,(acc,p)-> {
                        acc.add(p.getSubFolder().isEmpty() ? p.getFileName() : (p.getSubFolder() + File.separator + p.getFileName()));
                        return acc;
                    },(acc,p)->null);
                } catch (Exception e){}
            } else {
                pathList.add(path);
            }
            for (String filePath : pathList) {
                File img = new File(rootPath, filePath);
                if (img.exists() && img.isFile()) {
                    PhotoInfo pi = archiveInfo.find(img);
                    if (pi==null) continue;
                    deleteSameProperties(pi,params);
                    if (params.isEmpty()) continue;
                    String xml = xmlString(params);
                    String link = count + (img.getName().lastIndexOf(".") >= 0 ? img.getName().substring(img.getName().lastIndexOf(".")) : "");
                    try {
                        makeLink(img.getCanonicalPath(), new File(imgDir, link).getCanonicalPath());
                        if (new File(imgDir, link).exists()) {
                            File xmpFile = new File(xmpDir, count + ".xmp");
                            ArchiveUtils.writeToFile(xmpFile, xml, "UTF-8");
                            count++;
                            files.put(link, img);
                            pi.setPropertiesBy(params);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (count>=1000) {
                executeExiftool(xmpDir,imgDir,files);
                files.clear();
                count=0;
            }
        }
        if (count>0) {
            executeExiftool(xmpDir,imgDir,files);
        }
        ArchiveUtils.removeFilesInDir(xmpDir,true);
        ArchiveUtils.removeFilesInDir(imgDir,true);
    }
    public static void resetSyncAction(String rootPath) {
        new File(rootPath, modification_dat +".sync").delete();
    }
}
