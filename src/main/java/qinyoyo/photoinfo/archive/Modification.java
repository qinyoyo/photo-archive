package qinyoyo.photoinfo.archive;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.Setter;
import qinyoyo.photoinfo.ArchiveUtils;
import qinyoyo.utils.DateUtil;
import qinyoyo.photoinfo.exiftool.ExifTool;
import qinyoyo.photoinfo.exiftool.Key;
import qinyoyo.utils.FileUtil;
import qinyoyo.utils.Util;

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

    public static class ModificationTypeAdapter extends TypeAdapter<Object> {
        @Override
        public Object read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<Object>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(read(in));
                    }
                    in.endArray();
                    return list;
                case BEGIN_OBJECT:
                    Map<Key, Object> map = new HashMap<>();
                    in.beginObject();
                    while (in.hasNext()) {
                        Optional<Key> key = Key.findKeyWithName(in.nextName());
                        Object v = read(in);
                        if (key.isPresent()) map.put(key.get(), v==null?null:Key.parse(key.get(),v.toString()));
                    }
                    in.endObject();
                    return map;

                case STRING:
                    return in.nextString();

                case NUMBER:
                    /**
                     * 改写数字的处理逻辑，将数字值分为整型与浮点型。
                     */
                    double dbNum = in.nextDouble();

                    // 数字超过long的最大值，返回浮点类型
                    if (dbNum > Long.MAX_VALUE) {
                        return dbNum;
                    }

                    // 判断数字是否为整数值
                    long lngNum = (long) dbNum;
                    if (dbNum == lngNum) {
                        return lngNum;
                    } else {
                        return dbNum;
                    }

                case BOOLEAN:
                    return in.nextBoolean();

                case NULL:
                    in.nextNull();
                    return null;

                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            // 序列化无需实现
        }

    }

    private static final int Exif        = 1;
    private static final int Remove      = 2;
    private static final int Scan        = 3;
    private static final int Rename      = 4;

    public static final String modification_dat = ".modification.dat";
    public static final String temp_path = "._g_s_t_";
    public static final String CSV = "csv";
    int action;
    String path;
    Map<Key,Object> params;

    public static Modification exifModified(String path,Map<Key,Object> params) {
        return new Modification(Exif,path,params);
    }
    public static Modification removeFile(String path) {
        return new Modification(Remove,path,null);
    }
    public static Modification scanPath(String path) {
        return new Modification(Scan,path,null);
    }
    public static Modification renamePath(String path,String newName) {
        return new Modification(Rename,path,new HashMap<Key,Object>(){{ put(Key.DESCRIPTION,newName); }});
    }
    /**
     * 构建一个修改描述
     * @param action 修改行为
     * @param path 目录，不包含根目录
     * @param params 修改参数
     */
    public Modification(int action,String path,Map<Key,Object> params) {
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

    /**
     * 保存一个修改
     * @param mod 修改描述
     * @param rootPath 主目录
     */
    public static void save(Modification mod,String rootPath) {
        File bak = new File(rootPath, modification_dat +".bak");
        File src = new File(rootPath, modification_dat);
        try {
            Files.copy(src.toPath(),bak.toPath(),StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Util.printStackTrace(e);
        }
        FileUtil.appendToFile(src, mod.toString());
    }

    /**
     * 读取修改同步文件
     * @param rootPath 主目录
     * @return 修改列表
     */
    public static List<Modification> read(String rootPath) {
        String actions = FileUtil.getFromFile(new File(rootPath, modification_dat +".sync"));
        if (actions==null) return null;
        String [] aa = actions.split("\n");
        List<Modification> list=new ArrayList<>();
        Gson gson=new GsonBuilder()
                .registerTypeAdapter(new TypeToken<Map<Key, Object>>() {}.getType(), new ModificationTypeAdapter())
                .create();
        for (String s: aa) {
            s=s.trim();
            try {
                Modification m = gson.fromJson(s,Modification.class);
                list.add(m);
            } catch (Exception e){ Util.printStackTrace(e);}
        }
        return list;
    }

    /**
     * 从经纬度的dfm描述获得经纬度值
     * @param s 描述
     * @return 经纬度值
     */
    public static Double fromDFM(String s) {
        try {
            if (s==null) return null;
            String [] dfm = s.split(",",-1);
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

    /**
     * 获得tag键值对
     * @param pi 照片信息
     * @param keys 需要描述的tag列表
     * @return 键值对
     */
    public static Map<Key,Object> exifMap(PhotoInfo pi, List<Key> keys) {
        Map<Key,Object> map = new HashMap<>();
        if (pi==null || keys==null || keys.size()==0) return map;
        for (Key key : keys) {
            Object value = pi.getFieldByTag(key);
            map.put(key,value);
        }
        return map;
    }

    /**
     * 删除照片字段值与tag简直相同的简直
     * @param p 照片
     * @param attrs 键值对
     */
    public static void deleteSameProperties(PhotoInfo p, Map<Key, Object> attrs) {
        if (p==null || attrs==null) return;
        List<Key> keys = new ArrayList<>();
        for (Key key : attrs.keySet()) keys.add(key);
        Map<Key,Object> values = exifMap(p,keys);
        for (Key k : values.keySet()) {
            Object v1 = attrs.get(k);
            Object v2 = values.get(k);
            if (ArchiveUtils.equals(v1,v2)) attrs.remove(k);
        }
    }

    /**
     * 将tag键值对转换为xml串
     * @param map tag键值对
     * @return xml串
     */
    public static String xmlString(Map<Key,Object> map) {
        if (map==null || map.isEmpty()) return "";
        StringBuilder sb=new StringBuilder();
        for (Key key : map.keySet()) {
            Object value = map.get(key);
            if (!Util.isEmpty(value)) {
                switch (key) {
                    case DATETIMEORIGINAL:
                    case CREATEDATE:
                        if (value instanceof Date) {
                            Date dt = (Date)value ;
                            long  ms = (dt.getTime() % 1000);
                            String subName = key.equals(Key.DATETIMEORIGINAL) ? Key.getName(Key.SUB_SEC_TIME_ORIGINAL) : Key.getName(Key.SUB_SEC_TIME_ORIGINAL);
                            sb.append("\t<").append(subName).append(">")
                                    .append(ms)
                                    .append("</").append(subName).append(">\n");
                            value = DateUtil.date2String(dt);
                        }
                        break;
                    case GPS_LONGITUDE:
                        double longitude = Double.parseDouble(value.toString());
                        sb.append("\t<").append(Key.getName(Key.GPS_LONGITUDE_REF)).append(">")
                                .append(longitude < 0 ? "West" : "East")
                                .append("</").append(Key.getName(Key.GPS_LONGITUDE_REF)).append(">\n");
                        value = String.format("%.7f", longitude);
                        break;
                    case GPS_LATITUDE:
                        double latitude = Double.parseDouble(value.toString());
                        sb.append("\t<").append(Key.getName(Key.GPS_LATITUDE_REF)).append(">")
                                .append(latitude < 0 ? "South" : "North")
                                .append("</").append(Key.getName(Key.GPS_LATITUDE_REF)).append(">\n");
                        value = String.format("%.7f", latitude);
                        break;
                    case GPS_ALTITUDE:
                        double altitude = Double.parseDouble(value.toString());
                        sb.append("\t<").append(Key.getName(Key.GPS_ALTITUDE_REF)).append(">")
                                .append(altitude > 0.0 ? "Above Sea Level" : "Below Sea Level")
                                .append("</").append(Key.getName(Key.GPS_ALTITUDE_REF)).append(">\n");
                        value = String.format("%.7f", altitude);
                        break;
                    case GPS_DATETIME:
                        if (value instanceof Date) {
                            Date dt = (Date)value;
                            sb.append("\t<").append(Key.getName(Key.GPS_DATESTAMP)).append(">")
                                    .append(DateUtil.date2String(dt,"yyyy:MM:dd", TimeZone.getTimeZone("UTC")))
                                    .append("</").append(Key.getName(Key.GPS_DATESTAMP)).append(">\n");
                            sb.append("\t<").append(Key.getName(Key.GPS_TIMESTAMP)).append(">")
                                    .append(DateUtil.date2String(dt,"hh:mm:ss", TimeZone.getTimeZone("UTC")))
                                    .append("</").append(Key.getName(Key.GPS_TIMESTAMP)).append(">\n");
                            continue;
                        }
                        break;
                    case ARTIST:
                        sb.append("<").append(Key.getName(Key.BY_LINE)).append(">")
                                .append(value.toString())
                                .append("</").append(Key.getName(Key.BY_LINE)).append(">\n");
                        break;
                    default: value = value.toString();
                }
            }
            if (!key.equals(Key.GPS_DATETIME))
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

    private static Set<Key> modifiedTags(Map<String,Map<Key,Object>> exifMap) {
        Set<Key> result = new LinkedHashSet<>();
        exifMap.forEach((key,map)->result.addAll(map.keySet()));
        if (result.contains(Key.DATETIMEORIGINAL)) result.add(Key.SUB_SEC_TIME_ORIGINAL);
        if (result.contains(Key.CREATEDATE)) result.add(Key.SUB_SEC_TIME_CREATE);
        if (result.contains(Key.GPS_LONGITUDE)) result.add(Key.GPS_LONGITUDE_REF);
        if (result.contains(Key.GPS_LATITUDE)) result.add(Key.GPS_LATITUDE_REF);
        if (result.contains(Key.GPS_ALTITUDE)) result.add(Key.GPS_ALTITUDE_REF);
        if (result.contains(Key.GPS_DATETIME)) {
            result.add(Key.GPS_DATESTAMP);
            result.add(Key.GPS_TIMESTAMP);
            result.remove(Key.GPS_DATETIME);
        }
        if (result.contains(Key.ARTIST)) result.add(Key.BY_LINE);
        return result;
    }
    private static String formatValue(Object value) {
        if (Util.isEmpty(value)) return "-";  // delete
        else {
            Class<?> type = value.getClass();
            if (Boolean.class.isAssignableFrom(type)) {
                return ((Boolean)value) ? "1" : "0";
            } else if (Double.class.isAssignableFrom(type)) {
                return String.format("%.7f",(Double)value);
            } else if (String.class.isAssignableFrom(type)) {
                String s = (String) value;
                return s.contains(",") ? "\""+s+"\"" : s;
            } else if (Date.class.isAssignableFrom(type)) {
                return DateUtil.date2String((Date)value);
            } else return value.toString();
        }
    }
    private static String csvString(Map<String,Map<Key,Object>> exifMap) {
        if (exifMap==null || exifMap.isEmpty()) return null;
        Set<Key> selectedKeys = modifiedTags(exifMap);
        StringBuilder sb=new StringBuilder();
        sb.append("SourceFile");
        for (Key k : selectedKeys) sb.append(",").append(Key.getName(k));
        sb.append("\n");
        for (String path : exifMap.keySet()) {
            Map<Key,Object> map = exifMap.get(path);
            if (map!=null && !map.isEmpty()) {
                sb.append(path);
                for (Key k : selectedKeys) {
                    switch (k) {
                        case SUB_SEC_TIME_ORIGINAL:
                            if (map.containsKey(Key.DATETIMEORIGINAL)) {
                                Object v = map.get(Key.DATETIMEORIGINAL);
                                sb.append(",").append(v==null?"-":((Date)v).getTime() % 1000);
                            } else sb.append(",");
                            break;
                        case SUB_SEC_TIME_CREATE:
                            if (map.containsKey(Key.CREATEDATE)) {
                                Object v = map.get(Key.CREATEDATE);
                                sb.append(",").append(v==null?"-":((Date)v).getTime() % 1000);
                            } else sb.append(",");
                            break;
                        case GPS_LONGITUDE_REF:
                            if (map.containsKey(Key.GPS_LONGITUDE)) {
                                Object v = map.get(Key.GPS_LONGITUDE);
                                sb.append(",").append(v==null?"-":(((Double)v)>=0 ? "E":"W"));
                            } else sb.append(",");
                            break;
                        case GPS_LATITUDE_REF:
                            if (map.containsKey(Key.GPS_LATITUDE)) {
                                Object v = map.get(Key.GPS_LATITUDE);
                                sb.append(",").append(v==null?"-":(((Double)v)>=0 ? "N":"S"));
                            } else sb.append(",");
                            break;
                        case GPS_ALTITUDE_REF:
                            if (map.containsKey(Key.GPS_ALTITUDE)) {
                                Object v = map.get(Key.GPS_ALTITUDE);
                                sb.append(",").append(v==null?"-":(((Double)v)>=0 ? "0":"1"));
                            } else sb.append(",");
                            break;
                        case GPS_DATESTAMP:
                        case GPS_TIMESTAMP:
                            if (map.containsKey(Key.GPS_DATETIME)) {
                                Object v = map.get(Key.GPS_DATETIME);
                                sb.append(",").append(v==null?"-":DateUtil.date2String((Date)v,k.equals(Key.GPS_DATESTAMP)?"yyyy:MM:dd":"hh:mm:ss",
                                        TimeZone.getTimeZone("UTC")));
                            } else sb.append(",");
                            break;
                        case BY_LINE:
                            if (map.containsKey(Key.ARTIST)) {
                                Object v = map.get(Key.ARTIST);
                                sb.append(",").append(formatValue(v));
                            } else sb.append(",");
                            break;
                        default:
                            if (map.containsKey(k)) {
                                Object v = map.get(k);
                                sb.append(",").append(formatValue(v));
                            } else sb.append(",");
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static int executeExiftool(File csvDir, File imgDir, Map<String,File> files,String ... args) {
        try {
            Map<String, List<String>> result = ExifTool.getInstance().execute(imgDir, args);
            int count = ExifTool.updatesFiles(result);
            List<String> error = result.get(ExifTool.ERROR);
            if (error != null && error.size() > 0) {
                for (String err : error)
                    System.out.println(err);
            }
            for (String link : files.keySet()) {
                File modified = new File(imgDir, link);
                File f = files.get(link);
                try {
                    if (modified.exists() && !Files.isSymbolicLink(modified.toPath())) {
                        Files.move(modified.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (csvDir!=null) FileUtil.removeFilesInDir(csvDir, false);
            FileUtil.removeFilesInDir(imgDir,false);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int tagsFromCsvFile(Map<String,Map<Key,Object>> exifMap, File csvDir, File imgDir, Map<String,File> files) {
        String csvString = csvString(exifMap);
        if (csvString!=null) {
            FileUtil.writeToFile(new File(csvDir,"m.csv"), csvString, "UTF-8");
            return executeExiftool(csvDir,imgDir,files, "-n","-f",
                    "-charset", "IPTC=UTF8", "-charset", "EXIF=UTF8",
                    "-csv="+CSV + File.separator + "m.csv");
        } else return 0;

    }
    /**
     * 删除一个文件
     * @param path 文件
     * @param archiveInfo 归档信息
     * @param saveModification 是否需要记录修改
     * @return
     */
    public static boolean removeAction(String path,ArchiveInfo archiveInfo, boolean saveModification) {
        if (ArchiveUtils.deletePhoto(archiveInfo,path)) {
            if (saveModification) Modification.save(Modification.removeFile(path),archiveInfo.getPath());
            return true;
        }
        return false;
    }

    /**
     * 重新扫描一个子目录
     * @param path 子目录
     * @param archiveInfo 归档信息
     * @param saveModification 是否需要记录修改
     * @return 是否成功
     */
    public static boolean scanAction(String path, ArchiveInfo archiveInfo, boolean saveModification) {
        String rootPath = archiveInfo.getPath();
        File dir = new File(rootPath, path);
        if (dir.exists() && dir.isDirectory()) {
            FileUtil.removeEmptyFolder(dir);
            if (dir.exists()) {
                archiveInfo.rescanFile(dir);
                archiveInfo.sortInfos();
                archiveInfo.saveInfos();
                if (saveModification) save(Modification.scanPath(path), archiveInfo.getPath());
                return true;
            }
        }
        return false;
    }

    public static boolean renameAction(String path, String newName, ArchiveInfo archiveInfo, boolean saveModification) {
        PhotoInfo pi = archiveInfo.find(new File(archiveInfo.getPath(),path));
        if (pi!=null) {
            try {
                boolean result = pi.renameTo(archiveInfo.getPath(), newName);
                if ( result && saveModification) save(renamePath(path,newName),archiveInfo.getPath());
                return result;
            } catch (Exception e) {
                Util.printStackTrace(e);
            }
        }
        return false;
    }
    /**
     * 创建一个链接文件
     * @param source  源文件路径
     * @param targetLink 链接文件路径
     * @throws IOException 异常
     */
    public static void makeLink(String source,String targetLink) throws IOException {
        Path srcPath = Paths.get(source);
        Path linkPath = Paths.get(targetLink);
        Files.createSymbolicLink(linkPath,srcPath);
    }

    /**
     * 批量设置exif tag
     * @param pathMap 参数，以文件路径(不包含主目录）作为 键值，值为修改的键值对
     * @param rootPath 主目录
     * @return 修改成功的文件数，可能比 pathMap 数量大，因为可能会自动同步修改缩略图
     */
    public static int setExifTags(Map<String,Map<Key,Object>> pathMap, String rootPath) {
        File imgDir = new File(rootPath,temp_path);
        File csvDir = new File(imgDir,CSV);
        csvDir.mkdirs();
        FileUtil.removeFilesInDir(csvDir, false);
        FileUtil.removeFilesInDir(imgDir,false);
        int count = 0, updated = 0;
        Map<String,File> files = new HashMap<>();
        Map<String,Map<Key,Object>> exifMap = new HashMap<>();
        for (String path : pathMap.keySet()) {
            if (path==null || path.isEmpty()) continue;
            Map<Key,Object> params = pathMap.get(path);
            if (path.isEmpty() || params ==null || params.isEmpty()) continue;
            File img = new File(rootPath, path);
            if (img.exists() && img.isFile()) {
                String link = count + (img.getName().lastIndexOf(".") >= 0 ? img.getName().substring(img.getName().lastIndexOf(".")) : "");
                try {
                    makeLink(img.getCanonicalPath(), new File(imgDir, link).getCanonicalPath());
                    if (new File(imgDir, link).exists()) {
                        exifMap.put(link,params);
                        count++;
                        files.put(link, img);
                        if (params.containsKey(Key.ORIENTATION)) {
                            File thumb = new File(rootPath+File.separator+ArchiveUtils.THUMB, path);
                            if (thumb.exists() && thumb.isFile()) {
                                String thumbLink = count + (thumb.getName().lastIndexOf(".") >= 0 ? thumb.getName().substring(thumb.getName().lastIndexOf(".")) : "");
                                makeLink(thumb.getCanonicalPath(), new File(imgDir, thumbLink).getCanonicalPath());
                                if (new File(imgDir, thumbLink).exists()) {
                                    Map<Key,Object> thumbParams = new HashMap<>();
                                    thumbParams.put(Key.ORIENTATION,params.get(Key.ORIENTATION));
                                    exifMap.put(thumbLink,thumbParams);
                                    count++;
                                    files.put(thumbLink, thumb);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (count>=1000) {
                updated += tagsFromCsvFile(exifMap,csvDir,imgDir,files);
                exifMap.clear();
                files.clear();
                count=0;
                FileUtil.removeFilesInDir(csvDir,false);
                FileUtil.removeFilesInDir(imgDir,false);
            }
        }
        if (count>0) {
            updated += tagsFromCsvFile(exifMap,csvDir,imgDir,files);
        }
        FileUtil.removeFilesInDir(csvDir,true);
        FileUtil.removeFilesInDir(imgDir,true);
        return updated;
    }

    /**
     * 批量设置归档目录下的文件的exif tags
     * @param list 修改配置参数列表
     * @param archiveInfo 归档信息
     * @param saveModification 是否需要记录修改
     * @return 修改的文件数
     */
    public static int setExifTags(List<Modification> list, ArchiveInfo archiveInfo, boolean saveModification) {
        int updated = 0;
        Map<String,Map<Key,Object>> exifMap = new HashMap<>();
        long now = new Date().getTime();
        for (Modification m: list) {
            if (m.action==Exif) {
                if (m.path!=null && m.params!=null && !m.params.isEmpty()) {
                    String filePath = m.path;
                    File img = new File(archiveInfo.getPath(), filePath);
                    if (img.exists() && img.isFile()) {
                        Map<Key,Object> nm = new HashMap<>();
                        PhotoInfo info = archiveInfo.find(img);
                        nm.putAll(m.params);
                        if (info != null) {
                            deleteSameProperties(info, nm);
                        }
                        if (nm!=null && !nm.isEmpty()) {
                            if (exifMap.containsKey(filePath)) {
                                exifMap.get(filePath).putAll(nm);
                            } else {
                                exifMap.put(filePath,nm);
                            }
                            if (info!=null) {
                                info.setPropertiesBy(nm);
                                info.setLastModified(now);
                            }
                        }
                    }
                }
            }
            else if (m.action==Scan) scanAction(m.getPath(),archiveInfo,saveModification);
            else if (m.action==Remove) {
                if (removeAction(m.getPath(), archiveInfo,saveModification)) {
                    updated++;
                    exifMap.remove(m.getPath());
                }
            }
            else if (m.action==Rename) {
                if (m.getParams()!=null) {
                    Object newName = m.getParams().get(Key.DESCRIPTION);
                    if (newName!=null) renameAction(m.getPath(),newName.toString(),archiveInfo,saveModification);
                }
            }
        }
        if (!exifMap.isEmpty()) {
            int c = setExifTags(exifMap,archiveInfo.getPath());
            if(c>0) {
                updated += c;
                if (saveModification) {
                    for (String p:exifMap.keySet()) {
                        Modification.save(new Modification(Exif,p,exifMap.get(p)),archiveInfo.getPath());
                    }
                }
            }
        }
        return updated;
    }

    /**
     * 删除修改同步文件
     * @param rootPath
     */
    public static void resetSyncAction(String rootPath) {
        new File(rootPath, modification_dat +".sync").delete();
    }
}
