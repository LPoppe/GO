package main.Server;

import main.Logic.GoGame;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ClientHandler extends Thread {
    private String clientName;
    private Socket clientSocket;
    private Boolean isFirstPlayer;
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
            server.printOnServer("Socket connection lost: " + e.getMessage());
            shuttingDown();
        }
    }

    public String getClientName() {
        return this.clientName;
    }
    /**
     * Determines the correct response to a message sent by a client.
     * @param input
     */
    private void processMessage(String input) {
        server.printOnServer("From client: " + input);
        String[] splitMessage = input.split("\\+");
        String cmd = splitMessage[0];

        switch (cmd) {
            case "HANDSHAKE":
                //Identification to the server. Set name to the player name provided.
                //Acknowledges the player and sends back game ID and whether player is first to connect.
                processHandshake(splitMessage);
                break;
            case "SET_CONFIG":
                //Set the game configuration according to the preferred color and board size provided.
                processConfig(splitMessage);
                break;
            case "MOVE":
                //Determines if move is valid, updates the board, and sends the new game status to both players.
                processMove(splitMessage);
                break;
            case "EXIT":
                processExit(splitMessage);
                break;
            case "SET_REMATCH":
                processRematch(splitMessage);
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
        //TODO: remove check when done.
        if (message.contains("\n")) {
            throw new RuntimeException("Blegh: " + message);
        }

        try {
            server.printOnServer("Game " + gameHandler.getGameID()
                    + ", Player " + this.clientName + ": " + message);
            out.write(message + "\n");
            out.flush();
        } catch (IOException e) {
            server.printOnServer("Failed to write message. Shutting down: " + e.getMessage());
            shuttingDown();
        }
    }

    /**Sets the client name based on the name sent.
     * @param splitMessage should contain HANDSHAKE, PLAYER_NAME.
     */
    private void processHandshake(String[] splitMessage) {
        if (splitMessage.length != 2) {
            unknownCommand("Handshake did not contain name.");
            server.printOnServer("Handshake was incorrect length.");
        } else {
            this.clientName = splitMessage[1];
            sendLine("ACKNOWLEDGE_HANDSHAKE+" + gameHandler.getGameID() + "+" + booleanToInt(this.isFirstPlayer));
            gameHandler.handshakeReceived();
            if (this == gameHandler.getPlayer1()) {
                requestConfig();
            }
        }
    }

    private void requestConfig() {
        sendLine("REQUEST_CONFIG+Please provide game configuration");
    }
    /**Sets game configuration using the settings provided by the client.
     * Client needs to have been the first player to connect to the server.
     * @param splitMessage should contain SET_CONFIG, GAME_ID ,PREFERRED_COLOR, BOARD_SIZE.
     */
    private void processConfig(String[] splitMessage) {
        if (splitMessage.length != 4) {
            server.printOnServer(clientName + ": Set_config message was missing arguments.");
            unknownCommand("Message missing GameID, preferredColor, or boardSize.");
        } else {
            if (isFirstPlayer) {
                //Ignores gameId (splitMessage[1], as current structure doesn't depend on it
                int preferredColorInt = Integer.parseInt(splitMessage[2]);
                int boardSize = Integer.parseInt(splitMessage[3]);

                // Parse color.
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
                    server.printOnServer(clientName + ": Preferred color reached unexpected color: " + preferredColorInt);
                    unknownCommand("Preferred color should be 0, 1, or 2.");
                    return;
                }

                gameHandler.setConfig(this, preferredColor, boardSize);
            } else {
                server.printOnServer(clientName + ": Second player attempted to setConfig.");
                unknownCommand("Setting configuration failed: you are player two.");
            }
        }
    }

    /**Checks if it is the messaging player's turn and calls the GameHandler's processMove() method
     * to further handle the input.
     * @param splitMessage should contain MOVE, GAME_ID, PLAYER_NAME, TILE_INDEX
     */
    private void processMove(String[] splitMessage) {
        if (splitMessage.length != 4) {
            server.printOnServer(clientName +  ": Move message was missing arguments.");
            unknownCommand("Message missing gameID, username, or tileIndex.");
        } else {
            if (gameHandler.getCurrentPlayer() == this) {
                int move = Integer.parseInt(splitMessage[3]);
                    gameHandler.processMove(this, move);
            } else {
                sendLine("INVALID_MOVE+" + "Attempt at sending move out of turn.");
                server.printOnServer(clientName + ": Attempt at sending move out of turn.");
            }
        }
    }

    /**Processes the response to the REQUEST_REMATCH message.
     * @param splitMessage SET_REMATCH + '1' if rematch '0' if not.
     */
    private void processRematch(String[] splitMessage) {
    }
    /**Creates clean disconnect between clients and server after receiving exit message.
     * Shuts down anyway when the client does not provide the correct string,
     * because an EXIT command was sent, and the client may have already disconnected itself.
     * @param splitMessage should contain EXIT, GAME_ID, PLAYER_NAME
     */
    private void processExit(String[] splitMessage) {
        if (splitMessage.length != 3) {
            server.printOnServer(clientName + ": EXIT command was not properly sent. Closing down game.");
            shuttingDown();
        } else {
            server.printOnServer(clientName + ": EXIT command sent. Closing down game.");
            shuttingDown();
        }
    }

    private void shuttingDown() {
        gameHandler.clientLeft(this);
        try {
            this.in.close();
            this.out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unknownCommand(String errorMessage) {
        sendLine("UNKNOWN_COMMAND+" + errorMessage);
    }

}
