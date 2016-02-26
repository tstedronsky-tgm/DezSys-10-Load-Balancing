import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by fusions on 12.02.16.
 */
public class SocketServer extends Thread{
    private String listenaddr;
    private int port;
    private ServerSocket ss;
    public boolean running = true;
    private ArrayList<String> data = new ArrayList<>();

    public SocketServer(String listenaddr,int port){
        this.listenaddr = listenaddr;
        this.port = port;
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Port is used?");
        }
    }
    public String getListenaddr() {
        return listenaddr;
    }

    public void setListenaddr(String listenaddr) {
        this.listenaddr = listenaddr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void startListing(){

    }

    public void regAtLB(String ipOfLB){
        try {
            Socket s = new Socket(ipOfLB,8888);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String ... args){
        SocketServer ss = new SocketServer("0.0.0.0",1025);
        ss.start();
        ss.regAtLB("10.0.104.151");
    }


    @Override
    public void run() {
        while (running){
            try {
                Socket client = ss.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                data.add(br.readLine());
                System.out.println(data.get(data.size()-1));
                br.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
