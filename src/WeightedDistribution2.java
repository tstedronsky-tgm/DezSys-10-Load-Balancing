import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Thomas on 01.03.2016.
 */
public class WeightedDistribution2 extends Thread{
    public static final int SERVER_REG_PORT = 8888, CLIENT_RECV_PORT = 1025;
    private HashMap<String, Integer> server = new HashMap<String, Integer>();
    private HashMap<String, Integer> serverGew = new HashMap<String, Integer>();
    private ServerSocket serverRegSocket;
    private ServerSocket clientRecvSocket;
    public boolean serverRunning = true;
    public boolean clientRunning = true;
    public boolean sessionPersistance = false;
    public HashMap<String, String> log= new HashMap<String, String>();

    public WeightedDistribution2() {
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
                    int hoechstens=0;
                    int i =0;
                    for(String key : serverGew.keySet())
                    {
                        if(i==0){
                            weiterIp=key;
                        }
                        if(hoechstens>=(server.get(key)/serverGew.get(key))){
                            weiterIp= key;
                            hoechstens=server.get(key)/serverGew.get(key);
                        }
                        ++i;
                    }
                    if(sessionPersistance) {
                        String clientIP = cs.getRemoteSocketAddress().toString().replace("/", "").split(":")[0];
                        if (log.containsKey(clientIP)) {
                            Socket socket = new Socket(log.get(clientIP).split(":")[0], Integer.parseInt(log.get(clientIP).split(":")[1]));
                            PrintWriter pw = new PrintWriter(socket.getOutputStream());
                            server.put(log.get(clientIP), server.get(log.get(clientIP)) + 1); //Update der Connection
                            pw.print(data);
                            pw.close();
                            socket.close();
                        } else {
                            Socket socket = new Socket(weiterIp.split(":")[0], Integer.parseInt(weiterIp.split(":")[1]));
                            PrintWriter pw = new PrintWriter(socket.getOutputStream());
                            server.put(weiterIp, server.get(weiterIp) + 1); //Update der Connection
                            log.put(clientIP, weiterIp);
                            pw.print(data);
                            pw.close();
                            socket.close();
                        }
                        for(String key : log.keySet())
                        {
                            System.out.print("Client: " + key + " - ");
                            System.out.print("Server: " + log.get(key) + "\n");
                        }
                        System.out.println();
                    }
                    else {
                        Socket socket = new Socket(weiterIp.split(":")[0], Integer.parseInt(weiterIp.split(":")[1]));
                        PrintWriter pw = new PrintWriter(socket.getOutputStream());
                        server.put(weiterIp, server.get(weiterIp) + 1); //Update der Connection
                        pw.print(data);
                        pw.close();
                        socket.close();
                    }
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
                    String data = br.readLine();
                    String port = data.split("/")[0];
                    int gewicht= Integer.parseInt(data.split("/")[1]);
                    if(!server.containsKey(ip)){
                        server.put(ip+":"+port, 0);
                        serverGew.put(ip+":"+port, gewicht);
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
        WeightedDistribution2 lb = new  WeightedDistribution2 ();
        lb.start();
    }
}