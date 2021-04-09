package qinyoyo.photoinfo.archive;

import lombok.Getter;
import qinyoyo.photoinfo.exiftool.ExifTool;
import qinyoyo.photoinfo.exiftool.Key;
import qinyoyo.utils.Util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
V: Mirror vertical
H: Mirror horizontal
Rxx : Rotate xx CW
R-xx: Rotate xx AW
    V + H = H + V = R180 = 3
    H + R180 = V = 4
    V + R180 = H = 2
    V + R90 = H R270 = 5
    V + R270 = H + R90 = 7
    V + Rxx = R(360-xx) + V
    H + Rxx = R(360-xx) + H
    Rxx = R(xx-360) = R-(360-xx)
*/
@Getter
public enum Orientation {
    UNKNOWN("Unknown",0),
    NONE("Horizontal (normal)",1),
    M_H("Mirror horizontal",2),
    R180("Rotate 180",3),
    M_V( "Mirror vertical",4),
    M_H_R270("Mirror horizontal and rotate 270 CW",5),
    R90("Rotate 90 CW",6),
    M_H_R90("Mirror horizontal and rotate 90 CW",7),
    R270("Rotate 270 CW",8);
    private static final int [] [] orientationArrays = new int [][] {
    // ori0 + ori1 = orientationArrays[ori0-2][ori1-2]
    // ori1= M_H.value          R180.value      M_V.value       M_H_R270.value  R90.value       M_H_R90.value   R270.value
            {NONE.value,        M_V.value,      R180.value,     R270.value,     M_H_R90.value,  R90.value,      M_H_R270.value},    // ori0 = M_H.value
            {M_V.value,         NONE.value,     M_H.value,      M_H_R90.value,  R270.value,     M_H_R270.value, R90.value},         // ori0 = R180.value
            {R180.value,        M_H.value,      NONE.value,     R90.value,      M_H_R270.value, R270.value,     M_H_R90.value},     // ori0 = M_V.value
            {R90.value,         M_H_R90.value,  R270.value,     NONE.value,     M_H.value,      R180.value,     M_V.value},         // ori0 = M_H_R270.value
            {M_H_R270.value,    R270.value,     M_H_R90.value,  M_V.value,      R180.value,     M_H.value,      NONE.value},        // ori0 = R90.value
            {R270.value,        M_H_R270.value, R90.value,      R180.value,     M_V.value,      NONE.value,     M_H.value},         // ori0 = M_H_R90.value
            {M_H_R90.value,     R90.value,      M_H_R270.value, M_H.value,      NONE.value,     M_V.value,      R180.value}         // ori0 = R270.value
    };

    private final String name;
    private final int value;
    Orientation(String name, int value) {
        this.name=name;
        this.value=value;
    }
    public static Orientation get(int ori) {
        switch (ori) {
            case 0: return UNKNOWN;
            case 1: return NONE;
            case 2: return M_H;
            case 3: return R180;
            case 4: return M_V;
            case 5: return M_H_R270;
            case 6: return R90;
            case 7: return M_H_R90;
            case 8: return R270;
            default : return null;
        }
    }
    public static String name(int ori) {
        switch (ori) {
            case 2: return M_H.name;
            case 3: return R180.name;
            case 4: return M_V.name;
            case 5: return M_H_R270.name;
            case 6: return R90.name;
            case 7: return M_H_R90.name;
            case 8: return R270.name;
            default : return NONE.name;
        }
    }
    public static int value(String name) {
        if (name==null) return NONE.value;
        try {
            int i = Integer.parseInt(name);
            if (i>=NONE.value && i<=R270.value) return i;
        } catch (Exception e){}
        if (name.equals(M_H.name)) return M_H.value;
        else if (name.equals(R180.name)) return R180.value;
        else if (name.equals(M_V.name)) return M_V.value;
        else if (name.equals(M_H_R270.name)) return M_H_R270.value;
        else if (name.equals(R90.name)) return R90.value;
        else if (name.equals(M_H_R90.name)) return M_H_R90.value;
        else if (name.equals(R270.name)) return R270.value;
        else if (name.equals(NONE.name)) return NONE.value;
        else return NONE.value;
    }
    public static int add(int ori0, int ori1) {
        if (ori0 < M_H.value || ori0 > R270.value) return ori1;
        if (ori1 < M_H.value || ori1 > R270.value) return ori0;
        return orientationArrays[ori0-M_H.value][ori1-M_H.value];
    }
    public static int by(Integer ... oris) {
        if (oris==null || oris.length==0) return 1;
        int r = oris[0] == null ? 1 : oris[0];
        if (r<1 || r>8) r=1;
        for (int i=1;i<oris.length;i++) {
            if (oris[i]==null) continue;
            int ori = oris[i];
            if (ori>=2 && ori<=8) {
                if (r==1) r=ori;
                else r = add(r,ori);
            }
        }
        return r;
    }
    public static boolean equals(Integer o0,Integer o1) {
        if (o0==null && o1==null) return true;
        else if (o0==null) return o1.intValue()==NONE.value;
        else if (o1==null) return o0.intValue()==NONE.value;
        else return o0.equals(o1);
    }
    public static boolean setOrientationAndRating(File imgFile, Integer orientation, Integer rating) {
        Map<Key, Object> attrs = new HashMap<>();
        if (orientation!=null) attrs.put(Key.ORIENTATION, orientation);
        if (rating!=null) attrs.put(Key.RATING, rating);
        return ExifTool.getInstance().update(imgFile,attrs, true);
    }
    public static Integer getOrientation(File imgFile) {
        try {
            Map<String, List<String>> result = ExifTool.getInstance().execute(imgFile, "-n","-T", "-orientation");
            List<String> msgList = result.get(ExifTool.RESULT);
            if (msgList==null || msgList.size()==0) return null;
            return value(msgList.get(0));
        } catch (IOException e){ Util.printStackTrace(e);}
        return null;
    }
}
