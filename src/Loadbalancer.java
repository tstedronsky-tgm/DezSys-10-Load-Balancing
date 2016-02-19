import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by fusions on 19.02.16.
 */
public class Loadbalancer extends Thread{
    public static final int SERVER_REG_PORT = 8888, CLIENT_RECV_PORT = 1025;
    private ArrayList<String> serverIps = new ArrayList<>();
    private ServerSocket serverRegSocket;
    private ServerSocket clientRecvSocket;
    public boolean serverRunning=true;
    public boolean clientRunning=true;


    public Loadbalancer(){
        try {
            serverRegSocket = new ServerSocket(SERVER_REG_PORT);
            clientRecvSocket = new ServerSocket(CLIENT_RECV_PORT);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run(){
        while (clientRunning){
            try {
                Socket cs = clientRecvSocket.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    class ListingForServerThread extends Thread{

        public void run(){
            while (serverRunning) {
                try {
                    Socket sc = serverRegSocket.accept();
                    String ip = sc.getRemoteSocketAddress().toString();
                    if(!serverIps.contains(ip)){
                        serverIps.add(ip);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
