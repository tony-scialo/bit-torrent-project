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
    private static Piece[] pieces;
    private static Common commonFile;

    private static int numUnchoked = 0;
    private static boolean isOptimumUnchoked = false;

    public TorrentListener(Logger log, PeerInfo host, List<PeerInfo> piList, byte[] file, Piece[] pieces,
            Common commonFile) {
        TorrentListener.log = log;
        TorrentListener.host = host;
        TorrentListener.piList = piList;
        TorrentListener.file = file;
        TorrentListener.pieces = pieces;
        TorrentListener.commonFile = commonFile;
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

        private boolean isChoked = true;

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
                                interestedRecieved();
                                break;
                            case 3:
                                notInterestedRecieved();
                                break;
                            case 4:
                                haveRecieved(byteMessage);
                                break;
                            case 5:
                                bitfieldRecieved(byteMessage);
                                break;
                            case 6:
                                requestRecieved(byteMessage);
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

        public void unchokeRecieved() throws Exception {
            System.out.println("UNCHOKED RECIEVED");
            isChoked = false;
            TorrentListener.log.logUnchoked(connectedPeer.getPeerId());
            // if doesn't have the complete file, send request message
            if (!host.hasFile()) {
                sendRequest();
            }
        }

        public void interestedRecieved() throws Exception {
            System.out.println("INTERESTED RECIEVED");
            TorrentListener.log.logInterested(connectedPeer.getPeerId());
            connectedPeer.setInterested(true);
            sendUnchokeMessage();
        }

        public void notInterestedRecieved() throws Exception {
            TorrentListener.log.logNotInterested(connectedPeer.getPeerId());
            System.out.println("NOT INTERESTED");
            connectedPeer.setInterested(true);
        }

        public void haveRecieved(byte[] byteMessage) throws Exception {
            System.out.println("HAVE");

            FileUtil.printBytesAsString(byteMessage);

            int pieceIndex = MessageUtil.getPieceIndexFromPieceMessage(byteMessage);
            TorrentListener.log.logHave(connectedPeer.getPeerId(), pieceIndex);

            if (!isChoked) {
                sendRequest();
            } else {
                sendInterested();
            }
        }

        public void bitfieldRecieved(byte[] byteMessage) {
            // update bitfield of peer
            int peerIndex = PeerInfoUtil.findPeerInfoIndex(connectedPeer.getPeerId(), TorrentListener.piList);
            TorrentListener.piList.get(peerIndex)
                    .setBitfield(PeerInfoUtil.createBitfieldFromPayload(MessageUtil.getPayload(byteMessage)));
            BitfieldMessage bm = new BitfieldMessage();
            sendByteMessage(bm.createBitfieldMessage(TorrentListener.host.getBitfield()));
        }

        public void requestRecieved(byte[] byteMessage) throws Exception {
            System.out.print("REQUEST RECIEVED: ");
            FileUtil.printBytesAsString(byteMessage);

            // if it has the piece, send it
            String pieceIndex = MessageUtil.getPayload(byteMessage);
            if (PeerInfoUtil.peerHasPiece(TorrentListener.host.getBitfield(), Integer.parseInt(pieceIndex))) {
                sendPiece(pieceIndex, TorrentListener.pieces[Integer.parseInt(pieceIndex)].getData());
            }
        }

        public void pieceRecieved() throws Exception {
            System.out.println("PIECE RECIEVED");

            // figure out which piece it is
            int pieceIndex = MessageUtil.getPieceIndexFromPieceMessage(byteMessage);

            // get data and set it to piece
            TorrentListener.pieces[pieceIndex].setData(MessageUtil.getBytesFromPieceMessage(byteMessage));

            // set has piece to true
            TorrentListener.pieces[pieceIndex].setHasPiece(true);

            // update its own bitfield
            TorrentListener.host.getBitfield()[pieceIndex] = '1';

            boolean hasAllPieces = PeerInfoUtil.hasAllPieces(TorrentListener.host.getBitfield());

            // inc num pieces for loggert
            TorrentListener.host.incNumPieces();

            TorrentListener.log.logFinishPieceDownload(connectedPeer.getPeerId(), pieceIndex,
                    TorrentListener.host.getNumPiecesCollected());

            if (!hasAllPieces) {
                sendRequest();
            } else {
                /* TODO PROB NEED TO DO OTHER STUFF HERE AS WELL!!!!! */
                writeToLog();
                createFile();
            }
        }

        public void sendUnchokeMessage() {
            UnchokeMessage um = new UnchokeMessage();
            sendByteMessage(um.createUnchokeMessage());
        }

        public void sendInterested() {
            System.out.println("INTERESTED");
            InterestedMessage im = new InterestedMessage();
            sendByteMessage(im.createInterestedMessage());
        }

        public void sendRequest() {
            System.out.println("REQUEST");
            if (!isChoked) {
                String neededIndex = PeerInfoUtil.determineNextNeededPiece(TorrentListener.host);
                RequestMessage rm = new RequestMessage();
                sendByteMessage(rm.createRequestMessage(neededIndex));
            }
        }

        public void sendPiece(String pieceIndex, byte[] data) throws Exception {
            System.out.println("PIECE SENT: " + pieceIndex);
            PieceMessage pm = new PieceMessage();
            sendByteMessage(pm.createPieceMessage(pieceIndex, data));
        }

        public void createFile() throws Exception {
            FileUtil.buildFileFromPieces(TorrentListener.commonFile.getFileSize(), TorrentListener.pieces, "z2.txt");
        }

        public void writeToLog() throws Exception {
            TorrentListener.log.logDownloadComplete();
            TorrentListener.log.writeAllToLog();
        }

    }
}