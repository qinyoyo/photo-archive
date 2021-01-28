package qinyoyo.photoviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import tang.qinyoyo.ArchiveUtils;
import tang.qinyoyo.archive.ArchiveInfo;
import tang.qinyoyo.archive.FolderInfo;
import tang.qinyoyo.archive.PhotoInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SamePhotoController {
    @Autowired
    PVController pvController;
    @RequestMapping(value = "same")
    public String sameView(Model model) {
        if (!pvController.isDataReady()) {
            model.addAttribute("message","Not ready!!!");
            return "error";
        }
        model.addAttribute("separator", File.separator);
        List<Map<String,Object>> list = new ArrayList<>();
        BufferedReader ins=null;
        StringBuilder sb=new StringBuilder();
        String rootPath = pvController.getRootPath();
        ArchiveInfo archiveInfo = pvController.getArchiveInfo();
        try {
            File logFile = new File(rootPath, ".same_photo.log");
            ins = new BufferedReader(new InputStreamReader(new FileInputStream(logFile),"GBK"));
            String line = null;
            while ((line=ins.readLine())!=null) {
                String [] ll= line.split(" <-> ");
                if (ll.length==2) {
                    String f1=ll[0].trim(), f2 = ll[1].trim();
                    File file1=new File(f1);
                    File file2=new File(f2);
                    if (!f1.isEmpty() && !f2.isEmpty() && f1.startsWith(rootPath) && f2.startsWith(rootPath)) {
                        if (file1.exists() && file2.exists()) {
                            PhotoInfo p1=archiveInfo.find(file1), p2=archiveInfo.find(file2);
                            sb.append(line).append("\r\n");
                            list.add(new HashMap<String, Object>() {{
                                put("same1", f1.substring(rootPath.length() + 1));
                                put("title1", (p1==null?new PhotoInfo(rootPath,file1):p1).toString());
                                put("same2", f2.substring(rootPath.length() + 1));
                                put("title2", (p2==null?new PhotoInfo(rootPath,file2):p2).toString());
                            }});
                        }
                    }
                }
            }
            ins.close();
            if (sb.length()==0) logFile.delete();
            else ArchiveUtils.writeToFile(logFile,sb.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        model.addAttribute("sames",list);;
        return "same_view";
    }
    private boolean deleteFile(File file) {
        if (file.exists()) {
            try {
                String rootPath = pvController.getRootPath();
                String fp = file.getCanonicalPath();
                if (fp.indexOf(rootPath) == 0) {
                    return new PhotoInfo(rootPath, file).delete(rootPath);
                } else return file.delete();
            } catch (Exception e) {}
        }
        return false;
    }
    boolean autoDeleteSame(File file1, File file2) {
        try {
            if (file1.exists() && file2.exists()) {
                String f1 = file1.getCanonicalPath(), f2 = file2.getCanonicalPath();
                String s_d = File.separator + ".delete" + File.separator;
                String s_c = File.separator + FolderInfo.DEFPATH + File.separator;
                if (f1.contains(s_d)) return deleteFile(file1);
                else if (f2.contains(s_d)) return deleteFile(file2);
                else if (f1.contains(s_c)) return deleteFile(file1);
                else if (f2.contains(s_c)) return deleteFile(file2);
                else if (file1.length() <= file2.length()) return deleteFile(file1);
                else return deleteFile(file2);
            }
        }catch (Exception e) {}
        return false;
    }



    @ResponseBody
    @RequestMapping(value = "delete-file")
    public String deleteFile(HttpServletRequest request, HttpServletResponse response, String path) {
        if (!pvController.isDataReady()) {
            return "error";
        }
        if (path==null) return null;
        String rootPath = pvController.getRootPath();
        if (new PhotoInfo(rootPath,new File(rootPath + File.separator + path)).delete(rootPath)) {
            return "ok";
        }
        return "error";
    }

    @ResponseBody
    @RequestMapping(value = "save-file")
    public String saveFile(HttpServletRequest request, HttpServletResponse response, String path) {
        if (!pvController.isDataReady()) {
            return "error";
        }
        if (path==null) return null;
        String [] ll= path.split(" <-> ");
        String rootPath = pvController.getRootPath();
        if (ll.length==2) {
            BufferedReader ins=null;
            StringBuilder sb=new StringBuilder();
            for (String fn : ll) {
                if (fn.startsWith(".delete"+File.separator)) {
                    try {
                        Files.move(new File(rootPath, fn).toPath(),new File(rootPath, fn.substring(8)).toPath(), StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                    }
                }
            }
            try {
                File logFile = new File(rootPath, ArchiveInfo.same_photo_log);
                ins = new BufferedReader(new InputStreamReader(new FileInputStream(logFile),"GBK"));
                String line = null;
                boolean found= false;
                while ((line=ins.readLine())!=null) {
                    if (found) sb.append(line).append("\r\n");
                    else if (line.contains(ll[0]) && line.contains(ll[1])) found=true;
                    else sb.append(line).append("\r\n");
                }
                ins.close();
                if (sb.length()==0) logFile.delete();
                else ArchiveUtils.writeToFile(logFile,sb.toString());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }
        }
        return "ok";
    }

    @ResponseBody
    @RequestMapping(value = "auto-delete-same")
    public String autoDeleteSame(HttpServletRequest request, HttpServletResponse response) {
        BufferedReader ins=null;
        StringBuilder sb=new StringBuilder();
        try {
            String rootPath = pvController.getRootPath();
            File logFile = new File(rootPath, ArchiveInfo.same_photo_log);
            ins = new BufferedReader(new InputStreamReader(new FileInputStream(logFile),"GBK"));
            String line = null;
            while ((line=ins.readLine())!=null) {
                String [] ll= line.split(" <-> ");
                if (ll.length==2) {
                    String f1=ll[0].trim(), f2 = ll[1].trim();
                    File file1=new File(f1);
                    File file2=new File(f2);
                    if (!autoDeleteSame(file1,file2)) sb.append(line).append("\r\n");
                }
            }
            ins.close();
            if (sb.length()==0) logFile.delete();
            else ArchiveUtils.writeToFile(logFile,sb.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
        return "ok";
    }
}
