package qinyoyo.photoviewer;

import lombok.Getter;
import lombok.Setter;
import tang.qinyoyo.exiftool.Key;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SessionOptions {
    private boolean isDebug;
    private boolean htmlEditable;
    private boolean favoriteFilter;
    private Key rangeExif;
    private int loopTimer;
    private int musicIndex;
    private boolean unlocked;
    private boolean playBackMusic;
    public SessionOptions() {
        isDebug = false;
        htmlEditable = false;
        favoriteFilter = false;
        rangeExif = Key.SUBJECT_CODE;
        loopTimer = 5000;
        musicIndex = 0;
        unlocked = false;
        playBackMusic = true;
    }
    private static Map<String,SessionOptions> allSessions = new HashMap<>();
    public static SessionOptions getSessionOptions(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session!=null) {
            String id = session.getId();
            SessionOptions options = allSessions.get(id);
            if (options==null) {
                options = new SessionOptions();
                allSessions.put(id,options);
            }
            return options;
        }
        return new SessionOptions();
    }
}
