package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class GoClient extends Thread {

    //Communication
    private String clientName;
    private Socket sock;
    private BufferedReader in;
    private BufferedWriter out;

    //Parameters for playing the game
    private int BoardColor;

    /**
     * Constructs a client object and attempts to create socket connection.
     * @param name  name of the player
     * @param host
     * @param port  the server port
     */
    public GoClient(String name, InetAddress host, int port) {
        try {
            this.clientName = name;
            this.sock = new Socket(host, port);
            this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(this.sock.getOutputStream()));
        }  catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    public void run() {

    }

    public void sendHandshake() {

    }

    public void setConfig() {

    }

    public void sendMove() {

    }

    public void sendPass() {

    }

    public void sendExit() {
        try {
            this.in.close();
            this.out.close();
            this.sock.close();
        } catch (IOException e) {
            System.out.println("Encountered problem while exiting: " + e.getMessage());
        }
    }
}
