import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class TorrentClient {
    Socket requestSocket; //socket connect to the server
    ObjectOutputStream out; //stream write to the socket
    ObjectInputStream in; //stream read from the socket

    private PeerInfo host;
    private PeerInfo peer;
    private Logger log;

    private static List<PeerInfo> piList;

    private byte[] byteMessage;

    private boolean isChoked = true;

    public TorrentClient(PeerInfo host, PeerInfo peer, Logger log, List<PeerInfo> piList) {
        this.host = host;
        this.peer = peer;
        this.log = log;
        TorrentClient.piList = piList;
    }

    void run() {
        try {
            /* TODO CHANGE HOSTNAME TO NOT LOCALHOST */
            requestSocket = new Socket("localhost", peer.getPort());
            System.out.println("Connected to " + "localhost" + " in port " + peer.getPort());
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            sendHandshake(host, peer);
            byteMessage = (byte[]) in.readObject();
            System.out.println(FileUtil.convertByteToString(byteMessage));

            sendBitfield(host);

            while (true) {

                byteMessage = (byte[]) in.readObject();

                switch (MessageUtil.getMessageType(byteMessage)) {
                case 0:
                    chokeRecieved();
                    break;
                case 1:
                    unchokeRecieved(peer);
                    break;
                case 2:
                    interestedRecieved(log, peer);
                    break;
                case 3:
                    notInterestedRecieved(log, peer);
                    break;
                case 4:
                    haveRecieved();
                    break;
                case 5:
                    bitfieldRecieved(byteMessage, peer, host);
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

    public boolean isInterested(byte[] byteMessage, PeerInfo host) {
        String payload = MessageUtil.getPayload(byteMessage);
        char[] peerBitfield = BitfieldMessage.convertPayloadToBitfield(payload);

        int x = 0;
        for (char c : peerBitfield) {
            if (c == '1' && host.getBitfield()[x++] == '0')
                return true;
        }

        return false;
    }

    public void chokeRecieved() {
        System.out.println("CHOKE");
    }

    public void unchokeRecieved(PeerInfo peer) {
        System.out.println("UNCHOKE");
        isChoked = false;
        // if doesn't have the complete file, send request message
        if (!host.hasFile()) {
            sendRequest(peer);
        }
    }

    public void interestedRecieved(Logger log, PeerInfo peer) throws Exception {
        log.logInterested(peer.getPeerId());
    }

    public void notInterestedRecieved(Logger log, PeerInfo peer) throws Exception {
        log.logNotInterested(peer.getPeerId());
        System.out.println("NOT INTERESTED");
    }

    public void bitfieldRecieved(byte[] byteMessage, PeerInfo peer, PeerInfo host) {

        System.out.println("BITFIELD RECIEVED");

        // update bitfield of peer
        int peerIndex = PeerInfoUtil.findPeerInfoIndex(peer.getPeerId(), piList);
        TorrentClient.piList.get(peerIndex)
                .setBitfield(PeerInfoUtil.createBitfieldFromPayload(MessageUtil.getPayload(byteMessage)));

        if (isInterested(byteMessage, host)) {
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

    public void pieceRecieved() {
        System.out.println("PIECE");
    }

    public void sendHandshake(PeerInfo host, PeerInfo peer) throws Exception {
        HandshakeMessage hm = new HandshakeMessage(host.getPeerId());
        sendByteMessage(hm.createHandshake());
        log.logTcpFromHost(peer.getPeerId());
    }

    public void sendBitfield(PeerInfo host) throws Exception {
        BitfieldMessage bm = new BitfieldMessage();
        sendByteMessage(bm.createBitfieldMessage(host.getBitfield()));
    }

    public void sendInterested() {
        InterestedMessage im = new InterestedMessage();
        sendByteMessage(im.createInterestedMessage());
    }

    public void sendUninterested() {
        NotInterestedMessage nm = new NotInterestedMessage();
        sendByteMessage(nm.createNotInterestedMessage());
    }

    public void sendRequest(PeerInfo peer) {
        if (!isChoked) {
            String neededIndex = PeerInfoUtil.determineNextNeededPiece(peer);
            RequestMessage rm = new RequestMessage();
            sendByteMessage(rm.createRequestMessage(neededIndex));
        }
    }

}