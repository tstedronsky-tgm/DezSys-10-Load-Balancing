import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by fusions on 10.03.16.
 */
public class WeightedDistribution2 extends Thread {

    public static final int SERVER_REG_PORT = 8888, CLIENT_RECV_PORT = 1025;
    private HashMap<String, Integer> server = new HashMap<String, Integer>();
    private HashMap<String, Integer> serverGew = new HashMap<String, Integer>();
    private ServerSocket serverRegSocket;
    private ServerSocket clientRecvSocket;
    public boolean serverRunning = true;
    public boolean clientRunning = true;
    public boolean sessionPersistance = false;
    public HashMap<String, String> log = new HashMap<String, String>();
    private double[][] sharing_sheet;
    private int counter=1;
    private String[] server_list;

    public long[] statistics;
    public boolean useStats= true;

    /**
     * Load Balancer Weighted Distribution
     *
     * @author Thomas Stedronsky
     * @author Erik Braendli
     * @version 02-03-2016
     */
    public WeightedDistribution2() {
        try {
            serverRegSocket = new ServerSocket(SERVER_REG_PORT);
            clientRecvSocket = new ServerSocket(CLIENT_RECV_PORT);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public WeightedDistribution2(double... auslastungen) {
        this();
        double sum = 0;
        for (int i = 0; i < auslastungen.length; i++) {
            sum += auslastungen[i];
        }
        if (sum != 1) {
            throw new RuntimeException("Alle Werte zusammen müssen 1 (int) ergeben");
        }
        sharing_sheet = new double[auslastungen.length][3];
        for (int i = 0; i < sharing_sheet.length; i++) {
            sharing_sheet[i][0] = auslastungen[i];
            sharing_sheet[i][1] = 0;
            sharing_sheet[i][2] = auslastungen[i];
        }
        server_list = new String[auslastungen.length];
        statistics = new long[auslastungen.length];
        new ListingForServerThread().start();
    }


    private synchronized int useAlgo() {
        for (int i = 0; i < sharing_sheet.length; i++) {
            sharing_sheet[i][2] = sharing_sheet[i][0] - sharing_sheet[i][1] / counter;
        }
        int min_id = 0;
        for (int i = 0; i < sharing_sheet.length; i++) {
            if (sharing_sheet[i][2] > sharing_sheet[min_id][2])
                min_id = i;
        }
        sharing_sheet[min_id][1]++;
        counter++;
        statistics[min_id]++;
        return min_id;
    }



    public void run() {
        while (clientRunning){
            try {
                Socket cs = clientRecvSocket.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
                String data = br.readLine();
                String usedServer;
                if (sessionPersistance){
                    if (log.containsKey(cs.getRemoteSocketAddress().toString().replace("/", ""))) {
                        usedServer = log.get(cs.getRemoteSocketAddress().toString().replace("/", ""));
                    }else {
                        usedServer = server_list[useAlgo()];
                        log.put(cs.getRemoteSocketAddress().toString().replace("/", ""),usedServer);
                    }

                }else{
                    usedServer = server_list[useAlgo()];
                }
                Socket wsock = new Socket(usedServer.split(":")[0],Integer.parseInt(usedServer.split(":")[1]));
                System.out.println(cs.getRemoteSocketAddress().toString() + " -> " + wsock.getRemoteSocketAddress().toString());
                PrintWriter pw = new PrintWriter(wsock.getOutputStream());
                pw.print(data);
                pw.close();
                wsock.close();
                cs.close();

                if (useStats && counter > 100){
                    for (int i = 0; i <statistics.length ; i++) {
                        System.out.println("Server "+server_list[i] + " got "+ statistics[i] + " Connections");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class ListingForServerThread extends Thread {

        public void run() {
            int s_count= 0;
            while (serverRunning && s_count != server_list.length) {
                try {
                    String s_id="";
                    Socket sc = serverRegSocket.accept();
                    s_id= sc.getRemoteSocketAddress().toString().replace("/","").split(":")[0];
                    BufferedReader br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
                    int port = Integer.parseInt(br.readLine());
                    br.close();
                    s_id += ":"+port;
                    System.out.println(s_id + " registered");
                    server_list[s_count] = s_id;
                    sc.close();
                    s_count++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                serverRegSocket.close();
                System.out.println("no new server!!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String... args) {

        WeightedDistribution2 lb = new WeightedDistribution2(new double[]{0.2,0.4,0.4});
        lb.start();
    }
}

