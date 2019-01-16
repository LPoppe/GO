package Server;

import Client.GoClient;
import Logic.ValidityChecker;
import com.sun.security.ntlm.Client;

/**
 * Combines two opponent GoClients of a Go game played on the GoServer.
 */
public class GameHandler extends Thread {
    private ClientHandler player1;
    private ClientHandler player2;
    private GoGame goGame;
    private GoServer server;
    private final Integer gameID;

    GameHandler(GoServer server, ClientHandler firstPlayer, Integer gameID) {
        this.server = server;
        this.player1 = firstPlayer;
        this.gameID = gameID;
    }

    public void run() {
        while (player1.isAlive() && player2.isAlive()) {
            continue;
        }
        clientLeft();
    }

    void addPlayerTwo(ClientHandler secondPlayer) {
        this.player2 = secondPlayer;
    }

    private void broadcast(String message) {
        player1.sendMessage(message);
        player2.sendMessage(message);
    }

    /** Updates the game and its board after the turn's player makes a valid move.
     * @param messagingPlayer Should be the current turn's player (checked by ValidityChecker).
     * @param playerMove the tile index and player color associated with a player's move.
     * @return return false if a move is invalid. True if valid and processed.
     */
    private boolean processMove(ClientHandler messagingPlayer, int playerMove) {
        ValidityChecker checker = new ValidityChecker();
        String checkerMessage = checker.checkMove(messagingPlayer.getName(), playerMove);
        if (!checkerMessage.equals("VALID")) {
            messagingPlayer.sendMessage("INVALID_MOVE+" + checkerMessage + "\n");
            return false;
        } else {
            goGame.changeBoardState(messagingPlayer, playerMove);
            String gameState = goGame.currentGameState.name() + ";"
                    + goGame.getCurrentPlayer() + ";" + goGame.boardState + "\n";
            broadcast("ACKNOWLEDGE_MOVE+" + gameID + "+" + gameState + "\n");
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
            player1.sendMessage("UPDATE_STATUS+" + goGame.currentGameState.name() + "\n");
        } else if (player2.isAlive()) {
            player2.sendMessage("UPDATE_STATUS+" + goGame.currentGameState.name() + "\n");
        }
        //Remove players and game from the main server.
        server.removeHandler(player1);
        server.removeHandler(player2);
        server.removeGame(this);
    }
}
