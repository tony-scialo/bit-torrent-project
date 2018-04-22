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

}