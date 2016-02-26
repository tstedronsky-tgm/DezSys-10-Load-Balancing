import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
            new ListingForServerThread().start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run(){
        while (clientRunning){
            try {
                Socket cs = clientRecvSocket.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
                String data =br.readLine();
                try {
                    Socket socket = new Socket(this.serverIps.get(0).split(":")[0], Integer.parseInt(serverIps.get(0).split(":")[1]));
                    PrintWriter pw = new PrintWriter(socket.getOutputStream());
                    pw.print(data);
                    pw.close();
                    socket.close();
                } catch (java.io.IOException e) {
                    System.err.print(e);
                } finally {

                }
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
                    String ip = sc.getRemoteSocketAddress().toString().replace("/","").split(":")[0];
                    BufferedReader br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
                    String port = br.readLine();
                    if(!serverIps.contains(ip)){
                        serverIps.add(ip+":"+port);
                    }
                    for(int i=0; i<serverIps.size();++i){
                        System.out.println(serverIps.get(i));
                    }
                    System.out.println();
                    sc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main (String ... args){
        Loadbalancer lb = new Loadbalancer();
        lb.start();
    }
}
