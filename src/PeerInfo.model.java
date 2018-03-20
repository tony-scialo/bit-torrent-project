class PeerInfo {
    private int peerId;
    private String hostName;
    private int port;
    private boolean file;
    private String rawString;

    public PeerInfo() {

    }

    public String toString(){
        return peerId + " " + hostName + " " + port + " " + file;
    }

    public int getPeerId(){
        return peerId;
    }

    public void setPeerId(int peerId){
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

    public String getRawString(){
        return rawString;
    }

    public void setRawString(String rawString){
        this.rawString = rawString;
    }
}