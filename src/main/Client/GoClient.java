package main.Client;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class GoClient extends Thread {

    //Communication
    private String clientName;
    private Socket sock;
    private Integer gameID;
    private BufferedReader in;
    private BufferedWriter out;


    /**Constructs a client object and attempts to create socket connection.
     * @param host
     * @param port  the server port
     */
    public GoClient(InetAddress host, int port) {
        try {
            this.sock = new Socket(host, port);
            this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(this.sock.getOutputStream()));
        }  catch (UnknownHostException uhe) {
            System.out.println("Server not found: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Missing argument(s). Expecting: <address> <port>");
            System.exit(0);
        }
        InetAddress host = null;
        int port = 0;

        try {
            host = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            System.out.println("No valid host address.");
            System.exit(0);
        }

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("No valid port.");
            System.exit(0);
        }

        GoClient client = new GoClient(host, port);
        client.start();
    }

    /**
     * Reads input from the server.
     */
    public void run() {
        writeToStream(sendHandshake(readString("Please enter your name: ")));

        String serverMessage;
        try {
            while (true) {
                serverMessage = in.readLine();
                processServerMessage(serverMessage);
            }
        } catch (IOException ioe) {
            System.out.println("Receiving message failed: " + ioe.getMessage());
        }
    }

    private void writeToStream(String message) {
        try {
            out.write(message);
            out.flush();
        } catch (IOException ioe) {
            System.out.println("Sending message failed: '" + message + "': " + ioe.getMessage());
        }
    }

    private void processServerMessage(String input) {
        String[] splitMessage = input.split("\\+");
        String cmd = splitMessage[0];
        switch (cmd) {
            case "REQUEST_CONFIG":
                String preferredColor = readString("Provide color - 0 for random, 1 for black, 2 for white: ");
                String preferredBoardSize = readString("Provide board size: ");
                writeToStream(sendConfig(preferredColor, preferredBoardSize));
                break;
            default:
                System.out.println("Command not recognised.");
        }
    }

    private String sendHandshake(String input) {
        this.clientName = input;
        return "HANDSHAKE+" + this.clientName + "\n";
    }

    private String sendConfig(String preferredColor, String preferredBoardSize) {
        return "SET_CONFIG+" + this.gameID + "+" + preferredColor + "+" + preferredBoardSize + "\n";
    }

    private String sendMove(int tileIndex) {
        return "MOVE+" + this.gameID + "+" + this.clientName + "+" + tileIndex + "\n";
    }

    private String sendPass() {
        return "PASS+" + this.gameID + "+" + this.clientName + "\n";
    }

    private String sendExit() {
        try {
            this.in.close();
            this.out.close();
            this.sock.close();
        } catch (IOException ioe) {
            System.out.println("Encountered problem while exiting: " + ioe.getMessage());
        }
        return "EXIT+" + this.gameID + "+" + this.clientName + "\n";
    }

    private static String readString(String input) {
        System.out.print(input);
        String message = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    System.in));
            message = in.readLine();
        } catch (IOException e) {
            System.out.println("readString failed: " + e.getMessage());
        }

        return (message == null) ? "" : message.trim();
    }
}
