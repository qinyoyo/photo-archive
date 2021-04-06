package qinyoyo.photoinfo;

import lombok.NonNull;
import qinyoyo.photoinfo.archive.*;
import qinyoyo.photoinfo.exiftool.CommandRunner;
import qinyoyo.photoinfo.exiftool.ExifTool;
import qinyoyo.photoinfo.exiftool.Key;
import qinyoyo.utils.BaiduGeo;
import qinyoyo.utils.DateUtil;
import qinyoyo.utils.FileUtil;
import qinyoyo.utils.Util;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArchiveManager {
    static Map<String, Object> env = null;
    static String currentPath = ".";
    static TreeMap<Long, Map<String,Object>> gpxPoints = null;
    private static String getInputString(String message, String def) {
        String input=def;
        Scanner in = new Scanner(System.in);
        System.out.print(message + (def==null || def.isEmpty() ? ": " : ("(" + def + "): ")));
        input = in.nextLine().trim();
        if (input.isEmpty()) input=def;
        return input;
    }

    private static String chooseFolder(String title, FileFilter filter) {
        File[] f = selectFiles(title,false,filter);
        if (f==null || f.length==0) return null;
        else {
            if (f[0].isDirectory()) return f[0].getPath();
            else return f[0].getParent();
        }
    }
    private static File[] selectFiles(String title, boolean multiSelection, FileFilter filter) {
        int result = 0;
        String path = null;
        JFileChooser fileChooser = new JFileChooser();
        FileSystemView fsv = FileSystemView.getFileSystemView();  //注意了，这里重要的一句
        fileChooser.setCurrentDirectory(currentPath==null || currentPath.isEmpty() ? fsv.getHomeDirectory() : new File(currentPath));
        fileChooser.setDialogTitle(title==null || title.isEmpty() ? "请选择目录" : title);
        fileChooser.setApproveButtonText("确定");
        fileChooser.setFileSelectionMode(filter!=null ? JFileChooser.FILES_AND_DIRECTORIES : JFileChooser.DIRECTORIES_ONLY);
        if (filter!=null) fileChooser.setFileFilter(filter);
        fileChooser.setMultiSelectionEnabled(multiSelection);
        result = fileChooser.showOpenDialog(null);
        if (JFileChooser.APPROVE_OPTION == result) {
            if (multiSelection) {
                File [] files = fileChooser.getSelectedFiles();
                if (files!=null && files.length>0) {
                    File f = files[0];
                    currentPath = files[0].isDirectory() ? files[0].getPath() : files[0].getParent();
                    return files;
                }
            } else {
                File f = fileChooser.getSelectedFile();
                if (f!=null) {
                    currentPath = f.isDirectory() ? f.getPath() : f.getParent();
                    return new File[]{f};
                }
            }
        }
        return null;
    }

    private static String inputSubFolder(String title, String rootPath) {
        currentPath = rootPath;
        String path = chooseFolder(title, null);
        if (path==null) return null;
        path = ArchiveUtils.formatterSubFolder(path,rootPath);
        return path;
    }
    private static void afterChanged(ArchiveInfo archiveInfo) {
        archiveInfo.sortInfos();
        archiveInfo.saveInfos();
    }
    static String getProperty(@NonNull String key,String def) {
        if (env==null) return def;
        Map<String, Object> e = env;
        String [] kk = key.split("\\.");
        for (int i=0;i < kk.length; i++) {
            Object obj = e.get(kk[i]);
            if (obj==null) return def;
            else if (i==kk.length-1) return obj.toString();
            else if (obj instanceof Map) e = (Map<String, Object>)obj;
        }
        return def;
    }
    static boolean getProperty(Map<String, Object> env, @NonNull String key,boolean def) {
        String s = getProperty(key,def?"true":"false");
        return Util.boolValue(s);
    }
    static boolean rename(List<PhotoInfo> infos, String rootPath) {
        String pat = "";
        while (pat==null || pat.isEmpty()) {
            pat = getInputString("文件名格式(输入?显示提示)", PhotoInfo.RENAME_PATTERN);
            if (pat.equals("?")) {
                pat="";
                System.out.println("重命名文件时，可以指定一个模板，模板可以包含以下元数据：\n" +
                        "%y：拍摄时间年，4位\n" +
                        "%M：拍摄时间月，2位\n" +
                        "%d：拍摄时间日，2位\n" +
                        "%h：拍摄时间小时，2位\n" +
                        "%m：拍摄时间分钟，2位\n" +
                        "%s：拍摄时间秒，2位\n" +
                        "\n" +
                        "%o：拍摄设备\n" +
                        "%l：拍摄位置\n" +
                        "%u：主题代码，myStep App的足迹\n" +
                        "%c：拍摄场景\n" +
                        "\n" +
                        "%p：自动匹配目录名，忽略目录名前的数字和空格，如目录 201008 Canada匹配为 Canada\n" +
                        "%f: 原文件名，排除数字和连接符号(-_)\n" +
                        "\n" +
                        "使用默认值 ：\n" +
                        "   可以使用 %x=defvalue%的形式指定一个缺省值 defvalue，当x取值为空时用缺省值代替。\n" +
                        "   如 %y=2000%,表示使用年份，当没有拍摄时间时，使用2000\n" +
                        "   默认值可以使用 {x} 的方式引用另外一个元数据\n" +
                        "   如 %u={l}%,表示使用主题代码，当主题代码为空时，使用位置信息\n" +
                        "\n" +
                        "%E：扩展名大写\n" +
                        "%e：扩展名小写\n" +
                        "    %E %e只能使用一个，任意位置");
            }
        }
        if (infos!=null) {
            for (PhotoInfo pi : infos) {
                try {
                    String name0 = pi.getFileName();
                    pi.rename(rootPath, pat);
                    String name1 = pi.getFileName();
                    if (name1.equals(name0)) System.out.println(name0 + " 没有重命名");
                    else System.out.println(name0 + " 重命名为 " + name1);
                } catch (Exception e){ Util.printStackTrace(e);}
            }
            return true;
        }
        return false;
    }

    private static  FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || SupportFileType.isSupport(f.getName());
        }
        @Override
        public String getDescription() {
            return null;
        }
    };
    static boolean archiveWithArchivedInfo(@NonNull ArchiveInfo archived) {
        currentPath = FileUtil.getCurrentPath();
        boolean shutdown = false;
        final String rootPath = archived.getPath();
        String menuString =
                        "\n——————————————————————————————\n" +
                        "1 重建缩略图文件\n" +
                        "2 同步缩略图方向属性\n" +
                        "3 将属性写入到RAW文件\n" +
                        "4 重新扫描子目录\n" +
                        "5 将一个目录合并入归档\n" +
                        "6 重新命名目录文件\n" +
                        "7 删除空目录\n" +
                        "8 重新格式化游记文件\n" +
                        "9 获取地理位置信息\n" +
                        "a 执行 .modification.dat.sync\n" +
                        "b 生成 gpx 归档文件\n" +
                        "c 根据 gpx 写入地理数据\n" +
                        "d 生成gpsDatetime\n" +
                        "0 下一个操作之后关机\n" +
                        "s 启动浏览服务\n" +
                        "q 退出\n请选择一个操作";
        while (true) {
            boolean done = false;
            String path = "";
            String input = getInputString(menuString
                    , "");
            switch (input) {
                case "s": return true;
                case "q": return false;
                case "0":
                    shutdown = true;
                    break;
                case "1":
                    path = inputSubFolder("选择需要重建缩略图的子目录",rootPath);
                    if (path==null) break;
                    archived.createThumbFiles(path);
                    done = true;
                    break;
                case "2":
                    path = inputSubFolder("选择需要同步缩略图的子目录",rootPath);
                    if (path==null) break;
                    ArchiveUtils.syncThumbOrientation(archived, path);
                    done = true;
                    break;
                case "3":
                    path = chooseFolder("输入需要同步的RAW目录",fileFilter);
                    if (path==null) break;
                    currentPath = path;
                    List<String> args = new ArrayList<String>() {{
                        add("--ext");
                        add("xmp");
                    }};
                    ArchiveUtils.syncExifAttributesByTime(archived.getInfos(), photoInfoListIn(path,false, args), path);
                    done = true;
                    break;
                case "4":
                    path = inputSubFolder("选择需要重新扫描的子目录",rootPath);
                    if (path==null) break;
                    if (!path.isEmpty()) {
                        if (Modification.scanAction(path, archived))
                            Modification.save(new Modification(Modification.Scan, path, null), archived.getPath());
                        System.out.println("完成目录重新扫描 " + path);
                        afterChanged(archived);
                        done = true;
                    }
                    break;
                case "5":
                    path = chooseFolder("输入待归档的目录",fileFilter);
                    if (path==null) break;
                    currentPath = path;
                    if (!path.isEmpty()) {
                        File dir = new File(path);
                        if (dir != null && dir.exists() && dir.isDirectory()) {
                            boolean isOk = Util.boolValue(getInputString("文件已经处理好目录分类，直接添加", "yes"));
                            if (isOk) {
                                archived.addDirectory(new File(path));
                                done = true;
                            } else {
                                boolean clear1 = Util.boolValue(getInputString("是否重新完全扫描", "no"));
                                boolean same1 = Util.boolValue(getInputString("将相同文件移到.delete目录", "yes"));
                                boolean other1 = Util.boolValue(getInputString("将无法确定拍摄日期的文件移动到.other目录", "yes"));
                                ArchiveInfo camera = ArchiveUtils.getArchiveInfo(path, clear1, same1, other1);
                                if (camera!=null && camera.getInfos().size()>0) {
                                    System.out.println("删除归档文件夹已经存在的待归档文件...");
                                    camera.scanSameFilesWith(archived);
                                    ArchiveUtils.executeArchive(camera, archived);
                                    done = true;
                                }
                            }
                        }
                    }
                    break;
                case "6":
                    path = inputSubFolder("选择需要重新命名的目录",rootPath);
                    if (path==null) break;
                    String incSub = getInputString("是否搜索子目录", "yes");
                    final String temPath = path;
                    if (rename(archived.subFolderInfos(path,Util.boolValue(incSub)), archived.getPath())) {
                        done = true;
                        afterChanged(archived);
                    }
                    break;
                case "7":
                    FileUtil.removeEmptyFolder(new File(rootPath));
                    done = true;
                    break;
                case "8":
                    // 已归档的 html 文件
                    Set<String> archivedSteps = new HashSet<String>();
                    archived.getInfos().stream().filter(p->p.getMimeType()!=null && p.getMimeType().contains("html")).forEach(p->{
                        String stepPath = p.fullPath(rootPath);
                        ArchiveUtils.formatStepHtml(archived, new File(stepPath));
                        archivedSteps.add(stepPath);
                    });
                    // 未归档的 html 文件归档
                    List<PhotoInfo> stepList = ArchiveUtils.stepUnderFolder(rootPath,"",true);
                    if (archivedSteps.size()>0 && stepList!=null && stepList.size()>0){
                        for (Iterator iterator = stepList.iterator(); iterator.hasNext(); ) {
                            if (archivedSteps.contains(((PhotoInfo) iterator.next()).fullPath(rootPath))) iterator.remove();
                        }
                    }
                    if (stepList!=null && stepList.size()>0) {
                        stepList.forEach(p->{
                            ArchiveUtils.formatStepHtml(archived,new File(p.fullPath(rootPath)));
                        });
                        archived.getInfos().addAll(stepList);
                        afterChanged(archived);
                    }
                    done = true;
                    break;
                case "9":
                    BaiduGeo.seekAddressInfo(archived);
                    done = true;
                    break;
                case "a":
                    List<Modification> list = Modification.read(rootPath);
                    if (list!=null) {
                        System.out.println("同步修改...");
                        Modification.setExifTags(list,archived);
                        Modification.resetSyncAction(rootPath);
                        afterChanged(archived);
                        System.out.println("同步修改完成.");
                        done = true;
                    }
                    break;
                case "b":
                    path = inputSubFolder("选择需要归档的目录",rootPath);
                    if (path==null) break;
                    incSub = getInputString("是否搜索子目录", "yes");
                    done = writeGpxFile(archived,path, Util.boolValue(incSub))>0;
                    break;
                case "c":
                    if (!selectGpxInfo()) break;
                    String subPath = inputSubFolder("选择图像子目录(确保所有照片拍摄时区相同)",rootPath);
                    if (subPath == null) break;
                    boolean incSubF = Util.boolValue(getInputString("是否包含子目录", "yes"));
                    List<PhotoInfo> photoInfos = archived.subFolderInfos(subPath,incSubF).stream().filter(p->
                            p.getMimeType()!=null && p.getMimeType().contains("image") && p.getShootTime()!=null
                            && (p.getLongitude()==null || p.getLatitude()==null)
                    ).collect(Collectors.toList());
                    writeGpxInfo(photoInfos,archived.getPath(),gpxPoints);
                    afterChanged(archived);
                    done = true;
                    break;
                case "d":
                    addGpsDatetime(archived);
                    done = true;
                    break;
                default:
                    System.out.println("错误的选择！");
            }
            if (shutdown && done) {
                CommandRunner.shutdown(10);
                return false;
            }
        }
    }
    static List<PhotoInfo> photoInfoListIn(String path,boolean includeSubFolder, List<String> args) {
        List<PhotoInfo> list=ArchiveUtils.seekPhotoInfosInFolder(new File(path),path,includeSubFolder,args);
        ArchiveUtils.defaultSort(list);
        return list;
    }
    static boolean imageOperations() {
        currentPath = FileUtil.getCurrentPath();
        boolean shutdown = false;
        String menuString =
                "\n——————————————————————————————\n" +
                "未归档图像文件处理\n" +
                "1 根据图像文件获得gpx地理信息\n" +
                "2 校正图像拍摄时间\n" +
                "3 根据gpx文件写入地理信息\n" +
                "4 重新命名目录文件\n" +
                "5 删除空目录\n" +
                "0 下一个操作之后关机\n" +
                "s 启动浏览服务\n" +
                "q 退出\n请选择一个操作";
        while (true) {
            boolean done = false;
            String path = "";
            String input = getInputString(menuString, "");
            switch (input) {
                case "s": return true;
                case "q": return false;
                case "0":
                    shutdown = true;
                    break;
                case "1":
                    path = chooseFolder("选择图像目录",fileFilter);
                    if (path==null) break;
                    String incSub = getInputString("是否搜索子目录", "yes");
                    List<PhotoInfo> photoInfos = photoInfoListIn(path,Util.boolValue(incSub),null);
                    formatCountry(photoInfos,path);
                    done = writeGpxFile(photoInfos,new File(path,".archive.gpx")) > 0;
                    break;
                case "2":
                    path = chooseFolder("选择图像目录",fileFilter);
                    if (path==null) break;
                    incSub = getInputString("是否搜索子目录", "yes");
                    changeDateTime(path, Util.boolValue(incSub));
                    done = true;
                    break;
                case "3":
                    if (!selectGpxInfo()) break;
                    path = chooseFolder("选择图像目录",fileFilter);
                    if (path == null) break;
                    incSub = getInputString("是否搜索子目录(确保所有照片拍摄时区相同)", "yes");
                    List<PhotoInfo> list = photoInfoListIn(path,Util.boolValue(incSub),null).stream().filter(p->
                            p.getMimeType()!=null && p.getMimeType().contains("image") && p.getShootTime()!=null
                                  &&  (p.getLongitude()==null || p.getLatitude()==null)
                           ).collect(Collectors.toList());
                    writeGpxInfo(list,path,gpxPoints);
                    done = true;
                    break;
                case "4":
                    path = chooseFolder("选择图像目录",fileFilter);
                    if (path==null) break;
                    incSub = getInputString("是否搜索子目录", "yes");
                    done = rename(photoInfoListIn(path,Util.boolValue(incSub),null),path);
                    break;
                case "5":
                    path = chooseFolder("选择需要删除子空白目录的主目录",null);
                    if (path==null) break;
                    FileUtil.removeEmptyFolder(new File(path));
                    done = true;
                    break;
                default:
                    System.out.println("错误的选择！");
            }
            if (shutdown && done) {
                CommandRunner.shutdown(10);
                return false;
            }
        }
    }
    private static boolean selectGpxInfo() {
        if (gpxPoints==null || gpxPoints.size()==0) {
            File[] files = selectFiles("选择gpx文件", true, new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".gpx");
                }

                @Override
                public String getDescription() {
                    return null;
                }
            });
            if (files==null) return false;
            String title = getInputString("设置默认标题", "");
            gpxPoints = GpxUtils.readGpxInfo(files,title);
            if (gpxPoints==null || gpxPoints.size()==0) {
                System.out.println("未找到GPS信息");
                return false;
            }
            System.out.println("找到GPS信息数量: "+gpxPoints.size());
        }
        return true;
    }
    private static TimeZone inputTimeZone(String title) {
        return inputTimeZone(title,"GMT+8:00");
    }

    private static List<String> allTimeZoneIds = null;
    private static TimeZone inputTimeZone(String title,String defId) {
        if (allTimeZoneIds==null) allTimeZoneIds = Arrays.asList(TimeZone.getAvailableIDs());
        while (true) {
            String zs = getInputString(title==null?"时区":title, defId);
            if (allTimeZoneIds.contains(zs)) return TimeZone.getTimeZone(zs);
            zs=zs.toUpperCase();
            Pattern p = Pattern.compile("^(GMT)?(\\+|-)?((0?\\d|10|11|12)(:00|:30)?)$");
            Matcher m = p.matcher(zs);
            if (m.find()) {
                zs = "GMT" + (m.group(2)==null || m.group(2).isEmpty() ? "+" : m.group(2)) + m.group(3);
                return TimeZone.getTimeZone(zs);
            } else System.out.println("时区格式错误，请重新输入");
        }
    }

    private static void changeDateTime(String path, boolean incSub) {
        while(true) {
            String zs = getInputString("时间调整值[+/-]HH[:mm[:ss]]", "0");
            if (zs.equals("0")) return;
            Pattern p = Pattern.compile("^(\\+|-)?(\\d{1,2}(:\\d{1,2}(:\\d{1,2})?)?)$");
            Matcher m = p.matcher(zs);
            if (m.find()) {
                String jj = (m.group(1) != null && m.group(1).equals("-") ? "-" : "+");
                try {
                    System.out.println("调整拍摄时间...");
                    Map<String, List<String>> result = incSub ?
                            ExifTool.getInstance().execute(new File(path), "-overwrite_original", "-r", "-AllDates" + jj + "=" + m.group(2)) :
                            ExifTool.getInstance().execute(new File(path), "-overwrite_original", "-AllDates" + jj + "=" + m.group(2));
                    for (String k : result.keySet()) {
                        for (String s : result.get(k)) {
                            System.out.println(s);
                        }
                    }
                } catch (Exception e) {
                }
                return;
            } else System.out.println("格式错误，请重新输入");
        }
    }
    private static int writeGpxFile(List<PhotoInfo> list,File file) {
        return writeGpxFile(list,file,null);
    }
    private static int writeGpxFile(List<PhotoInfo> list,File file, String title) {
        if (list!=null && list.size()>0) {
            list = list.stream().filter(p->p.getShootTime()!=null && p.getLatitude()!=null && p.getLongitude()!=null).collect(Collectors.toList());
            System.out.println("检索地址信息...");
            BaiduGeo.seekAddressInfo(list.stream().filter(p->p.getCountry() == null || p.getCountry().trim().isEmpty()).collect(Collectors.toList()),null);
            if (list!=null && list.size()>0) {
                if (title==null) getInputString("设置默认标题", "");
                int count = GpxUtils.writeGpxInfo(file,list,title);
                if (count>0) System.out.println("写入 "+count + " 条GPS记录到 "+file.getAbsolutePath());
                else System.out.println("写入地理信息数据失败");
                return count;
            } else System.out.println("没有检索到GPS数据");
        }
        return 0;
    }
    private static int writeGpxFile(ArchiveInfo archiveInfo, String folder, boolean incSub) {
        final String subPath = (folder==null ?"" :ArchiveUtils.formatterSubFolder(folder,archiveInfo.getPath()));
        List<PhotoInfo> list = archiveInfo.getInfos().stream().filter(p->
                p.getSubFolder().equals(subPath) || (incSub && p.getSubFolder().startsWith(subPath+File.separator) )).collect(Collectors.toList());
        File gpx = new File(archiveInfo.getPath(),(subPath.isEmpty() ? "" : subPath + File.separator) + ".archive.gpx");
        return writeGpxFile(list,gpx);
    }
    private static Map<String,Object> seekGpxPoints(TreeMap<Long, Map<String,Object>> gpxPoints, long dtValue) {
        long prev = -1;
        Iterator<Long> iterator = gpxPoints.keySet().iterator();
        while (iterator.hasNext()) {
            long utc = iterator.next();
            if (utc == dtValue) return gpxPoints.get(utc);
            else if (utc<dtValue) prev = utc;
            else if (prev == -1) return gpxPoints.get(utc);
            else if (utc - dtValue > dtValue - prev) return gpxPoints.get(prev);
            else return gpxPoints.get(utc);
        }
        if (prev == -1) return null;
        else return gpxPoints.get(prev);
    }
    private static void writeGpxInfo(List<PhotoInfo> list,String path,TreeMap<Long, Map<String,Object>> gpxPoints) {
        if (list!=null && list.size()>0) {
            TimeZone defaultZone = list.get(0).getTimeZone();
            if (defaultZone==null) {
                Map<String, Object> firstPoint = gpxPoints.get(gpxPoints.keySet().iterator().next());
                Object cty = firstPoint.get(Key.getName(Key.COUNTRY_CODE)),
                        state = firstPoint.get(Key.getName(Key.STATE)),
                        city = firstPoint.get(Key.getName(Key.CITY)),
                        lon = firstPoint.get(Key.getName(Key.GPS_LONGITUDE));
                if (Util.isEmpty(cty)) cty = firstPoint.get(Key.getName(Key.COUNTRY));
                defaultZone = TimeZoneTable.getTimeZone(cty == null ? null : (String) cty, state == null ? null : (String) state,
                        city == null ? null : (String) city, lon == null ? null : (Double) lon);
            }
            TimeZone zone = inputTimeZone("拍摄时区",defaultZone==null?"GMT+8:00":defaultZone.getID());
            int timeAdjust = TimeZone.getDefault().getRawOffset() - zone.getRawOffset();
            if (zone.hasSameRules(TimeZone.getDefault())) zone=null;
            System.out.println("图像检索处理中...");
            Map<String,Map<String,Object>> modifications = new HashMap<>();
            for (PhotoInfo pi : list) {
                long dt = pi.getShootTime().getTime() + timeAdjust;
                Map<String,Object> params = seekGpxPoints(gpxPoints,dt);
                if (params!=null && !params.isEmpty()) {
                    Map<String,Object> map = new HashMap<>();
                    map.putAll(params);
                    if (!Util.isEmpty(pi.getSubjectCode())) map.remove(Key.getName(Key.SUBJECT_CODE));
                    if (!Util.isEmpty(pi.getProvince())) map.remove(Key.getName(Key.STATE));
                    if (!Util.isEmpty(pi.getCity())) map.remove(Key.getName(Key.CITY));
                    if (!Util.isEmpty(pi.getLocation())) map.remove(Key.getName(Key.LOCATION));
                    if (!Util.isEmpty(pi.getLongitude())) map.remove(Key.getName(Key.GPS_LONGITUDE));
                    if (!Util.isEmpty(pi.getLatitude())) map.remove(Key.getName(Key.GPS_LATITUDE));
                    if (!Util.isEmpty(pi.getAltitude())) map.remove(Key.getName(Key.GPS_ALTITUDE));
                    if (!Util.isEmpty(pi.getGpsDatetime())) map.remove(Key.getName(Key.GPS_DATETIME));
                    if (!Util.isEmpty(pi.getHeadline())) map.remove(Key.getName(Key.HEADLINE));
                    if (!Util.isEmpty(pi.getArtist())) map.remove(Key.getName(Key.ARTIST));
                    if (!map.isEmpty()) {
                        map.put(Key.getName(Key.GPS_DATETIME),DateUtil.date2String(new Date(dt),"yyyy:MM:dd HH:mm:ss",TimeZone.getTimeZone("UTC"))+"Z");
                        modifications.put(pi.getSubFolder() + (pi.getSubFolder().isEmpty() ? "" : File.separator) + pi.getFileName(),map);
                        pi.setPropertiesBy(map);
                    }
                }
            }
            if (modifications.size()>0) {
                Modification.setExifTags(modifications,path);
                System.out.println("更新gps信息文件数量："+modifications.size());
                return;
            }
        }
        System.out.println("没有需要修改的图像文件");
    }
    private static void addGpsDatetime(ArchiveInfo archiveInfo) {
        List<PhotoInfo> list=archiveInfo.getInfos().stream().filter(p->p.getShootTime()!=null &&
                p.getMimeType()!=null && p.getMimeType().contains("image") &&
                (Util.isEmpty(p.getGpsDatetime()) || Util.isEmpty(p.getCountryCode())) && p.getLongitude()!=null && p.getLatitude()!=null).collect(Collectors.toList());
        Map<String,Map<String,Object>> pathMap = new HashMap<>();
        formatCountry(list,archiveInfo.getPath());
        list.forEach(p->{
            Map<String,Object> map = new HashMap<>();
            map.put(Key.getName(Key.COUNTRY_CODE),p.getCountryCode());
            map.put(Key.getName(Key.COUNTRY),p.getCountry());
            if (Util.isEmpty(p.getGpsDatetime())) {
                String ctr = p.getCountryCode();
                if (ctr==null) ctr = p.getCountry();
                TimeZone zone = TimeZoneTable.getTimeZone(ctr,p.getProvince(),p.getCity(),p.getLongitude());
                if (zone!=null) {
                    String fmt = "yyyy:MM:dd HH:mm:ss";
                    String s= DateUtil.date2String(p.getShootTime(),fmt);
                    Date gpsDt = DateUtil.string2Date(s,fmt,zone);
                    p.setGpsDatetime(DateUtil.date2String(gpsDt,fmt,TimeZone.getTimeZone("UTC"))+"Z");
                    map.put(Key.getName(Key.GPS_DATETIME),p.getGpsDatetime());
                }
            }
            pathMap.put(p.getSubFolder().isEmpty() ? p.getFileName() : (p.getSubFolder()+File.separator+p.getFileName()),map);
        });
        if (!pathMap.isEmpty()) {
            Modification.setExifTags(pathMap,archiveInfo.getPath());
            archiveInfo.saveInfos();
        }
    }
    public static boolean archive() {
        env = Util.getYaml(new File(FileUtil.getCurrentPath(), "pv.yml"));
        ExifTool.FFMPEG = getProperty("photo.ffmpeg", "E:\\Photo\\ffmpeg.exe");
        ExifTool.EXIFTOOL = getProperty("photo.exiftool", "E:\\Photo\\exiftool.exe");
        ExifTool.getInstalledVersion();
        ExifTool.getInstalledFfmpegVersion();
        currentPath = getProperty("photo.root-path", null);
        boolean clear=false, same=false,other=false, removeNotExist=true;
        String rootPath = chooseFolder("选择归档的目录路径(取消操作未归档图像)",null);
        if (rootPath==null) return imageOperations();
        File dir = new File(rootPath);
        if (dir!=null && dir.exists() && dir.isDirectory()) {
            clear = Util.boolValue(getInputString("是否重新完全扫描", "no"));
            same = Util.boolValue(getInputString("将相同文件移到.delete目录", "no"));
            other = Util.boolValue(getInputString("将无法确定拍摄日期的文件移动到.other目录", "no"));
            removeNotExist = Util.boolValue(getInputString("删除不存在的项目", "yes"));
            ArchiveInfo archived = ArchiveUtils.getArchiveInfo(rootPath, clear, same,other);
            if (archived!=null && removeNotExist && archived.removeNotExistInfo()>0) {
                afterChanged(archived);
            }
            if (archived!=null) return archiveWithArchivedInfo(archived);
        }
        return false;
    }
    static public void getRawGpx() {
        String root = "E:\\Photo\\RAW\\";
        for (int i=2010;i<2022;i++) {
            String path = root + i;
            List<PhotoInfo> photoInfos = photoInfoListIn(path, true, null);
            formatCountry(photoInfos,path);
            File [] dirs = new File(path).listFiles(f->f.isDirectory());
            for (File f: dirs) {
                try {
                    writeGpxFile(photoInfos.stream().filter(p->p.getSubFolder().startsWith(f.getName())).collect(Collectors.toList()),
                            new File(f, ".archive.gpx"), "");
                } catch (Exception e) {}
            }
        }
    }
    static String getCountryCodeByPathName(String dir) {
        String ctr="CN";
        if (dir.contains("俄罗斯")) ctr = "RU";
        else if (dir.contains("Hongkong")) ctr = "CN";
        else if (dir.contains("巴厘岛")) ctr = "ID";
        else if (dir.contains("French")) ctr = "FR";
        else if (dir.contains("German")) ctr = "DE";
        else if (dir.contains("HK")) ctr = "CN";
        else if (dir.contains("Italy")) ctr = "IT";
        else if (dir.contains("Swissland")) ctr = "CH";
        else if (dir.contains("201207 Canada")) ctr = "CA";
        else if (dir.contains("Greece")) ctr = "GR";
        else if (dir.contains("柬埔寨")) ctr = "CM";
        else if (dir.contains("南非")) ctr = "ZA";
        else if (dir.contains("温哥华")) ctr = "CA";
        else if (dir.contains("201707 美加")) ctr = "US";
        else if (dir.contains("Canada")) ctr = "CA";
        else if (dir.contains("Australia")) ctr = "AU";
        return ctr;
    }
    static void formatCountry(List<PhotoInfo> photoInfos,String root) {
        photoInfos.forEach(p->{
            if (Util.isEmpty(p.getCountryCode()) && Util.isEmpty(p.getCountry())) {
                String code = getCountryCodeByPathName(p.fullPath(root));
                p.setCountryCode(code);
                p.setCountry(TimeZoneTable.standCountryName(code,false));
            } else if (Util.isEmpty(p.getCountryCode())) {
                p.setCountryCode(TimeZoneTable.standCountryName(p.getCountry(),true));
            } else if (Util.isEmpty(p.getCountry())) {
                p.setCountry(TimeZoneTable.standCountryName(p.getCountryCode(),false));
            }
        });
    }
}
