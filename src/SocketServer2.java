import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Socket Server for Weighted Distribution
 * @author Thomas Stedronsky
 * @author Erik Braendli
 * @version 02-03-2016
 */
public class SocketServer2 extends Thread{
    private String listenaddr;
    private int port;
    private ServerSocket ss;
    public boolean running = true;
    private ArrayList<String> data = new ArrayList<>();

    /**
     * Constructor
     * @param listenaddr
     * @param port
     */
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

    /**
     *
     * @return listenaddr
     */
    public String getListenaddr() {
        return listenaddr;
    }

    /**
     * set the listenaddr
     * @param listenaddr
     */
    public void setListenaddr(String listenaddr) {
        this.listenaddr = listenaddr;
    }

    /**
     * get server port
     * @return server port
     */
    public int getPort() {
        return port;
    }

    /**
     * set port
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * login to server
     * @param ipOfLB LB IP
     * @param gewichtung from the server
     */
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
        ss1.regAtLB("127.0.0.1", 5);
        /*
        SocketServer2 ss2 = new SocketServer2("", 1027);
        ss2.start();
        ss2.regAtLB("10.0.105.170", 3);
        SocketServer2 ss3 = new SocketServer2("", 1028);
        ss3.start();
        ss3.regAtLB("10.0.105.170", 2);
        */
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
