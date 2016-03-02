import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by fusions on 01.03.2016.
 */
public class WeightedDistribution extends Thread{
    public static final int SERVER_REG_PORT = 8888, CLIENT_RECV_PORT = 1025;

    private HashMap<String, Integer> server = new HashMap<String, Integer>();
    private ServerSocket serverRegSocket;
    private ServerSocket clientRecvSocket;
    public boolean serverRunning = true;
    public boolean clientRunning = true;
    private double[][] sharing_sheet;
    private int counter = 0;
    public  WeightedDistribution() {
        try {
            serverRegSocket = new ServerSocket(SERVER_REG_PORT);
            clientRecvSocket = new ServerSocket(CLIENT_RECV_PORT);
            new ListingForServerThread().start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public WeightedDistribution(double ... auslastungen){
        this();
        double sum=0;
        for (int i = 0; i < auslastungen.length; i++) {
            sum += auslastungen[i];
        }
        if (sum != 1){
            throw new RuntimeException("Alle Werte zusammen müssen 1 (int) ergeben");
        }
        sharing_sheet = new double[auslastungen.length][3];
        for (int i = 0; i < sharing_sheet.length; i++) {
            sharing_sheet[i][0] = auslastungen[i];
            sharing_sheet[i][1] = 0;
            sharing_sheet[i][2] = auslastungen[i];
        }
    }


    private synchronized int useAlgo(){
        for (int i = 0; i <sharing_sheet.length; i++) {
            sharing_sheet[i][2] = sharing_sheet[i][0] - sharing_sheet[i][1]/counter;
        }
        int min_id = 0;
        for (int i = 0; i < sharing_sheet.length; i++) {
            if (sharing_sheet[i][2] > sharing_sheet[min_id][2])
                min_id = i;
        }
        sharing_sheet[min_id][1]++;
        return min_id;
    }

    public void run() {
        while (clientRunning) {
            try {
                Socket cs = clientRecvSocket.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
                String data = br.readLine();
                try {
                    String weiterIp="";
                    int weiterConnections=0;
                    int i =0;
                    for(String key : server.keySet())
                    {
                        if(i==0){
                            weiterConnections=server.get(key);
                        }
                        if(weiterConnections>=server.get(key)){
                            weiterIp= key;
                            weiterConnections=server.get(key);
                        }
                        ++i;
                    }
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
                    String port = br.readLine();
                    if(!server.containsKey(ip)){
                        server.put(ip+":"+port, 0);
                    }
                    for(String key : server.keySet())
                    {
                        System.out.print("IP: " + key + " - ");
                        System.out.print("Connections: " + server.get(key) + "\n");
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