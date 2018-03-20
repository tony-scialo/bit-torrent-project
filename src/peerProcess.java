import java.util.List;
import java.util.ArrayList;

public class peerProcess {
    public static void main(String[] args) {

        int peerId;
        List<PeerInfo> piList;
        Common commonFile;
        int peerIndex;

        if(args.length == 0){
            System.out.println("Error: peer id not provided, closing program");
            return;
        }

        peerId = Integer.parseInt(args[0]);

        // parse the 2 files Common.cfg and PeerInfo.cfg
        FileParser parser = new FileParser();

        try{
            piList = parser.parsePeerInfo();
            commonFile = parser.parseCommonFile();
        }catch(Exception e){
            System.out.println("Error: exiting program");
            return;
        }

        //figure out which peer you are
        peerIndex = PeerInfoUtil.findPeerInfoIndex(peerId, piList);
        if(peerIndex == -1){
            System.out.println("Error: couldn't find provided peer id in PeerInfo.cfg");
            return;
        }
    }
}