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

    public void logTcp(int peerId) throws Exception {
        try {
            pw.println(formatDate(new Date()) + " Peer " + hostId + " makes a connection to Peer " + peerId);
        } catch (Exception e) {
            closeAllWriters();
            throw e;
        }
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

    private String formatDate(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH.mm.ss");
        return sdf.format(d) + ": ";
    }

}