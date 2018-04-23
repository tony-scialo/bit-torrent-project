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

    public TorrentClient(PeerInfo host, Logger log, List<PeerInfo> piList, Piece[] pieces) {
        TorrentClient.host = host;
        TorrentClient.log = log;
        TorrentClient.piList = piList;
        TorrentClient.pieces = pieces;
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
                        interestedRecieved(log);
                        break;
                    case 3:
                        notInterestedRecieved(log);
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

        public void chokeRecieved() {
            System.out.println("CHOKE");
        }

        public void unchokeRecieved() {
            System.out.println("UNCHOKE");
            isChoked = false;
            // if doesn't have the complete file, send request message
            if (!host.hasFile()) {
                sendRequest();
            }
        }

        public void interestedRecieved(Logger log) throws Exception {
            log.logInterested(peer.getPeerId());
        }

        public void notInterestedRecieved(Logger log) throws Exception {
            log.logNotInterested(peer.getPeerId());
            System.out.println("NOT INTERESTED");
        }

        public void bitfieldRecieved(byte[] byteMessage) {

            System.out.println("BITFIELD RECIEVED");

            // update bitfield of peer
            int peerIndex = PeerInfoUtil.findPeerInfoIndex(peer.getPeerId(), piList);
            TorrentClient.piList.get(peerIndex)
                    .setBitfield(PeerInfoUtil.createBitfieldFromPayload(MessageUtil.getPayload(byteMessage)));

            if (isInterested(byteMessage)) {
                sendInterested();
            } else {
                sendUninterested();
            }
        }

        public void haveRecieved() {
            System.out.println("HAVE");
        }

        public void requestRecieved() {
            System.out.println("REQUEST");
        }

        public void pieceRecieved(byte[] byteMessage) {
            System.out.println("PIECE RECIEVED");

            // figure out which piece it is
            int pieceIndex = MessageUtil.getPieceIndexFromPieceMessage(byteMessage);

            // get data and set it to piece
            TorrentClient.pieces[pieceIndex].setData(Arrays.copyOfRange(byteMessage, 8, byteMessage.length));

            // set has piece to true
            TorrentClient.pieces[pieceIndex].setHasPiece(true);

            // send another request
            // sendRequest(peer);

        }

        public void sendHandshake() throws Exception {
            HandshakeMessage hm = new HandshakeMessage(TorrentClient.host.getPeerId());
            sendByteMessage(hm.createHandshake());
            log.logTcpFromHost(peer.getPeerId());
        }

        public void sendBitfield() throws Exception {
            BitfieldMessage bm = new BitfieldMessage();
            sendByteMessage(bm.createBitfieldMessage(TorrentClient.host.getBitfield()));
        }

        public void sendInterested() {
            InterestedMessage im = new InterestedMessage();
            sendByteMessage(im.createInterestedMessage());
        }

        public void sendUninterested() {
            NotInterestedMessage nm = new NotInterestedMessage();
            sendByteMessage(nm.createNotInterestedMessage());
        }

        public void sendRequest() {
            if (!isChoked) {
                String neededIndex = PeerInfoUtil.determineNextNeededPiece(peer);
                RequestMessage rm = new RequestMessage();
                sendByteMessage(rm.createRequestMessage(neededIndex));
            }
        }
    }

}