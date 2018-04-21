public class HandshakeMessage {
    private final String HEADER = "P2PFILESHARINGPROJ";
    private final String ZERO_BITS = "0000000000";
    private int peerId;

    public HandshakeMessage() {

    }

    public HandshakeMessage(int peerId) {
        this.peerId = peerId;
    }

    public String createHandshake() {
        return HEADER + ZERO_BITS + peerId;
    }

    public String parseHandshake(String handshake) {
        return handshake.substring(28);
    }
}