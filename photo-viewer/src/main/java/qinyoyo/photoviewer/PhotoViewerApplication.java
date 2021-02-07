package qinyoyo.photoviewer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import qinyoyo.utils.Util;

@SpringBootApplication
public class PhotoViewerApplication {
    public static String[] args = null;
    public static void main(String[] args) {
        PhotoViewerApplication.args = args;
        Util.commonApplicationRun(PhotoViewerApplication.class,"application.yml","pv",args);
    }
}
