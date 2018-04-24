import java.io.ByteArrayOutputStream;

public class HaveMessage {
    public static final String TYPE = "4";

    public HaveMessage() {

    }

    public byte[] createHaveMessage(String pieceIndex) throws Exception {
        byte[] a = ("0000").getBytes();
        byte[] b = (TYPE).getBytes();
        byte[] c = (MessageUtil.convertStringToFourBytes(pieceIndex)).getBytes();

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bs.write(a);
        bs.write(b);
        bs.write(c);

        return bs.toByteArray();
    }
}