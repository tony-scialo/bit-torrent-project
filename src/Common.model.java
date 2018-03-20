class Common {
    private int numNeighbors;
    private int unchoke;
    private int optimisticUnchoke;
    private String fileName;
    private long fileSize;
    private long pieceSize;

    public Common() {

    }

    public String toString() {
        return numNeighbors + " " + unchoke + " " + optimisticUnchoke + 
            " " + fileName + " " + fileSize + " " + pieceSize;
    }

    public int getNumNeighbors(){
        return numNeighbors;
    }

    public void setNumNeighbors(int numNeighbors){
        this.numNeighbors = numNeighbors;
    }

    public int getUnchoke(){
        return unchoke;
    }

    public void setUnchoke(int unchoke){
        this.unchoke = unchoke;
    }

    public int getOptimisticUnchoke(){
        return optimisticUnchoke;
    }

    public void setOptimisticUnchoke(int optimisticUnchoke){
        this.optimisticUnchoke = optimisticUnchoke;
    }

    public String getFilename(){
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public long getFileSize(){
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getPieceSize(){
        return pieceSize;
    }

    public void setPieceSize(long pieceSize) {
        this.pieceSize = pieceSize;
    }
}