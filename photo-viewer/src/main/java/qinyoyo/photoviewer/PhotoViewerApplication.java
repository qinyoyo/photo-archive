package qinyoyo.photoviewer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import qinyoyo.utils.Util;

import java.io.FileNotFoundException;

@SpringBootApplication
public class PhotoViewerApplication {
    public static void main(String[] args) throws FileNotFoundException {
        Util.commonApplicationRun(PhotoViewerApplication.class,"application.yml","pv",args);
    }
}
