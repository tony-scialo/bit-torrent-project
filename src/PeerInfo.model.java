import java.util.List;
import java.util.ArrayList;

class PeerInfo {
    private int peerId;
    private String hostName;
    private int port;
    private boolean file;
    private String rawString;
    private char[] bitfield;
    private List<Integer> bitRequested = new ArrayList<>();

    public PeerInfo() {

    }

    public String toString() {
        String bf = "";
        for (char c : bitfield) {
            bf += c;
        }

        return peerId + " " + hostName + " " + port + " " + file + "\n" + "bitfield: " + bf;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean hasFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public String getRawString() {
        return rawString;
    }

    public void setRawString(String rawString) {
        this.rawString = rawString;
    }

    public void setBitfield(char[] bitfield) {
        this.bitfield = bitfield;
    }

    public char[] getBitfield() {
        return bitfield;
    }

    public void setBitRequested(List<Integer> bitRequested) {
        this.bitRequested = bitRequested;
    }

    public List<Integer> getBitRequested() {
        return bitRequested;
    }
}