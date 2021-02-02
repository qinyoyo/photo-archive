package qinyoyo.photoviewer;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import tang.qinyoyo.ArchiveUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Modification {
    public static final int Rating_Orientation  = 1;
    public static final int Remove      = 2;
    public static final String save_path = ".modification.dat";
    public static String rootPath;
    int action;
    String path;
    Map<String,Object> params;
    Modification(int action,String path,Map<String,Object> params) {
        this.action = action;
        this.path = path;
        this.params = params;
    }
    @Override
    public String toString() {
        /*return "{" + "\"action\"" + ": " + action + ", " +
                "\"path\"" + ": \"" + (path==null?"":path) + "\", " +
                "\"params\"" + ": \"" + (params==null?"":params) + "\"" + "}"; */
        JSONObject json = new JSONObject(this);
        return json.toString();
    }
    public static void setRootPath(String rootPath) {
        Modification.rootPath = rootPath;
    }

    public static void save(Modification mod) {
        ArchiveUtils.appendToFile(new File(rootPath,save_path), mod.toString());
    }
    public static List<Modification> read() {
        String actions = ArchiveUtils.getFromFile(new File(rootPath,save_path+".sync"));
        if (actions==null) return null;
        String [] aa = actions.split("\n");
        List<Modification> list=new ArrayList<>();
        for (String s: aa) {
            s=s.trim();
            if (s.startsWith("{") && s.endsWith("}")) {
                try {
                    JSONObject json = new JSONObject(s);
                    int action = json.getInt("action");
                    String path = json.has("path") ? json.getString("path") : null;
                    Map<String,Object> params = json.has("params") ? json.getJSONObject("params").toMap() : null;
                    Modification m = new Modification(action,path,params);
                    list.add(m);
                } catch (Exception e) {}
            }
        }
        return list;
    }
    public static void resetSyncAction() {
        new File(rootPath,save_path+".sync").delete();
    }
}
