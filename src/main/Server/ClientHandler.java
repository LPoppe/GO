package main.Server;

import main.Logic.GoGame;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ClientHandler extends Thread {
    private String clientName;
    private Socket clientSocket;
    private Boolean isFirstPlayer;
    private Boolean isCurrentPlayer;
    private GoServer server;
    private GameHandler gameHandler;
    private BufferedReader in;
    private BufferedWriter out;


    /**
     * Constructs a ClientHandler that handles communication with the client for the server.
     * @param server Server that creates the clientHandler.
     * @param clientSocket Socket the client uses to connect to the server.
     */
    ClientHandler(GoServer server, GameHandler gamehandler, Socket clientSocket, Boolean isFirst) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.gameHandler = gamehandler;
        this.isFirstPlayer = isFirst;
        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String input;
        String textDelimiter = "+";
        String gameStateDelimiter = ";";

        try {
            while ((input = in.readLine()) != null) {
                processMessage(input);
            }
        } catch (IOException e) {
            System.out.println("Socket connection lost: " + e.getMessage());
            this.clientLeft();
        }
    }

    /**
     * Determines the correct response to a message sent by a client.
     * @param input
     */
    private void processMessage(String input) {
        server.printOnServer("From client: " + input);
        String[] splitMessage = input.split("\\+");
        server.printOnServer(Arrays.toString(splitMessage));
        String cmd = splitMessage[0];

        switch (cmd) {
            //Identification to the server. Set name to the player name provided.
            //Acknowledges the player and sends back game ID and whether player is first to connect.
            case "HANDSHAKE":
                this.clientName = splitMessage[1];
                sendLine("ACKNOWLEDGE_HANDSHAKE+" + gameHandler.getGameID() + "+"
                        + booleanToInt(this.isFirstPlayer));
                break;
            //Set the game configuration according to the preferred color and board size provided.
            case "SET_CONFIG":
                // TODO: Check array length, send error message if unexpected length

                // We ignore gameId, as current structure doesn't depend on it
                //String gameId = splitMessage[1];


                int preferredColorInt = Integer.parseInt(splitMessage[2]);
                int boardSize = Integer.parseInt(splitMessage[3]);

                // Parse color
                GoGame.PlayerColor preferredColor;
                if (preferredColorInt == 0) {
                    if (Math.random() < 0.5) {
                        preferredColor = GoGame.PlayerColor.black;
                    } else {
                        preferredColor = GoGame.PlayerColor.white;
                    }
                } else if (preferredColorInt == 1) {
                    preferredColor = GoGame.PlayerColor.black;
                } else if (preferredColorInt == 2) {
                    preferredColor = GoGame.PlayerColor.white;
                } else {
                    unknownCommand("Preferred color should be 0, 1, or 2.");
                    server.printOnServer("Preferred color reached unexpected color: " + preferredColorInt);
                    break;
                }

                if (isFirstPlayer) {
                    gameHandler.setConfig(this, preferredColor, boardSize);
                } else {
                    unknownCommand("Setting configuration failed: you are player two.");
                    server.printOnServer("Second player attempted to setConfig.");
                    break;
                }
                break;
            default:
                unknownCommand("Command not recognised.");
                server.printOnServer("No match to message found.");
        }
    }

    /**Needed to convert isFirstPlayer to int.
     * @param value true or false, depending on whether the client was the first player to connect.
     * @return 1 if true, 0 if false.
     */
    private static int booleanToInt(boolean value) {
        // Convert true to 1 and false to 0.
        return value ? 1 : 0;
    }

    /**
     * Send a message to the connected client, and print it to the server.
     * Assumes the client has disconnected if the message cannot be sent.
     * @param message the message sent.
     */
    void sendLine(String message) {
        if (message.contains("\n")) {
            throw new RuntimeException("Blegh: " + message);
        }

        try {
            server.printOnServer("Game " + gameHandler.getGameID()
                    + ", Player " + this.getName() + ": " + message);
            out.write(message + "\n");
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to write message. Shutting down: " + e.getMessage());
            clientLeft();
        }
    }

    private void unknownCommand(String errorMessage) {
        sendLine("UNKNOWN_COMMAND+" + errorMessage);
    }

    /**
     * ???
     */
    private void clientLeft() {
        //Send message that client has left.
    }
}
