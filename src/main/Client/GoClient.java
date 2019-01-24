package main.Client;
import main.Logic.GoGame;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class GoClient extends Thread {

    //Communication
    private Socket sock;
    private BufferedReader in;
    private BufferedWriter out;
    private GoController controller;

    //Game characteristics
    private Integer gameID;
    private String clientName;
    private GoGame.GameState gameState;

    /**Constructs a client object and attempts to create socket connection.
     * The client processes all messages to and from the server.
     * Sends information to the controller during the game.
     */
    public GoClient(GoController myController) {
        this.controller = myController;

        InetAddress host = null;
        Integer port = null;

        while (host == null) {
            try {
                host = InetAddress.getByName(readString("Please enter a host to connect to."));
            } catch (UnknownHostException e) {
                System.out.println("Host address not valid.");
            }
        }

        while (port == null) {
            try {
                port = Integer.parseInt(readString("Please enter a port number."));
            } catch (NumberFormatException e) {
                System.out.println("Enter port not valid. Try again.");
            }
        }
        connectToStream(host, port);
        start();
    }

    public static void main(String[] args) {
        GoClient client = new GoClient(new GoController());
    }

    /**Reads input from the server.
     */
    public void run() {
        sendHandshake(readString("Please enter your name: "));
        String typeChoice = GoClient.readString("Please choose player type (H for human, A for AI): ");
        controller.initPlayer(typeChoice);
        String serverMessage;
        try {
            while (true) {
                serverMessage = in.readLine();
                processServerMessage(serverMessage);
            }
        } catch (IOException ioe) {
            printToGame("Connection to server lost: " + ioe.getMessage());
            closeClient();

        }
    }

    private void connectToStream(InetAddress host, int port) {
        try {
            this.sock = new Socket(host, port);
            this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(this.sock.getOutputStream()));
        }  catch (UnknownHostException uhe) {
            System.out.println("Server not found: " + uhe.getMessage());
            System.exit(0);
        } catch (IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
            System.exit(0);
        }
    }


    private void writeToStream(String message) {
        try {
            out.write(message + "\n");
            out.flush();
        } catch (IOException ioe) {
            printToGame("Sending message failed: '" + message + "': " + ioe.getMessage());
        }
    }
    //TODO maybe check if the right game is sending messages.
    private void processServerMessage(String input) {
        String[] splitMessage = input.split("\\+");
        String cmd = splitMessage[0];
        switch (cmd) {
            case "ACKNOWLEDGE_HANDSHAKE":
                processHandshakeAcknowledgement(splitMessage);
                break;
            case "REQUEST_CONFIG":
                processConfigRequest(splitMessage);
                break;
            case "ACKNOWLEDGE_CONFIG":
                processConfigAcknowledgement(splitMessage);
                break;
            case "ACKNOWLEDGE_MOVE":
                processMoveAcknowledgement(splitMessage);
                break;
            case "INVALID_MOVE":
                processInvalidMoveSent(splitMessage);
                break;
            case "UNKNOWN_COMMAND":
                processUnknownCommandWarning(splitMessage);
                break;
            case "GAME_FINISHED":
                processGameEnd(splitMessage);
                break;
            default:
                printToGame("Command not recognised.");
        }
    }

    private void processHandshakeAcknowledgement(String[] splitMessage) {
        //* The boolean is_leader (splitMessage[2]) is ignored in the current configuration.*/
        try {
            this.gameID = Integer.valueOf(splitMessage[1]);
            this.gameState = GoGame.GameState.WAITING;
        } catch (NullPointerException ne) {
            printToGame("Handshake acknowledgement missing gameID.");
        }
    }
    private void processConfigRequest(String[] splitMessage) {
        try {
            String preferredColor = readString("Provide color - 0 for random, 1 for black, 2 for white: ");
            String preferredBoardSize = readString("Provide board size: ");
            sendConfig(preferredColor, preferredBoardSize);
        } catch (NullPointerException ne) {
            printToGame("Config request did not match expected length.");
        }
    }

    private void processConfigAcknowledgement(String[] splitMessage) {
        if (splitMessage.length != 6) {
            printToGame("Configuration acknowledgement did not match expected length.");
        } else {
            this.clientName = splitMessage[1];
            int colorNumber = Integer.parseInt(splitMessage[2]);
            controller.setGame(Integer.valueOf(splitMessage[3]), splitMessage[5], colorNumber);
            this.gameState = GoGame.GameState.PLAYING;
        }
    }

    /**Processes a message containing a new move made on the board by this client or the opponent.
     * @param splitMessage contains ACKNOWLEDGE_MOVE, GAME_ID, MOVE, GAME_STATE
     */
    private void processMoveAcknowledgement(String[] splitMessage) {
        if (splitMessage.length != 4) {
            printToGame("Move acknowledgement did not match expected length.");
        } else {
            //gameState should look like: STATUS;CURRENT_PLAYER;BOARD
            String gameState = splitMessage[3];
            String[] splitGameState = gameState.split(";");

            //Checks message length and update the current turn status.
            if (splitGameState.length != 3) {
                printToGame("GameState message did not match expected length.");
            } else {
                String move = splitMessage[2];
                int currentPlayerColor = Integer.valueOf(splitGameState[1]);
                String newBoard = splitGameState[2];
                controller.updateTurnFromServer(currentPlayerColor, move, newBoard);
            }
        }
    }

    private void processInvalidMoveSent(String[] splitMessage) {

    }
    private void processUnknownCommandWarning(String[] splitMessage) {

    }

    private void processGameEnd(String[] splitMessage) {
        this.gameState = GoGame.GameState.FINISHED;
    }

    private void sendHandshake(String input) {
        this.clientName = input;
        writeToStream("HANDSHAKE+" + this.clientName);
    }

    private void sendConfig(String preferredColor, String preferredBoardSize) {
        writeToStream("SET_CONFIG+" + this.gameID + "+" + preferredColor + "+" + preferredBoardSize);
    }

    /**Sends the player's move to the server. The controller handles
     * retrieving this move and calling this method.
     * @param tileIndex the tile on which the player places their move.
     */
    void sendMove(int tileIndex) {
        writeToStream("MOVE+" + this.gameID + "+" + this.clientName + "+" + tileIndex);
    }

    private void sendPass() {
        writeToStream("PASS+" + this.gameID + "+" + this.clientName);
    }

    private void sendExit() {
        writeToStream("EXIT+" + this.gameID + "+" + this.clientName);
        closeClient();
    }

    /**Called when client decides to leave the game, or close after finishing a game.
     * Closes the input and output stream and the socket.
     */
    private void closeClient() {
        try {
            this.in.close();
            this.out.close();
            this.sock.close();
        } catch (IOException ioe) {
            printToGame("Encountered problem while exiting: " + ioe.getMessage());
        }
    }
    private GoGame.GameState getGameState() {
        return this.gameState;
    }
    private void printToGame(String input) {
        System.out.println(input);
    }

    /**
     * Reads input from the terminal. Will repeat the prompt if nothing is written.
     * @param input prompt asking for user input.
     * @return the user input.
     */
    private static String readString(String input) {
        String message = null;

        do {
            try {
                System.out.print(input);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        System.in));
                message = in.readLine();
            } catch (IOException e) {
                System.out.println("readString failed: " + e.getMessage());
            }
        } while (message == null || message.equals(""));

        return message.trim();
    }
}
