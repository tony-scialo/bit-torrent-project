import java.nio.file.Files;
import java.nio.file.Paths;

class FileParser {

    public static final String PEER_INFO = "PeerInfo.cfg";

    public FileParser() {

    }

    public PeerInfo parsePeerInfo() throws Exception {
        PeerInfo pi = new PeerInfo();

        String peerInfoRaw = convertFileToString(PEER_INFO);
        System.out.println(peerInfoRaw);

        return pi;
    }

    private String convertFileToString(String filename) throws Exception {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (Exception e) {
            System.err.println("Error reading file " + filename);
            throw new Exception();
        }
    }

}