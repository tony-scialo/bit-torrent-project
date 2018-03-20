import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

class FileParser {

    public static final String PEER_INFO = "PeerInfo.cfg";
    public static final String COMMON_FILE = "Common.cfg";

    public FileParser() {

    }

    public List<PeerInfo> parsePeerInfo() throws Exception {
        List<PeerInfo> piList;
        String peerInfoRaw = convertFileToString(PEER_INFO);
        piList = convertRawToPeerInfo(peerInfoRaw);
        return piList;
    }

    public Common parseCommonFile() throws Exception {
        Common c;
        String commonRaw = convertFileToString(COMMON_FILE);
        c = convertRawToCommonFile(commonRaw);
        return c;
    }

    private List<PeerInfo> convertRawToPeerInfo(String peerInfoRaw) {
        List<PeerInfo> piList = new ArrayList<>();
        String peerInfoOneLine = combineToOneLine(peerInfoRaw);
        String[] splitArray = peerInfoOneLine.split(" ");
        PeerInfo pi;

        for (int x = 0; x < splitArray.length; x += 4) {
            pi = new PeerInfo();
            pi.setPeerId(Integer.parseInt(splitArray[x]));
            pi.setHostName(splitArray[x + 1]);
            pi.setPort(Integer.parseInt(splitArray[x + 2]));
            pi.setFile((Integer.parseInt(splitArray[x + 3]) == 1));
            piList.add(pi);
        }
        return piList;
    }

    private Common convertRawToCommonFile(String commonFileRaw) {
        String commonOneLine = combineToOneLine(commonFileRaw);
        String[] splitArray = commonOneLine.split(" ");
        
        Common c = new Common();
        c.setNumNeighbors(Integer.parseInt(splitArray[1]));
        c.setUnchoke(Integer.parseInt(splitArray[3]));
        c.setOptimisticUnchoke(Integer.parseInt(splitArray[5]));
        c.setFileName(splitArray[7]);
        c.setFileSize(Long.parseLong(splitArray[9])); 
        c.setPieceSize(Long.parseLong(splitArray[11]));       

        return c;
    }

    private String combineToOneLine(String multilineString) {
        Scanner scan = new Scanner(multilineString);
        String oneLine = "";
        // not sure if one line or multiple so combine into one line
        while (scan.hasNextLine()) {
            oneLine += scan.nextLine() + " ";
        }
        return oneLine;
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