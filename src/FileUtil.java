import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

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

}