package Server;

import Client.GoClient;

/**
 * Combines two opponent GoClients of a Go game played on the GoServer.
 */
public class GameHandler extends Thread {
    private ClientHandler player1;
    private ClientHandler player2;
    private ClientHandler currentPlayer;
    private final Integer GameID;

    public GameHandler(ClientHandler firstPlayer, Integer gameID) {
        this.player1 = firstPlayer;
        this.GameID = gameID;
        this.currentPlayer = this.player1;
    }

    public void run() {

    }

    public void addPlayerTwo(ClientHandler secondPlayer) {
        this.player2 = secondPlayer;
    }

    public void sendBoardUpdate() {
    }
}
