import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Load Balancer Lesat Connection
 * @author Thomas Stedronsky
 * @author Erik Braendli
 * @version 02-03-2016
 */
public class LeastConnection extends Thread{
    public static final int SERVER_REG_PORT = 8888, CLIENT_RECV_PORT = 1025;
    private HashMap<String, Integer> server = new HashMap<String, Integer>();
    private ServerSocket serverRegSocket;
    private ServerSocket clientRecvSocket;
    public boolean serverRunning = true;
    public boolean clientRunning = true;
    public boolean sessionPersistance = false;
    public HashMap<String, String> log= new HashMap<String, String>();

    /**
     * Contstructor
     */
    public LeastConnection() {
        try {
            serverRegSocket = new ServerSocket(SERVER_REG_PORT);
            clientRecvSocket = new ServerSocket(CLIENT_RECV_PORT);
            new ListingForServerThread().start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * run methode
     */
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
        LeastConnection lb = new LeastConnection();
        lb.start();
    }
}