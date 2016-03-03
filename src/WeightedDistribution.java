import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
/**
 * Created by Thomas on 01.03.2016.
 */
public class WeightedDistribution extends Thread{
    public static final int SERVER_REG_PORT = 8888, CLIENT_RECV_PORT = 1025;

    private HashMap<String, Integer> server = new HashMap<String, Integer>();
    private HashMap<String, Integer> serverGew = new HashMap<String, Integer>();
    private HashMap<String, Integer> serverGew2 = new HashMap<String, Integer>();
    private ServerSocket serverRegSocket;
    private ServerSocket clientRecvSocket;
    public boolean serverRunning = true;
    public boolean clientRunning = true;
    private ArrayList<String> ips = new ArrayList<String>();
    private ArrayList<Integer> gew = new ArrayList<Integer>();
    private int aktiveConn =0;


    public  WeightedDistribution() {
        try {
            serverRegSocket = new ServerSocket(SERVER_REG_PORT);
            clientRecvSocket = new ServerSocket(CLIENT_RECV_PORT);
            new ListingForServerThread().start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {
        while (clientRunning) {
            try {
                Socket cs = clientRecvSocket.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
                String data = br.readLine();
                try {
                    String weiterIp="";
                    int hoechste=0;
                    int i =0;

                    for(String key : serverGew.keySet())
                    {
                        ips.add(key);
                        gew.add(serverGew.get(key));
                    }
                    for(int y=0; y<ips.size();++y){
                        System.out.print("IP: " + ips.get(y) + " - ");
                        System.out.print("Gew: " + gew.get(y) + "\n");
                    }

                    serverGew2.put(weiterIp, serverGew2.get(weiterIp) - 1); //Update der Connection

                    Socket socket = new Socket(weiterIp.split(":")[0], Integer.parseInt(weiterIp.split(":")[1]));
                    PrintWriter pw = new PrintWriter(socket.getOutputStream());
                    server.put(weiterIp, server.get(weiterIp) + 1); //Update der Connection
                    pw.print(data);
                    pw.close();
                    socket.close();
                    for(String key : server.keySet())
                    {
                        System.out.print("IP: " + key + " - ");
                        System.out.print("Connections: " + server.get(key) + "\n");
                    }
                    System.out.println();
                } catch (java.io.IOException e) {
                    System.err.print(e);
                } finally {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    class ListingForServerThread extends Thread {

        public void run() {
            while (serverRunning) {
                try {
                    Socket sc = serverRegSocket.accept();
                    String ip = sc.getRemoteSocketAddress().toString().replace("/", "").split(":")[0];
                    BufferedReader br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
                    String data =br.readLine();
                    String port = data.split("/")[0];
                    int gewichtung = Integer.parseInt(data.split("/")[1]);
                    if(!server.containsKey(ip)){
                        server.put(ip+":"+port, 0);
                        serverGew.put(ip+":"+port, gewichtung);
                        serverGew2.put(ip+":"+port, gewichtung);
                    }
                    for(String key : serverGew.keySet())
                    {
                        System.out.print("IP: " + key + " - ");
                        System.out.print("Gewichtung: " + serverGew.get(key) + "\n");
                    }
                    System.out.println();
                    sc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String... args) {
        WeightedDistribution lb = new  WeightedDistribution();
        lb.start();
    }
}