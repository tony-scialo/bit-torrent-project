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
    String MESSAGE; //capitalized message read from the server

    private String hostname;
    private int port;
    private PeerInfo peer;

    public TorrentClient(String hostname, int port, PeerInfo peer) {
        this.hostname = hostname;
        this.port = port;
        this.peer = peer;
    }

    void run() {
        try {
            requestSocket = new Socket(hostname, port);
            System.out.println("Connected to " + hostname + " in port " + port);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
            // while (true) {
            // start with handshake
            HandshakeMessage hm = new HandshakeMessage(peer.getPeerId());
            sendMessage(hm.createHandshake());
            //Receive the upperCase sentence from the server
            MESSAGE = (String) in.readObject();
            //show the message to the user
            System.out.println("Receive message: " + MESSAGE);
            // }
        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
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
}