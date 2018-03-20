import java.util.List;

class PeerInfoUtil {
    
    /**
     * Returns what your peer index is in the list
     */
    public static int findPeerInfoIndex(int peerId, List<PeerInfo> piList){
        int index = 0;
        while(index < piList.size()){
            if(peerId == piList.get(index).getPeerId())
                return index;
            else
                index++;
        }

        return -1;
    }

}