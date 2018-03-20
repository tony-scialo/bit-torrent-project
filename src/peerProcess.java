public class peerProcess {
    public static void main(String[] args) {
        System.out.println("TESTING");

        // parse the 2 files Common.cfg and PeerInfo.cfg
        FileParser parser = new FileParser();
        try{
            parser.parsePeerInfo();
        }catch(Exception e){
            System.out.println("Error: exiting program");
        }
    }
}