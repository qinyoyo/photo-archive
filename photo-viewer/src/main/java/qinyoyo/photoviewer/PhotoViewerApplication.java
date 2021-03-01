package qinyoyo.photoviewer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import qinyoyo.utils.ArchiveManager;
import qinyoyo.utils.Util;

@SpringBootApplication
public class PhotoViewerApplication {
    public static void main(String[] args) {
        if (args!=null && args.length>0) {
            if (args[0].equals("-a") || args.equals("--archive")) {
                if (!ArchiveManager.archive())  return;
            } else {
                System.out.println("options:\n   -a, --archive: 执行归档维护");
                return;
            }
        }
        Util.commonApplicationRun(PhotoViewerApplication.class,"application.yml","pv",args);
    }
}
