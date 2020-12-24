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

    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
        if (projectHomeDirection==null) initProjectHomeDiretion(SpringContextUtil.class);
        log.info("Home direction = {}", projectHomeDirection);
        log.info("User additional properties file = {}", propertyFile==null ? "<none>" : propertyFile);
    }

    //通过name获取 Bean.
    public static Object getBean(String name) {
        try {
            return getApplicationContext().getBean(name);
        } catch (Exception e) {
            return null;
        }
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        try {
            return getApplicationContext().getBean(clazz);
        } catch (Exception e) {
            return null;
        }
    }
    public static <T> List<T> getImplementedBean(Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        Map<String, T> result = applicationContext.getBeansOfType(clazz);
        int p = clazz.getName().lastIndexOf(".");
        String myName = StringUtil.firstLetterLower(p >= 0 ? clazz.getName().substring(p + 1) : clazz.getName());
        for (String k : result.keySet()) {
            if (k.equals(myName)) continue;
            T t = result.get(k);
            list.add(t);
        }
        return list;
    }
    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 获得当前对话的 HttpServletRequest
     *
     * @return 获得当前对话的 HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            return request;
        } else return null;
    }

    public static HttpServletResponse getResponse() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getResponse();
            return response;
        } else return null;
    }
    /**
     * 获取当前对话
     *
     * @return 获取当前对话
     */
    public static HttpSession getSession() {
        if (getRequest()==null) return null;
        HttpSession session = getRequest().getSession();
        return session;
    }

    /**
     * 返回当前对话对应键值的属性值
     *
     * @param key 键值
     * @return 返回当前对话对应键值的属性值
     */
    public static Object getSessionAttribute(String key) {
        HttpSession session = getSession();
        if (session != null) return session.getAttribute(key);
        else return null;
    }

    /**
     * 设置当前对话的键值属性
     *
     * @param key  键值
     * @param attr 设置当前对话的键值属性
     */
    public static void setSessionAttribute(String key, Object attr) {
        HttpSession session = getSession();
        if (session != null) session.setAttribute(key, attr);
    }

    /**
     * 删除当前对话的键值属性
     *
     * @param key 键值
     */
    public static void removeSessionAttribute(String key) {
        HttpSession session = getSession();
        if (session != null) session.removeAttribute(key);
    }

    /**
     * 获得会话处理进度
     *
     * @return 当前进度, 0-100
     */
    public static Integer getProgress() {
        Object o = getSessionAttribute("progress");
        if (o != null) {
            try {
                int i = Integer.parseInt(o.toString());
                if (i >= 0 && i <= 100) return i;
            } catch (Exception e) {
                return 100;
            }
        }
        return 100;
    }

    /**
     * 设置会话的处理进度
     *
     * @param p 进度，0-100
     */
    public static void setProgress(Integer p) {
        if (p != null && p >= 0 && p <= 100) setSessionAttribute("progress", p);
        else setSessionAttribute("progress", null);
    }
    /**
     * 获得contextPath
     *
     * @return contextPath
     */
    static public String getContextPath() {
        HttpServletRequest request = SpringContextUtil.getRequest();
        return request==null?null:request.getContextPath();
    }
    public static void publishEvent(ApplicationEvent event) {
        if (event!=null) getApplicationContext().publishEvent(event);
    }
    private static void initProjectHomeDiretion(@NonNull Class<?> clazz) {
        if ("file".equals(clazz.getResource("").getProtocol())) {  // ide模式，使用上級目錄
            String path = FileUtil.getPath(clazz);
            if (path.endsWith("\\target\\classes") || path.endsWith("/target/classes")) {
                File hd = new File(path).getParentFile().getParentFile();
                try {
                    setProjectHomeDirection(hd.getCanonicalPath());
                } catch (Exception e) {}
            } else {
                setProjectHomeDirection(FileUtil.getPath(null));
            }
        } else {
            setProjectHomeDirection(FileUtil.getPath(null));
        }
    }
}
