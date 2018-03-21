import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class Handler extends Thread{
    private String message;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int no;

    public Handler() {

    }

    public Handler(Socket socket, int no){
        this.socket = socket;
        this.no = no;
    }
}