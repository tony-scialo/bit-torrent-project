import java.util.List;
import java.util.ArrayList;

public class peerProcess {
    public static void main(String[] args) {

        if(args.length == 0){
            System.out.println("Error: peer id not provided, closing program");
            return;
        }

        int peerId = Integer.parseInt(args[0]);

        // parse the 2 files Common.cfg and PeerInfo.cfg
        FileParser parser = new FileParser();
        try{
            List<PeerInfo> piList = parser.parsePeerInfo();
            Common c = parser.parseCommonFile();
        }catch(Exception e){
            System.out.println("Error: exiting program");
        }
    }
}