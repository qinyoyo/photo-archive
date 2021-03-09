package qinyoyo.photoviewer;

import java.io.File;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import qinyoyo.utils.SpringContextUtil;
import qinyoyo.utils.Util;

@Configuration
public class MyWebAppConfigurer implements WebMvcConfigurer  {
    @Autowired
    private Environment env;

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/META-INF/resources/", "classpath:/resources/",
            "classpath:/static/", "classpath:/public/" };


    private void addRootPath(ResourceHandlerRegistry registry, File webapp) {
        if (webapp.exists() && webapp.isDirectory()) {
            try {
                String webappPath = webapp.getCanonicalPath() + File.separator;
                registry.addResourceHandler("/**").addResourceLocations("file:" + webappPath);
            } catch (Exception e){ Util.printStackTrace(e);}
        }
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
        File webapp=new File(SpringContextUtil.getProjectHomeDirection(),"webapp");
        addRootPath(registry,webapp);
        String photoRoot = env.getProperty("photo.root-path");
        if (photoRoot!=null) addRootPath(registry,new File(photoRoot));
    }   
}
