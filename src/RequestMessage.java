public class RequestMessage {
    public static final String TYPE = "6";

    public RequestMessage() {

    }

    public byte[] createRequestMessage(String pieceIndex) {
        return ("0000" + TYPE + MessageUtil.convertStringToFourBytes(pieceIndex)).getBytes();
    }
}