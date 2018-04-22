import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class TorrentClient {
    Socket requestSocket; //socket connect to the server
    ObjectOutputStream out; //stream write to the socket
    ObjectInputStream in; //stream read from the socket
    String message; //message send to the server

    private PeerInfo host;
    private PeerInfo peer;
    private Logger log;

    public TorrentClient(PeerInfo host, PeerInfo peer, Logger log) {
        this.host = host;
        this.peer = peer;
        this.log = log;
    }

    void run() {
        try {
            /* TODO CHANGE HOSTNAME TO NOT LOCALHOST */
            requestSocket = new Socket("localhost", peer.getPort());
            System.out.println("Connected to " + "localhost" + " in port " + peer.getPort());
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            // while (true) {
            sendHandshake(host, peer);
            message = (String) in.readObject();
            System.out.println(message);

            sendBitfield(host);
            message = (String) in.readObject();
            System.out.println(message);

            if (isInterested(message, host)) {
                sendInterested();
            } else {
                sendUninterested();
            }

            // close the log
            log.closeAllWriters();

            // }

        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unknown error occured. " + e);
        } finally {
            //Close connections
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    //send a message to the output stream
    void sendMessage(String msg) {
        try {
            //stream write the message
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void sendHandshake(PeerInfo host, PeerInfo peer) throws Exception {
        HandshakeMessage hm = new HandshakeMessage(host.getPeerId());
        sendMessage(hm.createHandshake());
        log.logTcpFromHost(peer.getPeerId());
    }

    public void sendBitfield(PeerInfo host) throws Exception {
        BitfieldMessage bm = new BitfieldMessage();
        sendMessage(bm.createBitfieldMessage(host.getBitfield()));
    }

    public boolean isInterested(String message, PeerInfo host) {
        String payload = message.substring(5);
        char[] peerBitfield = BitfieldMessage.convertPayloadToBitfield(payload);

        int x = 0;
        for (char c : peerBitfield) {
            if (c == '1' && host.getBitfield()[x++] == '0')
                return true;
        }

        return false;
    }

    public void sendInterested() {
        InterestedMessage im = new InterestedMessage();
        sendMessage(im.createInterestedMessage());
    }

    public void sendUninterested() {
        NotInterestedMessage nm = new NotInterestedMessage();
        sendMessage(nm.createNotInterestedMessage());
    }
}