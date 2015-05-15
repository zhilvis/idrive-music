import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zhilvis on 15-05-15.
 */
public class FileNegator {

    public static void negate(File in, File out) throws IOException {
        negate(in.getAbsolutePath(), out.getAbsolutePath());
    }

    public static void negate(String in, String out) throws IOException {
        FileInputStream inf = new FileInputStream(in);
        FileOutputStream outf = new FileOutputStream(out);

        byte[] buff = new byte[1024];

        while (true) {
            int s = inf.read(buff);
            if (s == -1) {
                break;
            } else {
                for (int i = 0; i < s; i++){
                    buff[i] = (byte)~buff[i];
                }
                outf.write(buff);
            }
        }

        inf.close();
        outf.close();
    }
}
