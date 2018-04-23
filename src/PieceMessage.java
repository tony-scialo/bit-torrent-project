import java.io.ByteArrayOutputStream;

public class PieceMessage {
    public static final String TYPE = "7";

    public PieceMessage() {

    }

    public byte[] createPieceMessage(String pieceIndex, byte[] data) throws Exception {

        byte[] a = ("0000").getBytes();
        byte[] b = (TYPE).getBytes();
        byte[] c = (pieceIndex).getBytes();

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bs.write(a);
        bs.write(b);
        bs.write(c);
        bs.write(data);

        return bs.toByteArray();

    }
}