public class UnchokeMessage {
    public static final String TYPE = "1";

    public UnchokeMessage() {

    }

    public byte[] createUnchokeMessage() {
        return ("0000" + TYPE).getBytes();
    }
}