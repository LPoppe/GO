package main.Server;

import main.Logic.GoGame;
import main.Logic.ValidityChecker;

/**Combines two opponent GoClients of a Go game played on the GoServer.
 */
public class GameHandler {
    private ClientHandler player1;
    private ClientHandler player2;
    private GoGame goGame;
    private GoServer server;
    private Integer gameID;
    private boolean disconnectAttempted = false;
    public enum GameHandlerState { INIT, PLAYING, FINISHED }
    private GameHandlerState currentState;
    private ValidityChecker checker;
    private boolean lastPlayerPassed;

    GameHandler(GoServer server, int gameID) {
        this.checker = new ValidityChecker();
        this.server = server;
        server.printOnServer("GAMEID = " + gameID);
        this.gameID = gameID;
        this.currentState = GameHandlerState.INIT;
    }

    /**Requires currentState != PLAYING.
     * Sends the acknowledgement of the configuration to the players.
     */
    //TODO GAME STARTS AS SOON AS PLAYER 2 IS CONNECTED, INSTEAD OF AFTER PLAYER 2 HANDSHAKE
    private synchronized void setNextState() {
        if (player1 != null && player2 != null && goGame != null) {
            if (player1.getClientName() != null && player2.getClientName() != null) {
                goGame.setPlayerTwo(player2);
                currentState = GameHandlerState.PLAYING;
                player1.sendLine("ACKNOWLEDGE_CONFIG+" + player1.getClientName()
                        + "+" + goGame.getColorByClient(player1).getPlayerColorNumber() + "+" + goGame.getBoardSize()
                        + "+" + goGame.getBoardState() + "+" + player2.getClientName());
                player2.sendLine("ACKNOWLEDGE_CONFIG+" + player2.getClientName()
                        + "+" + goGame.getColorByClient(player2).getPlayerColorNumber() + "+" + goGame.getBoardSize()
                        + "+" + goGame.getBoardState() + "+" + player1.getClientName());
            } else {
                currentState = GameHandlerState.INIT;
            }
        } else {
            currentState = GameHandlerState.INIT;
        }
    }

    /**Called by the clientHandler to check if it needs to ask the player for a configuration.
     *@return the first player to connect.
     */
    ClientHandler getPlayer1() {
        return player1;
    }


    /**Requires the user command to match $PREFERRED_COLOR+$BOARD_SIZE.
     * @param firstPlayer ClientHandler of client that provides the configuration.
     * @param preferredColor the tile color the client wishes to play the game as.
     * @param boardSize the preferred size of the go board.
     */
    void setConfig(ClientHandler firstPlayer, GoGame.PlayerColor preferredColor, Integer boardSize) {
        this.goGame = new GoGame(firstPlayer, preferredColor, boardSize);
        setNextState();
    }

    /**Adds first player to the game. Can only be called once.
     * Requires newPlayer != null
     */
    void addPlayer1(ClientHandler newPlayer) {
        this.player1 = newPlayer;
        setNextState();
    }

    /**Adds second player to the game.
     * Can only be called once, and must be called after addPlayer1().
     * Requires this.player1 != null && this.player2 == null && newPlayer != null
     */
    void addPlayer2(ClientHandler newPlayer) {
        this.player2 = newPlayer;
        setNextState();
    }

    void handshakeReceived() {
        //The game will only start after receiving a handshake from both players.
        // The clientHandler calls this to notify the gameHandler the game might be ready to start.
        setNextState();
    }

    /**Sends a message to both players in the game. Doing so also prints it to the server.
     * @param message The message to be broadcast.
     */
    private synchronized void broadcast(String message) {
        player1.sendLine(message);
        player2.sendLine(message);
    }

    /** Updates the game and its board after the turn's player makes a valid move.
     * @param messagingPlayer Should be the current turn's player (checked in ClientHandler).
     * @param playerMove the tile index and player color associated with a player's move.
     * @return return false if a move is invalid. True if valid and processed.
     */
    synchronized void processMove(ClientHandler messagingPlayer, int playerMove) {
        //First check if both players have passed:
        if (playerMove == -1) {
            if (lastPlayerPassed) {
                ClientHandler winner = goGame.getClientByColor(GoGame.determineWinner(goGame.getBoard()));
                broadcast("GAME_FINISHED+" + "+" + gameID + "+" + winner.getClientName() + "+" +
                        goGame.getPlayerScores() + "+" + "Both players passed turn.");
            }
        } else {
            String checkerMessage = checker.checkMove(goGame.getColorByClient(messagingPlayer).getPlayerColorNumber(),
                    playerMove, goGame.getBoard());
            if (!checkerMessage.equals("VALID")) {
                messagingPlayer.sendLine("INVALID_MOVE+" + checkerMessage);
            } else {
                //Reset the pass tracker.
                lastPlayerPassed = false;
                //Update the board.
                goGame.changeBoardState(messagingPlayer, playerMove);
                //The game state includes the current status, the current player (to make next move),
                //and the new board represented as a string.
                String gameState = this.currentState + ";"
                        + goGame.getCurrentPlayerColorNumber() + ";" + goGame.getBoardState();
                //Move contains the move made and the player's color.
                String move = playerMove + ";" + goGame.getColorByClient(messagingPlayer).getPlayerColorNumber();
                broadcast("ACKNOWLEDGE_MOVE+" + gameID + "+" + move + "+" + gameState);
                goGame.turnTimer++;
            }
        }
    }

    /**
     * * Handles the disconnect of one of the clients.
     */
    void clientLeft(ClientHandler client) {
        //TODO nullpointerexception if client 2 does not exist yet.
        if (!disconnectAttempted) {
            this.currentState = GameHandlerState.FINISHED;
            //Notify other client. UPDATE_STATUS+FINISHED
            if (client == player2) {
                player1.sendLine("UPDATE_STATUS+" + this.currentState);
                player1.sendLine("GAME_FINISHED+" + gameID + player1.getClientName() +
                        goGame.getPlayerScores() + "Other player disconnected.");

            } else if (client == player1) {
                player2.sendLine("UPDATE_STATUS+" + this.currentState);
                player2.sendLine("GAME_FINISHED+" + gameID + player2.getClientName() +
                        goGame.getPlayerScores() + "Other player disconnected.");
            }
            //Remove players and game from the main server.
            server.removeHandler(player1);
            server.removeHandler(player2);
            server.removeGame(this);
            disconnectAttempted = true;
        }
    }

    /**Returns the game's ID.*/
    int getGameID() {
        return this.gameID;
    }

    /**Returns the Client currently allowed to send a move.*/
    ClientHandler getCurrentPlayer() {
        return goGame.getCurrentClient();
    }
}
