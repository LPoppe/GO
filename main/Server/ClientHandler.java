package Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private GoServer server;
    private BufferedReader in;
    private BufferedWriter out;


    /**
     * Constructs a ClientHandler that handles communication with the client for the server.
     * @param server Server that creates the clientHandler.
     * @param clientSocket Socket the client uses to connect to the server.
     */
    ClientHandler(GoServer server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String input;
        try {
            while ((input = in.readLine()) != null) {
                continue;
            }
        } catch (IOException e) {
            System.out.println("Socket connection lost: " + e.getMessage());
            this.clientLeft();
        }
    }

    public void sendMessage(String message) {
        try {
            out.write(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to write message. Shutting down: " + e.getMessage());
            clientLeft();
        }
    }

    public void clientLeft() {
        server.removeHandler(this);
        //Send message that client has left.
    }
}
