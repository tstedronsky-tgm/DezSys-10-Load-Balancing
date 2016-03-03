import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by fusions on 12.02.16.
 */
public class SocketServer2 extends Thread{
    private String listenaddr;
    private int port;
    private ServerSocket ss;
    public boolean running = true;
    private ArrayList<String> data = new ArrayList<>();

    public SocketServer2(String listenaddr,int port){
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

    public void regAtLB(String ipOfLB, int gewichtung){
        try {
            Socket s = new Socket(ipOfLB,8888);
            s.getOutputStream();
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.print(this.port+"/"+gewichtung);
            pw.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String ... args){
        SocketServer2 ss1 = new SocketServer2("", 1026);
        ss1.start();
        ss1.regAtLB("192.168.0.101", 5);
        SocketServer2 ss2 = new SocketServer2("", 1027);
        ss2.start();
        ss2.regAtLB("192.168.0.101", 3);
        SocketServer2 ss3 = new SocketServer2("", 1028);
        ss3.start();
        ss3.regAtLB("192.168.0.101", 2);
    }


    @Override
    public void run() {
        while (running){
            try {
                Socket client = ss.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                data.add(br.readLine());
                System.out.println("Port "+ port +"\tdata: "+ data.get(data.size()-1));
                br.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
