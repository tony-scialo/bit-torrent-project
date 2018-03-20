import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

class FileParser {

    public static final String PEER_INFO = "PeerInfo.cfg";

    public FileParser() {

    }

    public List<PeerInfo> parsePeerInfo() throws Exception {
        List<PeerInfo> piList;
        String peerInfoRaw = convertFileToString(PEER_INFO);
        piList = convertRawToPeerInfo(peerInfoRaw);
        return piList;
    }

    private List<PeerInfo> convertRawToPeerInfo(String peerInfoRaw){
        List<PeerInfo> piList = new ArrayList<>();
        Scanner scan = new Scanner(peerInfoRaw);
        String peerInfoOneLine = "";
        // not sure if one line or multiple so combine into one line
        while(scan.hasNextLine()){
            peerInfoOneLine += scan.nextLine() + " ";
        }

        String[] splitArray = peerInfoOneLine.split(" ");
        PeerInfo pi;

        for(int x = 0; x < splitArray.length; x += 4){
            pi = new PeerInfo();
            pi.setPeerId(Integer.parseInt(splitArray[x]));
            pi.setHostName(splitArray[x + 1]);
            pi.setPort(Integer.parseInt(splitArray[x + 2]));
            pi.setFile((Integer.parseInt(splitArray[x + 3]) == 1));
            piList.add(pi);
        }

        for (PeerInfo item : piList) {
            System.out.println(item.toString());
        }

        return piList;
    }

    private String convertFileToString(String filename) throws Exception {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (Exception e) {
            System.err.println("Error reading file " + filename);
            throw new Exception();
        }
    }

}