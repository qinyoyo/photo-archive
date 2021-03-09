package qinyoyo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SpringContextUtil implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(SpringContextUtil.class);
    private static ApplicationContext applicationContext = null;
    private static String projectHomeDirection = null;
    private static String propertyFile = null;

    public static void setProjectHomeDirection(String path) {
        projectHomeDirection = path;
    }
    public static void setPropertyFile(String path) {
        propertyFile = path;
    }
    public static String getProjectHomeDirection() {
        if (projectHomeDirection==null) initProjectHomeDiretion(SpringContextUtil.class);
        return projectHomeDirection;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
        if (projectHomeDirection==null) initProjectHomeDiretion(SpringContextUtil.class);
        log.info("Home direction = {}", projectHomeDirection);
        log.info("User additional properties file = {}", propertyFile==null ? "<none>" : propertyFile);
    }

    private static void initProjectHomeDiretion(@NonNull Class<?> clazz) {
        if ("file".equals(clazz.getResource("").getProtocol())) {  // ide模式，使用上級目錄
            String path = FileUtil.getPath(clazz);
            if (path.endsWith("\\target\\classes") || path.endsWith("/target/classes")) {
                File hd = new File(path).getParentFile().getParentFile();
                try {
                    setProjectHomeDirection(hd.getCanonicalPath());
                } catch (Exception e){ Util.printStackTrace(e);}
            } else {
                setProjectHomeDirection(FileUtil.getPath(null));
            }
        } else {
            setProjectHomeDirection(FileUtil.getPath(null));
        }
    }
}
