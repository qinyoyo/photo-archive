package tang.qinyoyo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SystemOut {
    static FileWriter writer;
    public static void print(Object s) {
        System.out.print(s);
        if (writer==null) {
            try {
                writer=new FileWriter(new File("out.log"),true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (writer!=null) {
            try {
                writer.write(s==null ? "<null>" : s.toString());
            } catch(Exception e) {}
        }
    }
    public static void println(Object s) {
        System.out.println(s);
        if (writer==null) {
            try {
                writer=new FileWriter(new File("out.log"),true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (writer!=null) {
            try {
                writer.write(s==null ? "<null>\r\n" : (s.toString()+"\r\n"));
            } catch(Exception e) {}
        }
    }
    public static void close() {
        if (writer!=null) {
            try {
                writer.close();
            } catch(Exception e) {}
        }
    }
}
