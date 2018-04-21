import java.util.List;
import java.util.ArrayList;

public class peerProcess {
    public static void main(String[] args) {

        int peerId;
        List<PeerInfo> piList;
        Common commonFile;
        int peerIndex;
        Logger log;

        if (args.length == 0) {
            System.out.println("Error: peer id not provided, closing program");
            return;
        }

        peerId = Integer.parseInt(args[0]);

        // parse the 2 files Common.cfg and PeerInfo.cfg
        FileParser parser = new FileParser();

        try {
            piList = parser.parsePeerInfo();
            commonFile = parser.parseCommonFile();
        } catch (Exception e) {
            System.out.println("Error: exiting program");
            return;
        }

        //print for demo
        System.out.println("PeerInfo.cfg:");
        for (PeerInfo pi : piList) {
            System.out.println(pi.toString());
        }

        System.out.println("Common.cfg");
        System.out.println(commonFile.toString());

        //figure out which peer you are
        peerIndex = PeerInfoUtil.findPeerInfoIndex(peerId, piList);
        if (peerIndex == -1) {
            System.out.println("Error: couldn't find provided peer id in PeerInfo.cfg");
            return;
        }

        // init the logger
        try {
            log = new Logger(peerIndex);
        } catch (Exception e) {
            System.out.println("Error creating the logger");
            return;
        }

        try {
            log.logTcp(1);
            log.closeAllWriters();
        } catch (Exception e) {
            System.out.println("Error logging file");
        }

        //if peer index == 0, just start listening on specified port
        // if (peerIndex == 0) {
        //     TorrentListener tl = new TorrentListener();
        //     System.out.println("Listening on port: " + piList.get(peerIndex).getPort());
        //     try {
        //         tl.listenForRequests(1);
        //     } catch (Exception e) {
        //         return;
        //     }
        // }
    }
}