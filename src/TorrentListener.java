import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

class TorrentListener {
    private Logger log;
    private PeerInfo host;

    public TorrentListener(Logger log, PeerInfo host) {
        this.log = log;
        this.host = host;
    }

    public void listenForRequests() throws Exception {
        ServerSocket socket = new ServerSocket(host.getPort());
        int clientNum = 0;
        try {
            while (true) {
                new Handler(socket.accept(), clientNum, host, log).start();
                clientNum++;
            }
        } finally {
            socket.close();
        }

    }

    /**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    private static class Handler extends Thread {
        private String message; //message received from the client
        private Socket connection;
        private ObjectInputStream in; //stream read from the socket
        private ObjectOutputStream out; //stream write to the socket
        private int no; //The index number of the client

        private boolean recievedHandshake = false;
        private PeerInfo connectedPeer;
        private PeerInfo host;
        private Logger log;

        public Handler(Socket connection, int no, PeerInfo host, Logger log) {
            this.connection = connection;
            this.no = no;
            this.host = host;
            this.log = log;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                try {
                    while (true) {
                        message = (String) in.readObject();
                        System.out.println("Receive message: " + message + " from client " + no);

                        if (!recievedHandshake) {
                            connectedPeer = handshakeRecieved(message);
                            HandshakeMessage hm = new HandshakeMessage(host.getPeerId());
                            sendMessage(hm.createHandshake());
                            log.logTcpFromPeer(connectedPeer.getPeerId());
                            log.closeAllWriters();
                            recievedHandshake = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error in process " + e);
                }
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client " + no);
            } finally {
                //Close connections
                try {
                    in.close();
                    out.close();
                    connection.close();
                } catch (IOException ioException) {
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }

        //send a message to the output stream
        public void sendMessage(String msg) {
            try {
                out.writeObject(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to Client " + no);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public PeerInfo handshakeRecieved(String message) {
            HandshakeMessage hm = new HandshakeMessage();
            connectedPeer = new PeerInfo();
            connectedPeer.setPeerId(Integer.parseInt(hm.parseHandshake(message)));
            return connectedPeer;
        }
    }
}