import java.io.PrintWriter;
import java.net.Socket;

/**
 * Socket Client
 * @author Thomas Stedronsky
 * @author Erik Braendli
 * @version 02-03-2016
 */
public class SocketClient {
    private String host;
    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {

        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     *
     * @param addr ip addr of the service endpoint
     * @param port port of the service endpoint
     */
    public SocketClient(String addr, int port){
        this.host=addr;
        this.port = port;
    }

    /**
     * send Message to Server
     * @param msg Message to server
     */
    public void writeMsg(String msg){
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.print(msg);
            pw.close();
            socket.close();
        } catch (java.io.IOException e) {
            System.err.print(e);
        } finally {

        }
    }

    public static void main (String ... args){
        int port =1025;
        SocketClient sc = new SocketClient("127.0.0.1", port);
        for(int i=0; i<100;++i){
            sc.writeMsg(i+"");
        }
    }
}
