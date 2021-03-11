package qinyoyo.photoinfo;

import qinyoyo.photoinfo.ArchiveUtils;
import qinyoyo.photoinfo.archive.Modification;
import qinyoyo.photoinfo.archive.ArchiveInfo;
import qinyoyo.photoinfo.archive.PhotoInfo;
import qinyoyo.photoinfo.exiftool.CommandRunner;
import qinyoyo.utils.FileUtil;
import qinyoyo.utils.Util;

import java.io.File;
import java.util.*;

public class ArchiveManager {

    public static String getInputString(String message, String def) {
        String input=null;
        Scanner in = new Scanner(System.in);
        while(input==null || input.isEmpty()) {
            System.out.print(message + (def==null || def.isEmpty() ? ":" : ("(" + def + "):")));
            input = in.nextLine().trim();
            if (input.isEmpty()) input=def;
        }
        return input;
    }
    public static String inputSubFolder(String rootEndWithSeparator) {
        String path = ArchiveUtils.formatterSubFolder(getInputString("归档目录 "+rootEndWithSeparator+" 下的一个子目录(空表示改归档目录)：", ""));
        if (path.startsWith(rootEndWithSeparator)) path = path.substring(rootEndWithSeparator.length());
        return path;
    }
    public static void afterChanged(ArchiveInfo archiveInfo) {
        archiveInfo.sortInfos();
        archiveInfo.saveInfos();
    }
    public static boolean archive() {
        File dir=null;
        String rootInput = "E:\\Photo\\Archived";
        boolean clear=false, same=false,other=false;
        while(dir==null || !dir.exists() || !dir.isDirectory()) {
            rootInput = getInputString("输入已经归档的目录路径", "E:\\Photo\\Archived");
            if (rootInput.isEmpty() || rootInput.equals("-")) break;
            dir = new File(rootInput);
            if (dir!=null && dir.exists() && dir.isDirectory()) {
                clear = Util.boolValue(getInputString("是否重新完全扫描", "no"));
                same = Util.boolValue(getInputString("将相同文件移到.delete目录", "no"));
                other = Util.boolValue(getInputString("将无法确定拍摄日期的文件移动到.other目录", "no"));
            }
        }
        ArchiveInfo archived=ArchiveUtils.getArchiveInfo(rootInput, clear, same,other);
        if (archived==null) return false;
        boolean shutdown = false;
        String rootEndWithSeparator = archived.getPath()+File.separator;
        final String rootPath = archived.getPath();
        while (true) {
            boolean done = false;
            String path = "";
            String input = getInputString(
                    "1 重建缩略图文件\n" +
                            "2 同步缩略图方向属性\n" +
                            "3 将属性写入到RAW文件\n" +
                            "4 重新扫描子目录\n" +
                            "5 将一个目录合并入归档\n" +
                            "6 重新命名目录文件\n" +
                            "7 删除空目录\n" +
                            "8 重新格式化游记文件\n" +
                            "s 启动浏览服务\n" +
                            "0 下一个操作之后关机\n" +
                            "q 退出\n请选择一个操作", "");
            switch (input) {
                case "s": return true;
                case "q": return false;
                case "0":
                    shutdown = true;
                    break;
                case "1":
                    path = inputSubFolder(rootEndWithSeparator);
                    archived.createThumbFiles(path);
                    done = true;
                    break;
                case "2":
                    path = inputSubFolder(rootEndWithSeparator);;
                    ArchiveUtils.syncThumbOrientation(archived, path);
                    done = true;
                    break;
                case "3":
                    path = getInputString("输入需要同步的RAW目录绝对路径：", "");
                    List<String> args = new ArrayList<String>() {{
                        add("--ext");
                        add("xmp");
                    }};
                    ArchiveInfo raw = new ArchiveInfo(path, args);
                    raw.sortInfos();
                    raw.saveInfos();
                    ArchiveUtils.syncExifAttributesByTime(archived, raw);
                    raw.saveInfos();
                    done = true;
                    break;
                case "4":
                    path = inputSubFolder(rootEndWithSeparator);;
                    if (path != null && !path.isEmpty()) {
                        if (Modification.scanAction(path, archived))
                            Modification.save(new Modification(Modification.Scan, path, null), archived.getPath());
                        System.out.println("完成目录重新扫描 " + path);
                        afterChanged(archived);
                        done = true;
                    }
                    break;
                case "5":
                    path = getInputString("输入待归档的绝对目录路径", "");
                    if (!path.isEmpty()) {
                        dir = new File(path);
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
                    path = inputSubFolder(rootEndWithSeparator);
                    String pat = "";
                    while (pat==null || pat.isEmpty()) {
                        pat = getInputString("文件名格式(输入空显示提示)", PhotoInfo.RENAME_PATTERN);
                        if (pat==null || pat.isEmpty()) {
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
                    List<PhotoInfo> infos = archived.subFolderInfos(path);
                    if (infos!=null) {
                        for (PhotoInfo pi : infos) {
                            try {
                                String name0 = pi.getFileName();
                                pi.rename(archived.getPath(), pat);
                                String name1 = pi.getFileName();
                                if (name1.equals(name0)) System.out.println(name0 + " 没有重命名");
                                else System.out.println(name0 + " 重命名为 " + name1);
                            } catch (Exception e){ Util.printStackTrace(e);}
                        }
                        afterChanged(archived);
                        done = true;
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
                default:
                    System.out.println("错误的选择！");
            }
            if (shutdown && done) {
                CommandRunner.shutdown(10);
                return false;
            }
        }
    }
}
