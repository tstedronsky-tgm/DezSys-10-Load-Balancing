import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by fusions on 12.02.16.
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
        for(int i=0; i<10;++i){
            SocketClient sc = new SocketClient("", 1025);
            sc.writeMsg(i+"");
        }
    }
}
