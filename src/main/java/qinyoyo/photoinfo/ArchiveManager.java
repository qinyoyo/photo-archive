package qinyoyo.photoinfo;

import lombok.NonNull;
import qinyoyo.photoinfo.archive.Modification;
import qinyoyo.photoinfo.archive.ArchiveInfo;
import qinyoyo.photoinfo.archive.PhotoInfo;
import qinyoyo.photoinfo.exiftool.CommandRunner;
import qinyoyo.photoinfo.exiftool.ExifTool;
import qinyoyo.utils.BaiduGeo;
import qinyoyo.utils.FileUtil;
import qinyoyo.utils.Util;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArchiveManager {
    static Map<String, Object> env = null;
    private static String getInputString(String message, String def) {
        String input=def;
        Scanner in = new Scanner(System.in);
        System.out.print(message + (def==null || def.isEmpty() ? ": " : ("(" + def + "): ")));
        input = in.nextLine().trim();
        if (input.isEmpty()) input=def;
        return input;
    }

    private static String chooseFolder(String title, String current) {
        int result = 0;
        String path = null;
        JFileChooser fileChooser = new JFileChooser();
        FileSystemView fsv = FileSystemView.getFileSystemView();  //注意了，这里重要的一句
        fileChooser.setCurrentDirectory(current==null || current.isEmpty() ? fsv.getHomeDirectory() : new File(current));
        fileChooser.setDialogTitle(title==null || title.isEmpty() ? "请选择目录" : title);
        fileChooser.setApproveButtonText("确定");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        result = fileChooser.showOpenDialog(null);
        if (JFileChooser.APPROVE_OPTION == result) {
            path=fileChooser.getSelectedFile().getPath();
            return path;
        } else return null;
    }

    private static String inputSubFolder(String title, String rootPath) {
        String path = chooseFolder(title, rootPath);
        if (path==null) return null;
        path = ArchiveUtils.formatterSubFolder(path,rootPath);
        return path;
    }
    private static void afterChanged(ArchiveInfo archiveInfo) {
        archiveInfo.sortInfos();
        archiveInfo.saveInfos();
    }
    static String getProperty(Map<String, Object> env, @NonNull String key,String def) {
        if (env==null) return def;
        String [] kk = key.split("\\.");
        for (int i=0;i < kk.length; i++) {
            Object obj = env.get(kk[i]);
            if (obj==null) return def;
            else if (i==kk.length-1) return obj.toString();
            else if (obj instanceof Map) env = (Map<String, Object>)obj;
        }
        return def;
    }
    static boolean getProperty(Map<String, Object> env, @NonNull String key,boolean def) {
        String s = getProperty(env,key,def?"true":"false");
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

    static boolean archiveWithArchivedInfo(@NonNull ArchiveInfo archived) {
        String currentPath = FileUtil.getCurrentPath();
        boolean shutdown = false;
        final String rootPath = archived.getPath();
        String menuString = "1 重建缩略图文件\n" +
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
                    path = chooseFolder("输入需要同步的RAW目录", currentPath);
                    if (path==null) break;
                    currentPath = path;
                    List<String> args = new ArrayList<String>() {{
                        add("--ext");
                        add("xmp");
                    }};
                    ArchiveUtils.syncExifAttributesByTime(archived.getInfos(), photoInfoListIn(path,false), path);
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
                    path = chooseFolder("输入待归档的目录", currentPath);
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
                    path = inputSubFolder("选择需要重新命名的目录(不含子目录)",rootPath);
                    if (path==null) break;
                    final String temPath = path;
                    if (rename(archived.getInfos().stream().filter(p->p.getSubFolder().equals(temPath)).collect(Collectors.toList()), archived.getPath())) {
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
                    break;
                case "a":
                    List<Modification> list = Modification.read(rootPath);
                    if (list!=null) {
                        System.out.println("同步修改...");
                        Modification.execute(list,archived);
                        Modification.resetSyncAction(rootPath);
                        afterChanged(archived);
                        System.out.println("同步修改完成.");
                    }
                    break;
                case "b":
                    path = inputSubFolder("选择需要归档的目录(不包含子目录)",rootPath);
                    if (path==null) break;
                    writeGpxFile(archived,path);
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
    static List<PhotoInfo> photoInfoListIn(String path,boolean includeSubFolder) {
        List<PhotoInfo> list=ArchiveUtils.seekPhotoInfosInFolder(new File(path),path,includeSubFolder,null);
        ArchiveUtils.defaultSort(list);
        return list;
    }
    static boolean imageOperations() {
        String currentPath = FileUtil.getCurrentPath();
        boolean shutdown = false;
        TreeMap<Long, Map<String,Object>> gpxPoints = null;
        String menuString =
                "操作对象均不包含子目录下图像\n" +
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
                    path = chooseFolder("选择图像目录",currentPath);
                    if (path==null) break;
                    currentPath = path;
                    writeGpxFile(photoInfoListIn(path,false),new File(path,".archive.gpx"));
                    done = true;
                    break;
                case "2":
                    path = chooseFolder("选择图像目录",currentPath);
                    if (path==null) break;
                    currentPath = path;
                    changeDateTime(path);
                    done = true;
                    break;
                case "3":
                    if (gpxPoints.size()==0) {
                        String gpxPath = chooseFolder("选择gpx文件所在目录",currentPath);
                        String title = getInputString("设置默认标题", "");
                        gpxPoints = GpxUtils.readGpxInfo(new File(gpxPath),title);
                    }
                    if (gpxPoints.size()>0) {
                        path = chooseFolder("选择图像目录", currentPath);
                        if (path == null) break;
                        currentPath = path;
                        writeGpxInfo(path,gpxPoints);
                        done = true;
                    }
                    break;
                case "4":
                    path = chooseFolder("选择图像目录", currentPath);
                    if (path==null) break;
                    currentPath = path;
                    done = rename(photoInfoListIn(path,false),path);
                    break;
                case "5":
                    path = chooseFolder("选择需要删除子空白目录的主目录", currentPath);
                    if (path==null) break;
                    currentPath = path;
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
    private static TimeZone inputTimeZone(String title) {
        while (true) {
            String zs = getInputString(title==null?"时区":title, "GMT+8:00").toUpperCase();
            Pattern p = Pattern.compile("^(GMT)?(\\+|-)?((0?\\d|10|11|12)(:00|:30)?)$");
            Matcher m = p.matcher(zs);
            if (m.find()) {
                zs = "GMT" + (m.group(2)==null || m.group(2).isEmpty() ? "+" : m.group(2)) + m.group(3);
                TimeZone zone = TimeZone.getTimeZone(zs);
                System.out.println(zone.getID());
                return zone;
            } else System.out.println("时区格式错误，请重新输入");
        }
    }

    private static void changeDateTime(String path) {
        String zs = getInputString("时间调整值[+/-]HH[:mm[:ss]]", "0");
        if (zs.equals("0")) return;
        Pattern p = Pattern.compile("^(\\+|-)?(\\d{1,2}(:\\d{1,2}(:\\d{1,2})?)?)$");
        Matcher m = p.matcher(zs);
        if (m.find()) {
            String jj = (m.group(1) != null && m.group(1).equals("-") ? "-" : "+");
            try {
                Map<String, List<String>> result = ExifTool.getInstance().execute(new File(path), "-overwrite_original", "-AllDates" + jj + "=" + m.group(2));
                if (ExifTool.updatesFiles(result)>0) {
                    ArchiveUtils.clearArchiveInfoFile(path);
                }
                for (String k : result.keySet()) {
                    for (String s: result.get(k)) {
                        System.out.println(s);
                    }
                }
            } catch (Exception e) {}
        } else System.out.println("格式错误，请重新输入");
    }

    private static void writeGpxFile(List<PhotoInfo> list,File file) {
        if (list!=null && list.size()>0) {
            list = list.stream().filter(p->p.getShootTime()!=null && p.getLatitude()!=null && p.getLongitude()!=null).collect(Collectors.toList());
            BaiduGeo.seekAddressInfo(list.stream().filter(p->p.getProvince() == null && p.getCity() == null && p.getLocation() == null && p.getCountry() == null).collect(Collectors.toList()));
            if (list!=null && list.size()>0) {
                TimeZone zone = inputTimeZone("拍摄时区");
                String title = getInputString("设置默认标题", "");
                GpxUtils.writeGpxInfo(file,list,title,zone);
            }
        }
    }
    private static void writeGpxFile(ArchiveInfo archiveInfo, String folder) {
        final String subPath = (folder==null ?"" :ArchiveUtils.formatterSubFolder(folder,archiveInfo.getPath()));
        List<PhotoInfo> list = archiveInfo.getInfos().stream().filter(p->p.getSubFolder().equals(subPath)).collect(Collectors.toList());
        File gpx = new File(archiveInfo.getPath(),(subPath.isEmpty() ? "" : subPath + File.separator) + ".archive.gpx");
        writeGpxFile(list,gpx);
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
    private static void writeGpxInfo(String path,TreeMap<Long, Map<String,Object>> gpxPoints) {
        List<PhotoInfo> list = photoInfoListIn(path,false).stream().filter(p->
                p.getMimeType()!=null && p.getMimeType().contains("image") && p.getShootTime()!=null &&
                        (p.getLongitude()==null || p.getLatitude()==null))
                .collect(Collectors.toList());
        if (list!=null && list.size()>0) {
            TimeZone zone = inputTimeZone("拍摄时区");
            int timeAdjust = TimeZone.getDefault().getRawOffset() - zone.getRawOffset();
            if (zone.hasSameRules(TimeZone.getDefault())) zone=null;
            Map<String,Map<String,Object>> modifications = new HashMap<>();
            for (PhotoInfo pi : list) {
                long dt = pi.getShootTime().getTime() + timeAdjust;
                Map<String,Object> params = seekGpxPoints(gpxPoints,dt);
                if (params!=null && !params.isEmpty()) {
                    modifications.put(pi.getSubFolder() + (pi.getSubFolder().isEmpty() ? "" : File.separator) + pi.getFileName(),params);
                } else break;
            }
            if (modifications.size()>0) {
                Modification.execute(modifications,path);
            }
        }
    }
    public static boolean archive() {
        env = Util.getYaml(new File(FileUtil.getCurrentPath(), "pv.yml"));
        ExifTool.FFMPEG = getProperty(env,"photo.ffmpeg", "E:\\Photo\\ffmpeg.exe");
        ExifTool.EXIFTOOL = getProperty(env,"photo.exiftool", "E:\\Photo\\exiftool.exe");
        String rootInput = getProperty(env,"photo.root-path", null);
        boolean clear=false, same=false,other=false, removeNotExist=true;
        rootInput = chooseFolder("选择归档的目录路径(取消操作未归档图像)", rootInput);
        if (rootInput==null) return imageOperations();
        File dir = new File(rootInput);
        if (dir!=null && dir.exists() && dir.isDirectory()) {
            clear = Util.boolValue(getInputString("是否重新完全扫描", "no"));
            same = Util.boolValue(getInputString("将相同文件移到.delete目录", "no"));
            other = Util.boolValue(getInputString("将无法确定拍摄日期的文件移动到.other目录", "no"));
            removeNotExist = Util.boolValue(getInputString("删除不存在的项目", "yes"));
            ArchiveInfo archived = ArchiveUtils.getArchiveInfo(rootInput, clear, same,other);
            if (archived!=null && removeNotExist && archived.removeNotExistInfo()>0) {
                afterChanged(archived);
            }
            if (archived!=null) return archiveWithArchivedInfo(archived);
        }
        return false;
    }
}
