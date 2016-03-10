import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Socket Server for Least Connection
 * @author Thomas Stedronsky
 * @author Erik Braendli
 * @version 02-03-2016
 */
public class SocketServer extends Thread{
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

    /**
     *
     * @return listenaddr
     */
    public String getListenaddr() {
        return listenaddr;
    }

    /**
     * set listenaddr
     * @param listenaddr server addr
     */
    public void setListenaddr(String listenaddr) {
        this.listenaddr = listenaddr;
    }

    /**
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * set server port
     * @param port server port
     */
    public void setPort(int port) {
        this.port = port;
    }


    /**
     * log ad server
     * @param ipOfLB IP of LB
     */
    public void regAtLB(String ipOfLB){
        try {
            Socket s = new Socket(ipOfLB,8888);
            s.getOutputStream();
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.print(this.port);
            pw.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String ... args){
        for(int i=0; i<3;++i) {
            SocketServer ss = new SocketServer("127.0.0.1", i + 1026);
            ss.start();
            ss.regAtLB("127.0.0.1");
        }
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
