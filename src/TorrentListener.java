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
                        if (!recievedHandshake) {
                            connectedPeer = handshakeRecieved(message);
                            HandshakeMessage hm = new HandshakeMessage(host.getPeerId());
                            sendMessage(hm.createHandshake());
                            log.logTcpFromPeer(connectedPeer.getPeerId());
                            recievedHandshake = true;
                        } else {
                            switch (getMessageType(message)) {
                            case 0:
                                chokeRecieved();
                                break;
                            case 1:
                                unchokeRecieved();
                                break;
                            case 2:
                                interestedRecieved(log, connectedPeer);
                                break;
                            case 3:
                                notInterestedRecieved();
                                break;
                            case 4:
                                haveRecieved();
                                break;
                            case 5:
                                bitfieldRecieved(message, host);
                                break;
                            case 6:
                                requestRecieved();
                                break;
                            case 7:
                                pieceRecieved();
                                break;
                            default:
                                System.out.println("WRONG");
                            }
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
                    log.closeAllWriters();
                    in.close();
                    out.close();
                    connection.close();
                } catch (Exception ioException) {
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

        public void chokeRecieved() {
            System.out.println("CHOKE");
        }

        public void unchokeRecieved() {
            System.out.println("UNCHOKE");
        }

        public void interestedRecieved(Logger log, PeerInfo peer) throws Exception {
            log.logInterested(peer.getPeerId());
            System.out.println("INTERESTED");
        }

        public void notInterestedRecieved() {
            System.out.println("NOT INTERESTED");
        }

        public void haveRecieved() {
            System.out.println("HAVE");
        }

        public void bitfieldRecieved(String message, PeerInfo host) {
            // respond w/ bitfield message
            BitfieldMessage bm = new BitfieldMessage();
            sendMessage(bm.createBitfieldMessage(host.getBitfield()));
        }

        public void requestRecieved() {
            System.out.println("REQUEST");
        }

        public void pieceRecieved() {
            System.out.println("PIECE");
        }

        public int getMessageType(String message) {
            return Integer.parseInt(message.substring(4, 5));
        }
    }
}