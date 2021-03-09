package qinyoyo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.lang.NonNull;
import qinyoyo.photoinfo.ArchiveManager;
import qinyoyo.utils.FileUtil;
import qinyoyo.utils.SpringContextUtil;
import qinyoyo.utils.Util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;

@SpringBootApplication
public class PhotoArchiveApplication {
    public static boolean ideMode = false;
    public static void main(String[] args) {
        ideMode = "file".equals(PhotoArchiveApplication.class.getResource("").getProtocol());
        if (args!=null && args.length>0) {
            if (args[0].equals("-a") || args.equals("--archive")) {
                if (!ArchiveManager.archive())  return;
            } else {
                System.out.println("options:\n   -a, --archive: 执行归档维护");
                return;
            }
        }
        commonApplicationRun(PhotoArchiveApplication.class,"application.yml","pv",args);
    }
    public static int getProcessID(Logger log) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        log.info(runtimeMXBean.getName());
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
    }
    private static String additionalPropertyFile(@NonNull String path, @NonNull String fileName) {
        PropertiesPropertySourceLoader propLoader = new PropertiesPropertySourceLoader();
        YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();
        String ext = null;
        File file = null;
        int dp = fileName.lastIndexOf(".");
        if (dp > 0) {
            ext = fileName.substring(dp + 1).toLowerCase();
            file = new File(path, fileName);
            if (file != null && file.exists()) {
                if (Arrays.asList(yamlLoader.getFileExtensions()).contains(ext)) {
                    return file.getAbsolutePath();
                } else {
                    if (Arrays.asList(propLoader.getFileExtensions()).contains(ext)) {
                        return file.getAbsolutePath();
                    }
                }
            }
        } else {
            for (String e : propLoader.getFileExtensions()) {
                file = new File(path, fileName + "." + e);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
            if (file == null || !file.exists()) {
                for (String e : yamlLoader.getFileExtensions()) {
                    file = new File(path, fileName + "." + e);
                    if (file.exists()) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }
    private static String additionalPropertyFile(@NonNull String fileName) {
        String path = SpringContextUtil.getProjectHomeDirection();
        if (Util.isEmpty(path)) return null;
        String fullName = additionalPropertyFile(path, fileName);
        return fullName;
    }
    public static void commonApplicationRun(@NonNull Class<?> clazz, String applicationProperties, String customerProperties, String[] args) {
        Logger log = LoggerFactory.getLogger(Util.class);
        int pid = getProcessID(log);
        try {
            FileUtil.writeToFile(new File(SpringContextUtil.getProjectHomeDirection(),"pid.log"),String.valueOf(pid));
        } catch (Exception e){ Util.printStackTrace(e);}
        SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(clazz);
        appBuilder.properties("file.encoding=UTF-8");
        String myPropertyFile = additionalPropertyFile(customerProperties == null ? "iotequ" : customerProperties);
        if (myPropertyFile!=null) SpringContextUtil.setPropertyFile(myPropertyFile);
        if (applicationProperties != null) {
            String location = "spring.config.location=classpath:/" + applicationProperties +
                    (myPropertyFile == null ? "" : "," + myPropertyFile);
            appBuilder.properties(location);
        } else {
            if (myPropertyFile != null) {
                appBuilder.properties("spring.config.additional-location=" + myPropertyFile);
            }
        }
        appBuilder.run(args);
    }
}
