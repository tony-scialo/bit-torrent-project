import java.net.ServerSocket;

class TorrentListener {
    public TorrentListener() {

    }

    public void listenForRequests(int port) throws Exception{
        ServerSocket socket = new ServerSocket(port);
        int clientNum = 0;
        try{
            while(true){
                new Handler(socket.accept(),clientNum).start();
                clientNum++;
            }
        }finally{
            socket.close();
        }

    }
}