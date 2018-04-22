public class InterestedMessage {
    private static final String TYPE = "2";

    public InterestedMessage() {

    }

    public byte[] createInterestedMessage() {
        return ("0000" + TYPE).getBytes();
    }
}