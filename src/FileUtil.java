import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.Charset;

import java.io.FileOutputStream;

public class FileUtil {

    public static byte[] fileToByteStream(String filename) throws Exception {

        try {
            /*TODO prob need to make sure this path is correct on uf server */
            Path p = Paths.get(filename);
            byte[] data = Files.readAllBytes(p);
            return data;
        } catch (Exception e) {
            System.out.println("Error converting file to byte[]. " + e);
            throw e;
        }
    }

    public static void printBytesAsString(byte[] data) {
        System.out.println(new String(data, Charset.forName("ISO-8859-1")));
    }

    public static void createFileFromBytes(byte[] data, String filename) throws Exception {
        try {
            /*TODO prob need to make sure this path is correct on uf server */
            FileOutputStream stream = new FileOutputStream(filename);
            try {
                stream.write(data);
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            System.out.println("Error writing byte[] to file.");
            throw e;
        }

    }

}