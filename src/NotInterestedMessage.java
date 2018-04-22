public class NotInterestedMessage {

    public static final String TYPE = "3";

    public NotInterestedMessage() {

    }

    public byte[] createNotInterestedMessage() {
        return ("0000" + TYPE).getBytes();
    }
}