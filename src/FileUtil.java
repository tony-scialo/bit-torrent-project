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
        System.out.println(convertByteToString(data));
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

    public static byte[] convertStringToBytes(String payload) {
        return payload.getBytes(Charset.forName("UTF-8"));
    }

    public static String convertByteToString(byte[] data) {
        return new String(data, Charset.forName("ISO-8859-1"));
    }

    public static Piece[] breakIntoPieces(byte[] data, long pieceSize) throws Exception {
        try {
            Piece[] pa = new Piece[calcNumOfPieces(data.length, pieceSize)];

            byte[] ba;
            int dataIndex = 0;

            for (int x = 0; x < pa.length; x++) {
                ba = new byte[(int) pieceSize];
                for (int baIndex = 0; baIndex < pieceSize; baIndex++) {
                    try {
                        ba[baIndex] = data[dataIndex++];
                    } catch (Exception e) {
                        // DO NOTHING CATCHING ARRAY OUTTA BOUNDS HERE AND I DONT HAVE TIME TO REFACTOR!!!!!!!!
                    }
                }
                pa[x] = new Piece(ba, true);
            }
            return pa;

        } catch (Exception e) {
            System.out.println("Error breaking file into pieces . " + e);
            throw e;
        }

    }

    public static Piece[] breakIntoPiecesNoFile(int filesize, long pieceSize) {
        Piece[] pa = new Piece[calcNumOfPieces(filesize, pieceSize)];

        System.out.println(pa.length);

        for (int x = 0; x < pa.length; x++) {
            pa[x] = new Piece();
        }
        return pa;
    }

    public static int calcNumOfPieces(int fileSize, long pieceSize) {
        return (int) (Math.ceil(fileSize / (double) pieceSize));
    }

    public static void buildFileFromPieces(long filesize, Piece[] pieces, String filename) throws Exception {
        try {
            byte[] b = new byte[(int) filesize];

            int bIndex = 0;
            byte[] pb;

            for (Piece p : pieces) {
                pb = p.getData();
                for (int x = 0; x < pb.length; x++) {
                    try {
                        b[bIndex++] = pb[x];
                    } catch (Exception e) {
                        // SAME PROBLEM AS EARLIER SWALLOW THE EXCEPTION :(
                    }
                }
            }
            createFileFromBytes(b, filename);
        } catch (Exception e) {
            System.out.println("Error building file from pieces. " + e);
            throw e;
        }

    }

}