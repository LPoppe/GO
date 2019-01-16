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
    private int boardColor;
    private int gameID;

    /**Constructs a client object and attempts to create socket connection.
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
        }  catch (UnknownHostException uhe) {
            System.out.println("Server not found: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
        }
    }

    public void run() {

    }

    public String sendHandshake() {
        return "HANDSHAKE+" + this.clientName + "\n";
    }

    public String sendConfig(String preferredColor, Integer preferredBoardSize) {
        return "SET_CONFIG+" + this.gameID + "+" + preferredColor + "+" + preferredBoardSize + "\n";
    }

    public String sendMove(int tileIndex) {
        return "MOVE+" + this.gameID + "+" + this.clientName + "+" + tileIndex + "\n";
    }

    public String sendPass() {
        return "PASS+" + this.gameID + "+" + this.clientName + "\n";
    }

    public String sendExit() {
        try {
            this.in.close();
            this.out.close();
            this.sock.close();
        } catch (IOException ioe) {
            System.out.println("Encountered problem while exiting: " + ioe.getMessage());
        }
        return "EXIT+" + this.gameID + "+" + this.clientName + "\n";
    }
}
