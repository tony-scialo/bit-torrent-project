public class PieceMessage {
    public static final String TYPE = "7";

    public PieceMessage() {

    }

    public byte[] createPieceMessage(String pieceIndex, byte[] data) {
        return ("0000" + TYPE + MessageUtil.convertStringToFourBytes(pieceIndex) + data).getBytes();
    }
}