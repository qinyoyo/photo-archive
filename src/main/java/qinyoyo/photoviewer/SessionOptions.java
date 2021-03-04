package qinyoyo.photoviewer;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.env.Environment;
import qinyoyo.photoinfo.exiftool.Key;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SessionOptions {
    public boolean debug;
    private boolean htmlEditable;
    private boolean favoriteFilter;
    private String rangeExif;
    private String rangeExifNote;
    private int loopTimer;
    private int musicIndex;
    private boolean unlocked;
    private boolean playBackMusic;
    private boolean mobile;
    private boolean supportOrientation;
    public SessionOptions() {
        debug = false;
        htmlEditable = false;
        favoriteFilter = false;
        rangeExif = Key.getName(Key.SUBJECT_CODE);
        rangeExifNote = Key.getNotes(Key.SUBJECT_CODE);
        loopTimer = 5000;
        musicIndex = 0;
        unlocked = false;
        playBackMusic = true;
        mobile = false;
        supportOrientation = false;
    }
    public void setRangeTag(Key key) {
        if (key==null) {
            rangeExif = null;
            rangeExifNote = "未设置";
        } else {
            rangeExif = Key.getName(key);
            rangeExifNote = Key.getNotes(key);
        }
    }
    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
    private static Map<String,SessionOptions> allSessions = new HashMap<>();
    public static SessionOptions getSessionOptions(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session!=null) {
            String id = session.getId();
            SessionOptions options = allSessions.get(id);
            if (options==null) {
                options = new SessionOptions();
                String userAgent = request.getHeader("USER-AGENT");
                options.mobile = userAgent.contains("Mobile") || userAgent.contains("Phone");
                if (options.mobile) {
                    Environment env = SpringContextHolder.getBean(Environment.class);
                    if (env != null) {
                        String browsers = env.getProperty("photo.support-orientation");
                        if (browsers != null) {
                            String[] bs = browsers.split(",");
                            for (String b : bs) {
                                if (userAgent.contains(b)) {
                                    options.supportOrientation = true;
                                    break;
                                }
                            }
                        }
                    }
                } else options.supportOrientation = true;
                allSessions.put(id,options);
            }
            return options;
        }
        return new SessionOptions();
    }
}
