package qinyoyo.photoviewer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import qinyoyo.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

@SpringBootApplication
public class PhotoViewerApplication {
    public static final String STDOUT = "stdout.log";
    public static void main(String[] args) throws FileNotFoundException {
        System.setOut(new PrintStream(new File(STDOUT)));
        Util.commonApplicationRun(PhotoViewerApplication.class,"application.yml","pv",args);
    }
}
