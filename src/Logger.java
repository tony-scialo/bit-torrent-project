import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public FileWriter fw;
    public PrintWriter pw;
    public static final String FILENAME = "log_peer_";
    private int hostId;

    public Logger(int hostId) throws Exception {
        try {
            this.hostId = hostId;
            fw = new FileWriter(FILENAME + hostId + ".log", true);
            pw = new PrintWriter(fw);
            pw.println("-------------- NEW LOG " + formatDate(new Date()) + " --------------");
        } catch (Exception e) {
            System.out.println("Error in logger constructor. " + e);
            throw e;
        }
    }

    public void logTcpFromHost(int peerId) throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " makes a connection to Peer " + peerId + ".");
    }

    public void logTcpFromPeer(int peerId) throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " is connected from " + peerId + ".");
    }

    /*TODO probs need to change how neighbors is passed in*/
    public void logChangePreferredNeighbors(String neighborList) throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " has the preferred neighbors " + neighborList + ".");
    }

    public void logChangeOptimisticUnchoked(int peerId) throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " has the optimistically unchoked neighbor " + peerId + ".");
    }

    public void logUnchoked(int peerId) throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " is unchoked by " + peerId + ".");
    }

    public void logChoked(int peerId) throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " is choked by " + peerId + ".");
    }

    public void logHave(int peerId, int pieceIndex) throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " recieved the 'have' message from " + peerId
                + " for the piece " + pieceIndex + ".");
    }

    public void logInterested(int peerId) throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " recieved the 'interested' message from " + peerId + ".");
    }

    public void logNotInterested(int peerId) throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " recieved the 'not interested' message from " + peerId + ".");
    }

    public void logFinishPieceDownload(int peerId, int pieceIndex, int numPieces) throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " has downloaded the piece " + pieceIndex + " from " + peerId
                + ". Now the number of pieces it has is " + numPieces + ".");
    }

    public void logDownloadComplete() throws Exception {
        log(formatDate(new Date()) + " Peer " + hostId + " has downloaded the complete file.");
    }

    public void closeAllWriters() throws Exception {
        try {
            pw.close();
            fw.close();
        } catch (Exception e) {
            System.out.println("Error closing writers. " + e);
            throw e;
        }
    }

    private void log(String whatToLog) throws Exception {
        try {
            pw.println(whatToLog);
        } catch (Exception e) {
            System.out.println("Error logging to file. " + e);
            closeAllWriters();
            throw e;
        }
    }

    private String formatDate(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH.mm.ss");
        return sdf.format(d) + ": ";
    }

}