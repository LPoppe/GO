package main.Server;

import main.Logic.GoGame;
import main.Logic.ValidityChecker;

/**
 * Combines two opponent GoClients of a Go game played on the GoServer.
 */
public class GameHandler {
    private ClientHandler player1;
    private ClientHandler player2;
    private GoGame goGame;
    private GoServer server;
    private final Integer gameID;

    public enum GameHandlerState { INIT, PLAYING }
    private GameHandlerState currentState;

    GameHandler(GoServer server, Integer gameID) {
        this.server = server;
        this.gameID = gameID;
        this.currentState = GameHandlerState.INIT;
    }

    /**Requires currentState != PLAYING.
     * Sends the acknowledgement of the configuration to the players.
     */
    private synchronized void setNextState() {
        if (player1 != null && player2 != null && goGame != null) {
            goGame.setPlayerTwo(player2);
            currentState = GameHandlerState.PLAYING;
            player1.sendLine("ACKNOWLEDGE_CONFIG+" + player1.getName()
                    + "+" + goGame.getColorByClient(player1) + "+" + goGame.getBoardSize()
                    + "+" + goGame.getBoardState() + "+" + player2.getName());
            player2.sendLine("ACKNOWLEDGE_CONFIG+" + player2.getName()
                    + "+" + goGame.getColorByClient(player2) + goGame.getBoardSize()
                    + "+" + goGame.getBoardState() + "+" + player1.getName());
        } else {
            currentState = GameHandlerState.INIT;
        }
    }

    private void requestConfig(ClientHandler firstPlayer) {
        firstPlayer.sendLine("REQUEST_CONFIG+Please provide game configuration");
    }

    /**
     * Requires the user command to match $PREFERRED_COLOR+$BOARD_SIZE.
     * @param firstPlayer ClientHandler of client that provides the configuration.
     * @param preferredColor the tile color the client wishes to play the game as.
     * @param boardSize the preferred size of the go board.
     */
    void setConfig(ClientHandler firstPlayer, GoGame.PlayerColor preferredColor, Integer boardSize) {
        this.goGame = new GoGame(firstPlayer, preferredColor, boardSize);
    }

    /**
     * Adds first player to the game. Can only be called once.
     * Requires newPlayer != null
     */
    void addPlayer1(ClientHandler newPlayer) {
        this.player1 = newPlayer;
        this.requestConfig(this.player1);
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

    /**Sends a message to both players in the game. Doing so also prints it to the server.
     * @param message The message to be broadcast.
     */
    synchronized void broadcast(String message) {
        player1.sendLine(message);
        player2.sendLine(message);
    }

    /** Updates the game and its board after the turn's player makes a valid move.
     * @param messagingPlayer Should be the current turn's player (checked by ValidityChecker).
     * @param playerMove the tile index and player color associated with a player's move.
     * @return return false if a move is invalid. True if valid and processed.
     */
    private synchronized boolean processMove(ClientHandler messagingPlayer, int playerMove) {
        ValidityChecker checker = new ValidityChecker();
        String checkerMessage = checker.checkMove(messagingPlayer.getName(), playerMove);
        if (!checkerMessage.equals("VALID")) {
            messagingPlayer.sendLine("INVALID_MOVE+" + checkerMessage);
            return false;
        } else {
            goGame.changeBoardState(messagingPlayer, playerMove);
            String gameState = goGame.currentGameState.name() + ";"
                    + goGame.getCurrentPlayerColorNumber() + ";" + goGame.getBoardState();
            broadcast("ACKNOWLEDGE_MOVE+" + gameID + "+" + gameState);
            goGame.changePlayer();
            goGame.turnTimer++;
            return true;
        }
    }

    /**
     * * Handles the disconnect of one of the clients.
     */
    private void clientLeft() {
        goGame.currentGameState = GoGame.GameState.FINISHED;
        //Notify other client. UPDATE_STATUS+FINISHED
        if (player1.isAlive()) {
            player1.sendLine("UPDATE_STATUS+" + goGame.currentGameState.name());
        } else if (player2.isAlive()) {
            player2.sendLine("UPDATE_STATUS+" + goGame.currentGameState.name());
        }
        //Remove players and game from the main server.
        server.removeHandler(player1);
        server.removeHandler(player2);
        server.removeGame(this);
    }

    public int getGameID() {
        return this.gameID;
    }
}