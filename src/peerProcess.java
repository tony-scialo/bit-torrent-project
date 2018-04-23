import java.util.List;
import java.util.ArrayList;

public class peerProcess {
    public static void main(String[] args) {

        int peerId;
        List<PeerInfo> piList;
        Common commonFile;
        int peerIndex;
        PeerInfo host;
        Logger log;

        String filename;
        byte[] file;
        Piece[] pieces;

        if (args.length != 2) {
            System.out.println("Error: peer id and file to download not provided, closing program");
            return;
        }

        peerId = Integer.parseInt(args[0]);
        filename = args[1];

        // parse the 2 files Common.cfg and PeerInfo.cfg
        FileParser parser = new FileParser();

        try {
            piList = parser.parsePeerInfo();
            commonFile = parser.parseCommonFile();
        } catch (Exception e) {
            System.out.println("Error: exiting program");
            return;
        }

        //figure out which peer you are
        peerIndex = PeerInfoUtil.findPeerInfoIndex(peerId, piList);
        if (peerIndex == -1) {
            System.out.println("Error: couldn't find provided peer id in PeerInfo.cfg");
            return;
        }
        host = piList.get(peerIndex);

        // set up bitfield for host
        for (PeerInfo pi : piList) {
            pi.setBitfield(PeerInfoUtil.createEmptyBitfield(commonFile));
        }
        host.setBitfield(PeerInfoUtil.setUpBitfield(commonFile, host.hasFile()));

        //if host has file, create byte[] from it
        if (host.hasFile()) {
            try {
                file = FileUtil.fileToByteStream(filename);
                pieces = FileUtil.breakIntoPieces(file, commonFile.getPieceSize());
            } catch (Exception e) {
                System.out.println("Error w/ file, exiting program");
                return;
            }
        } else {
            file = null;
            pieces = FileUtil.breakIntoPiecesNoFile(file, commonFile.getPieceSize());
        }

        // init the logger
        try {
            log = new Logger(peerId);
        } catch (Exception e) {
            System.out.println("Error creating the logger");
            return;
        }

        // if peer index == 0, just start listening on specified port
        // else, you are a client and need to send requests to the other peer's that came before you
        if (peerIndex == 0) {
            TorrentListener tl = new TorrentListener(log, host, piList, file);
            System.out.println("Listening on port: " + piList.get(peerIndex).getPort());
            try {
                tl.listenForRequests();
            } catch (Exception e) {
                return;
            }
        } else {
            TorrentClient tl = new TorrentClient(host, piList.get(0), log, piList);
            tl.run();
        }
    }
}