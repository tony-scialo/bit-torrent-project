class PeerInfo {
    private String peerId;
    private String hostName;
    private int port;
    private boolean file;

    public PeerInfo() {

    }

    public PeerInfo(String peerId, String hostName, int port, boolean file) {
        this.peerId = peerId;
        this.hostName = hostName;
        this.port = port;
        this.file = file;
    }

    public String getPeerId(){
        return peerId;
    }

    public void setPeerId(String peerId){
        this.peerId = peerId;
    }

    public String getHostName(){
        return hostName;
    }

    public void setHostName(String hostName){
        this.hostName = hostName;
    }

    public int getPort(){
        return port;
    }

    public void setPort(int port){
        this.port = port;
    }

    public boolean hasFile(){
        return file;
    }

    public void setFile(boolean file){
        this.file = file;
    }
}