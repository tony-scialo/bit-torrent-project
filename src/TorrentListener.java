import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

class TorrentListener {
    private static Logger log;
    private static PeerInfo host;

    private static List<PeerInfo> piList;
    private static byte[] file;

    public TorrentListener(Logger log, PeerInfo host, List<PeerInfo> piList, byte[] file) {
        this.log = log;
        this.host = host;
        TorrentListener.piList = piList;
        TorrentListener.file = file;
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
        private Socket connection;
        private ObjectInputStream in; //stream read from the socket
        private ObjectOutputStream out; //stream write to the socket
        private int no; //The index number of the client

        private boolean recievedHandshake = false;
        private PeerInfo connectedPeer;
        private PeerInfo host;
        private Logger log;

        private byte[] byteMessage;

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
                        // byte[] test = (byte[]) in.readObject();
                        byteMessage = (byte[]) in.readObject();
                        if (!recievedHandshake) {
                            connectedPeer = handshakeRecieved(byteMessage);
                            HandshakeMessage hm = new HandshakeMessage(host.getPeerId());
                            sendByteMessage(hm.createHandshake());
                            log.logTcpFromPeer(connectedPeer.getPeerId());
                            recievedHandshake = true;
                        } else {
                            switch (MessageUtil.getMessageType(byteMessage)) {
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
                                notInterestedRecieved(log, connectedPeer);
                                break;
                            case 4:
                                haveRecieved();
                                break;
                            case 5:
                                bitfieldRecieved(byteMessage, host, connectedPeer);
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

        void sendByteMessage(byte[] data) {
            try {
                //stream write the message
                out.writeObject(data);
                out.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public PeerInfo handshakeRecieved(byte[] message) {
            HandshakeMessage hm = new HandshakeMessage();
            connectedPeer = new PeerInfo();
            connectedPeer.setPeerId(Integer.parseInt(hm.parseHandshake(FileUtil.convertByteToString(message))));
            System.out.println(connectedPeer.getPeerId());
            return connectedPeer;
        }

        public void chokeRecieved() {
            System.out.println("CHOKE");
        }

        public void unchokeRecieved() {
            System.out.println("UNCHOKE");
        }

        public void interestedRecieved(Logger log, PeerInfo peer) throws Exception {
            System.out.println("INTERESTED RECIEVED");
            log.logInterested(peer.getPeerId());

            /*TODO NEED TO KEEP A LIST OF INTERESTED NEIGHBORS AT SOME POINT */

        }

        public void notInterestedRecieved(Logger log, PeerInfo peer) throws Exception {
            log.logNotInterested(peer.getPeerId());
            System.out.println("NOT INTERESTED");
        }

        public void haveRecieved() {
            System.out.println("HAVE");
        }

        public void bitfieldRecieved(byte[] byteMessage, PeerInfo host, PeerInfo conncetedPeer) {
            // update bitfield of peer
            int peerIndex = PeerInfoUtil.findPeerInfoIndex(connectedPeer.getPeerId(), piList);
            TorrentListener.piList.get(peerIndex)
                    .setBitfield(PeerInfoUtil.createBitfieldFromPayload(MessageUtil.getPayload(byteMessage)));
            BitfieldMessage bm = new BitfieldMessage();
            sendByteMessage(bm.createBitfieldMessage(host.getBitfield()));
        }

        public void requestRecieved() {
            System.out.println("REQUEST");
        }

        public void pieceRecieved() {
            System.out.println("PIECE");
        }

        public void sendUnchokeMessage() {
            UnchokeMessage um = new UnchokeMessage();
            sendByteMessage(um.createUnchokeMessage());
        }

    }
}