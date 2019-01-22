package main.Client;
import main.Logic.GoGame;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class GoClient extends Thread {

    //Communication
    private Socket sock;
    private BufferedReader in;
    private BufferedWriter out;

    //Game characteristics
    private Integer gameID;
    private String clientName;
    private GoGame.PlayerColor clientColor;
    private boolean isClientsTurn;
    private Integer boardSize;
    private String opponentName;

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

    /**Reads input from the server.
     */
    public void run() {
        sendHandshake(readString("Please enter your name: "));

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
            case "UPDATE_STATUS":
                processStatusUpdate(splitMessage);
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
            String preferredColor = readString("Provide color - 0 for random, 1 for black, 2 for white: ");
            String preferredBoardSize = readString("Provide board size: ");
            sendConfig(preferredColor, preferredBoardSize);
        } catch (NullPointerException ne) {
            printToGame("Config request did not match expected length.");
        }
    }

    private void processConfigAcknowledgement(String[] splitMessage) {
        printToGame(Arrays.toString(splitMessage));
        try {
            this.clientName = splitMessage[1];
            int colorNumber = Integer.parseInt(splitMessage[2]);

            if (colorNumber == 1) {
                this.clientColor = GoGame.PlayerColor.black;
            } else if (colorNumber == 2) {
                this.clientColor = GoGame.PlayerColor.white;
            } else {
                printToGame("Impossible tile color assigned to player.");
            }

            this.boardSize = Integer.valueOf(splitMessage[2]);
            this.opponentName = splitMessage[4];
        } catch (NullPointerException ne) {
            printToGame("Config acknowledgement did not match expected length.");
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
            if (splitGameState.length != 4) {
                printToGame("GameState message did not match expected length.");
            } else {
                Integer move = Integer.valueOf(splitMessage[2]);
                isClientsTurn = Integer.valueOf(splitGameState[3]) == clientColor.getPlayerColorNumber();
            }
        }
    }

    private void processInvalidMoveSent(String[] splitMessage) {

    }
    private void processUnknownCommandWarning(String[] splitMessage) {

    }

    private void processStatusUpdate(String[] splitMessage) {

    }
    private void processGameEnd(String[] splitMessage) {

    }

    private void sendHandshake(String input) {
        this.clientName = input;
        writeToStream("HANDSHAKE+" + this.clientName);
    }

    private void sendConfig(String preferredColor, String preferredBoardSize) {
        writeToStream("SET_CONFIG+" + this.gameID + "+" + preferredColor + "+" + preferredBoardSize);
    }

    private void sendMove(int tileIndex) {
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

    private void printToGame(String input) {
        System.out.println(input);
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
