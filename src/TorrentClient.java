import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class TorrentClient {

    private static PeerInfo host;
    private static Logger log;
    private static List<PeerInfo> piList;
    private static Piece[] pieces;
    private static Common commonFile;
    private static List<String> strLog = new ArrayList<>();

    private static int numUnchoked = 0;
    private static boolean isOptimumUnchoked = false;

    public TorrentClient(PeerInfo host, Logger log, List<PeerInfo> piList, Piece[] pieces, Common commonFile) {
        TorrentClient.host = host;
        TorrentClient.log = log;
        TorrentClient.piList = piList;
        TorrentClient.pieces = pieces;
        TorrentClient.commonFile = commonFile;
    }

    public void sendRequests() throws Exception {
        try {
            // send a request for each peer lower than you in the list
            int peerIndex = PeerInfoUtil.findPeerInfoIndex(host.getPeerId(), piList);
            for (int x = peerIndex - 1; x >= 0; x--) {
                new ClientHandler(piList.get(x)).start();
            }

        } finally {
            /*TODO SHOULD PROB CLOSE CONNECTIONS AT SOME POINT */
        }

    }

    private static class ClientHandler extends Thread {
        Socket requestSocket; //socket connect to the server
        ObjectOutputStream out; //stream write to the socket
        ObjectInputStream in; //stream read from the socket

        private PeerInfo peer;

        private byte[] byteMessage;
        private boolean isChoked = true;

        public ClientHandler(PeerInfo peer) {
            this.peer = peer;
        }

        public void run() {
            try {
                requestSocket = new Socket(peer.getHostName(), peer.getPort());
                System.out.println("Connected to " + "localhost" + " in port " + peer.getPort());
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(requestSocket.getInputStream());

                sendHandshake();
                byteMessage = (byte[]) in.readObject();
                System.out.println(FileUtil.convertByteToString(byteMessage));
                handshakeRecieved(byteMessage);

                sendBitfield();

                while (true) {

                    byteMessage = (byte[]) in.readObject();

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
                        haveRecieved();
                        break;
                    case 5:
                        bitfieldRecieved(byteMessage);
                        break;
                    case 6:
                        requestRecieved();
                        break;
                    case 7:
                        pieceRecieved(byteMessage);
                        break;
                    default:
                        System.out.println("WRONG");
                    }
                }

                // close the log
                // log.closeAllWriters();

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

        void sendByteMessage(byte[] data) {
            try {
                //stream write the message
                out.writeObject(data);
                out.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public boolean isInterested(byte[] byteMessage) {
            String payload = MessageUtil.getPayload(byteMessage);
            char[] peerBitfield = BitfieldMessage.convertPayloadToBitfield(payload);

            int x = 0;
            for (char c : peerBitfield) {
                if (c == '1' && TorrentClient.host.getBitfield()[x++] == '0')
                    return true;
            }

            return false;
        }

        public void handshakeRecieved(byte[] message) {
            /*TODO do i need to do anything here???? */
            return;
        }

        public void chokeRecieved() {
            System.out.println("CHOKE");
        }

        public void unchokeRecieved() throws Exception {
            System.out.println("UNCHOKE RECIEVED");
            isChoked = false;
            TorrentClient.log.logUnchoked(peer.getPeerId());
            // if doesn't have the complete file, send request message
            if (!host.hasFile()) {
                sendRequest();
            }
        }

        public void interestedRecieved() throws Exception {
            System.out.println("INTERESTED RECIEVED");
            TorrentClient.log.logInterested(peer.getPeerId());
            peer.setInterested(true);
            sendUnchokeMessage();

        }

        public void notInterestedRecieved() throws Exception {
            System.out.println("NOT INTERESTED");
            TorrentClient.log.logNotInterested(peer.getPeerId());
            peer.setInterested(true);
        }

        public void bitfieldRecieved(byte[] byteMessage) throws Exception {

            System.out.println("BITFIELD RECIEVED");

            // update bitfield of peer
            int peerIndex = PeerInfoUtil.findPeerInfoIndex(peer.getPeerId(), piList);
            TorrentClient.piList.get(peerIndex)
                    .setBitfield(PeerInfoUtil.createBitfieldFromPayload(MessageUtil.getPayload(byteMessage)));

            if (isInterested(byteMessage)) {
                sendInterested();
            } else {
                int doHave = doIHaveWhatYouWant();
                System.out.println(doHave);
                if (doHave != -1) {
                    sendHave(doHave);
                } else {
                    sendUninterested();
                }
            }
        }

        public void haveRecieved() {
            System.out.println("HAVE");
        }

        public void requestRecieved() throws Exception {
            System.out.print("REQUEST RECIEVED: ");
            FileUtil.printBytesAsString(byteMessage);

            // if it has the piece, send it
            String pieceIndex = MessageUtil.getPayload(byteMessage);
            if (PeerInfoUtil.peerHasPiece(TorrentClient.host.getBitfield(), Integer.parseInt(pieceIndex))) {
                sendPiece(pieceIndex, TorrentClient.pieces[Integer.parseInt(pieceIndex)].getData());
            }
        }

        public void pieceRecieved(byte[] byteMessage) throws Exception {
            System.out.println("PIECE RECIEVED");

            // figure out which piece it is
            int pieceIndex = MessageUtil.getPieceIndexFromPieceMessage(byteMessage);

            // get data and set it to piece
            TorrentClient.pieces[pieceIndex].setData(MessageUtil.getBytesFromPieceMessage(byteMessage));

            // set has piece to true
            TorrentClient.pieces[pieceIndex].setHasPiece(true);

            // update its own bitfield
            TorrentClient.host.getBitfield()[pieceIndex] = '1';

            boolean hasAllPieces = PeerInfoUtil.hasAllPieces(TorrentClient.host.getBitfield());

            // inc num pieces for loggert
            TorrentClient.host.incNumPieces();

            TorrentClient.log.logFinishPieceDownload(peer.getPeerId(), pieceIndex,
                    TorrentClient.host.getNumPiecesCollected());

            if (!hasAllPieces) {
                sendRequest();
            } else {
                /* TODO PROB NEED TO DO OTHER STUFF HERE AS WELL!!!!! */
                writeToLog();
                createFile();
            }

        }

        public void sendHandshake() throws Exception {
            HandshakeMessage hm = new HandshakeMessage(TorrentClient.host.getPeerId());
            sendByteMessage(hm.createHandshake());
            TorrentClient.log.logTcpFromHost(peer.getPeerId());
        }

        public void sendBitfield() throws Exception {
            BitfieldMessage bm = new BitfieldMessage();
            sendByteMessage(bm.createBitfieldMessage(TorrentClient.host.getBitfield()));
        }

        public void sendUnchokeMessage() {
            System.out.println("SEND UNCHOKE");
            UnchokeMessage um = new UnchokeMessage();
            sendByteMessage(um.createUnchokeMessage());
        }

        public void sendInterested() {
            InterestedMessage im = new InterestedMessage();
            sendByteMessage(im.createInterestedMessage());
        }

        public void sendUninterested() {
            NotInterestedMessage nm = new NotInterestedMessage();
            sendByteMessage(nm.createNotInterestedMessage());
        }

        public void sendHave(int pieceIndex) throws Exception {
            HaveMessage hm = new HaveMessage();
            sendByteMessage(hm.createHaveMessage(Integer.toString(pieceIndex)));
        }

        public void sendRequest() {
            if (!isChoked) {
                String neededIndex = PeerInfoUtil.determineNextNeededPiece(TorrentClient.host);
                RequestMessage rm = new RequestMessage();
                sendByteMessage(rm.createRequestMessage(neededIndex));
            }
        }

        public void sendPiece(String pieceIndex, byte[] data) throws Exception {
            System.out.println("PIECE SENT");
            PieceMessage pm = new PieceMessage();
            sendByteMessage(pm.createPieceMessage(pieceIndex, data));
        }

        public void createFile() throws Exception {
            FileUtil.buildFileFromPieces(TorrentClient.commonFile.getFileSize(), TorrentClient.pieces, "z3.txt");
        }

        public void writeToLog() throws Exception {
            TorrentClient.log.logDownloadComplete();
            TorrentClient.log.writeAllToLog();
        }

        public int doIHaveWhatYouWant() throws Exception {
            for (int x = 0; x < TorrentClient.host.getBitfield().length; x++) {
                if (peer.getBitfield()[x] != TorrentClient.host.getBitfield()[x]) {
                    return x;
                }
            }
            return -1;
        }

    }

}