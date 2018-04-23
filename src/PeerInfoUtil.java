import java.util.List;

class PeerInfoUtil {

    /**
     * Returns what your peer index is in the list
     */
    public static int findPeerInfoIndex(int peerId, List<PeerInfo> piList) {
        int index = 0;
        while (index < piList.size()) {
            if (peerId == piList.get(index).getPeerId())
                return index;
            else
                index++;
        }

        return -1;
    }

    public static char[] setUpBitfield(Common commonFile, boolean hasFile) {
        char[] bitfield = new char[commonFile.calcNumPieces()];
        char hasFileChar;
        if (hasFile) {
            hasFileChar = '1';
        } else {
            hasFileChar = '0';
        }
        for (int x = 0; x < bitfield.length; x++) {
            bitfield[x] = hasFileChar;
        }
        return bitfield;
    }

    public static char[] createEmptyBitfield(Common commonFile) {
        return new char[commonFile.calcNumPieces()];
    }

    public static char[] createBitfieldFromPayload(String payload) {
        return payload.toCharArray();
    }

    public static String determineNextNeededPiece(PeerInfo host) {

        // find next needed piece
        int neededIndex = 0;
        for (int x = 0; x < host.getBitfield().length; x++) {
            if (host.getBitfield()[x] == '0' && !isAlreadyRequested(host.getBitRequested(), x)) {
                neededIndex = x;
                break;
            }
        }
        return Integer.toString(neededIndex);
    }

    public static boolean isAlreadyRequested(List<Integer> bitRequested, int neededIndex) {
        for (Integer i : bitRequested) {
            if (i == neededIndex)
                return true;
        }

        return false;
    }

    public static boolean peerHasPiece(char[] bitfield, int pieceIndex) {

        System.out.println(pieceIndex);
        System.out.println(bitfield.length);

        if (bitfield[pieceIndex] == '1')
            return true;
        else
            return false;
    }

    public static boolean hasAllPieces(char[] bitfield) {
        for (char c : bitfield) {
            if (c == '0')
                return false;
        }

        return true;
    }

}