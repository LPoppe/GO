package main.Client;
import main.Logic.GoGame;
import main.Server.GameHandler;

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
    private GameHandler.GameHandlerState gameState;

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
                host = InetAddress.getByName(readStringWithDefault("Please enter a host to connect to <localhost>: ", "localhost"));
            } catch (UnknownHostException e) {
                System.out.println("Host address not valid.");
            }
        }

        while (port == null) {
            try {
                port = Integer.parseInt(readStringWithDefault("Please enter a port number <7000>: ", "7000"));
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
        sendHandshake(readStringWithDefault("Please enter your name <Linda>: ", "Linda"));
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
        } catch (NullPointerException ne) {
            printToGame("Handshake acknowledgement missing gameID.");
        }
    }
    private void processConfigRequest(String[] splitMessage) {
        try {
            String preferredColor = readStringWithDefault("Provide color - 0 for random, 1 for black, 2 for white <black>: ", "1");
            String preferredBoardSize = readStringWithDefault("Provide board size <5>: ", "5");
            sendConfig(preferredColor, preferredBoardSize);
        } catch (NullPointerException ne) {
            printToGame("Config request did not match expected length.");
        }
    }

    /**Starts the client's side of the game.
     * @param splitMessage contains ACKNOWLEDGE_CONFIG, PLAYER_NAME,
     *                    COLOR, SIZE, GAME_STATE, OPPONENT.
     */
    private void processConfigAcknowledgement(String[] splitMessage) {
        if (splitMessage.length != 6) {
            printToGame("Configuration acknowledgement did not match expected length.");
        } else {
            this.clientName = splitMessage[1];
            int colorNumber = Integer.parseInt(splitMessage[2]);
            controller.setGame(Integer.valueOf(splitMessage[3]), splitMessage[5], colorNumber);
            this.gameState = GameHandler.GameHandlerState.PLAYING;
        }
    }

    /**Asks the user for input about what player type they would like to use.
     * @return either "H" or "A", depending on the player type that should be used in the game.
     */
    String askForPlayerType() {
        //Upon setting the game in processConfigAcknowledgment(), the controller will create
        // a player based on this input.
        String typeChoice;
        do {
            typeChoice = GoClient.readString("Please choose player type (H for human, A for AI): ").toUpperCase();
        } while (!(typeChoice.equals("H") || typeChoice.equals("A")));
        return typeChoice;
    }

    /**Processes a message containing a new move made on the board by this client or the opponent.
     * @param splitMessage contains ACKNOWLEDGE_MOVE, GAME_ID, MOVE, GAME_STATE
     */
    private void processMoveAcknowledgement(String[] splitMessage) {
        //TODO change back to 4
        if (splitMessage.length != 5) {
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
                System.out.println("N groups at server: " + splitMessage[4]);
                controller.updateTurnFromServer(currentPlayerColor, move, newBoard, splitMessage[4]);
            }
        }
    }

    private void processInvalidMoveSent(String[] splitMessage) {
        printToGame("Move was invalid. Try again.");
        controller.retryMove();
    }

    private void processUnknownCommandWarning(String[] splitMessage) {

    }

    private void processGameEnd(String[] splitMessage) {
        this.gameState = GameHandler.GameHandlerState.FINISHED;
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
     * @param tileIndex the tile on which the player places their move. -1 if passing.
     */
    //TODO sendMove happens out of turn regularly
    void sendMove(int tileIndex) {
        writeToStream("MOVE+" + this.gameID + "+" + this.clientName + "+" + tileIndex);
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
    private GameHandler.GameHandlerState getGameState() {
        return this.gameState;
    }

    //TODO Change from terminal to location on GUI. Also requires text input field on GUI, and immediate start of GUI.
    private void printToGame(String input) {
        System.out.println(input);
    }

    /**
     * Reads input from the terminal. Will repeat the prompt if nothing is written.
     * @param input prompt asking for user input.
     * @return the user input.
     */
    static String readStringWithDefault(String input, String dflt) {
        String message = null;

        try {
            System.out.print(input);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    System.in));
            message = in.readLine();
        } catch (IOException e) {
            System.out.println("readString failed: " + e.getMessage());
            System.exit(1);
        }

        if (message == null || message.trim().equals("")) {
            return dflt;
        } else {
            return message.trim();
        }
    }

    /**
     * Reads input from the terminal. Will repeat the prompt if nothing is written.
     * @param input prompt asking for user input.
     * @return the user input.
     */
    static String readString(String input) {
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
