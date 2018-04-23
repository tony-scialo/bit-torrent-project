class Piece {
    private byte[] data;
    private boolean hasPiece;

    public Piece() {

    }

    public Piece(byte[] data, boolean hasPiece) {
        this.data = data;
        this.hasPiece = hasPiece;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean getHasPiece() {
        return hasPiece;
    }

    public void setHasPiece(boolean hasPiece) {
        this.hasPiece = hasPiece;
    }
}