package qinyoyo.photoviewer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import qinyoyo.utils.Util;

@SpringBootApplication
public class PhotoViewerApplication {
    public static void main(String[] args) {
        Util.commonApplicationRun(PhotoViewerApplication.class,"application.yml","pv",args);
    }
}
