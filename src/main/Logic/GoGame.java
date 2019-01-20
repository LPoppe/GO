package main.Logic;

import main.Server.ClientHandler;

import java.util.*;

/**
 * Keeps track of the turns in an ongoing GO game. Used by the server's GameHandler and the client.
 */
public class GoGame {

    private Board goBoard;

    //if Black: 1, if White: 2
    public enum PlayerColor {
        black(1), white (2);
        final int playerColorNumber;
        PlayerColor(int i) {
            this.playerColorNumber = i;
        }
    }

    private PlayerColor currentPlayerColor;
    private ClientHandler blackClient;
    private ClientHandler whiteClient;

    public enum GameState { WAITING, PLAYING, FINISHED }
    public GameState currentGameState;

    private List<String> boardHistory = new ArrayList<>();
    public Integer turnTimer;

    public GoGame(ClientHandler player1, PlayerColor color, Integer boardSize) {
        this.goBoard = new Board(this, boardSize);
        setPlayerOneColor(player1, color);
        this.currentPlayerColor = PlayerColor.black;

        this.currentGameState = GameState.WAITING;
        this.boardHistory.add(goBoard.getBoardState());
        this.turnTimer = 0;
    }

    private void setPlayerOneColor(ClientHandler playerOne, PlayerColor playerOneColor) {
        if (playerOneColor == PlayerColor.black) {
            this.blackClient = playerOne;
        } else {
            this.whiteClient = playerOne;
        }
    }

    //Must be called after setPlayerOneColor().
    public void setPlayerTwo(ClientHandler playerTwo) {
        if (this.blackClient != null) {
            this.whiteClient = playerTwo;
        } else {
            this.blackClient = playerTwo;
        }
    }

    public int getCurrentPlayerColorNumber() {
        return currentPlayerColor.playerColorNumber;
    }

    public String getBoardState() {
        return goBoard.getBoardState();
    }

    public Integer getBoardSize() {
        return goBoard.getBoardSize();
    }

    //Requires player to exist in the map.
    //Can only be called after setPlayerOneColor() and setPlayerTwoColor().
    public PlayerColor getColorByClient(ClientHandler player) {
        return player == this.blackClient ? PlayerColor.black : PlayerColor.white;
    }

    public ClientHandler getClientByColor(PlayerColor color) {
        return color == PlayerColor.black ? this.blackClient : this.whiteClient;
    }

    public List<String> getBoardHistory() {
        return boardHistory;
    }

    public void changePlayer() {
        if (currentPlayerColor == PlayerColor.black) {
            currentPlayerColor = PlayerColor.white;
        } else {
            currentPlayerColor = PlayerColor.black;
        }
    }

    /**
     * Determine new board state after player has placed a tile on the current board.
     * @param messagingPlayer should be the current player.
     * @param playerMove the tile index and player color associated with a player's move.
     * @return the new boardString to be sent to the players.
     */
    public void changeBoardState(ClientHandler messagingPlayer, int playerMove) {
        if (getColorByClient(messagingPlayer) != currentPlayerColor) {
            goBoard.setBoardState(getColorByClient(messagingPlayer).playerColorNumber, playerMove);
            boardHistory.add(goBoard.getBoardState());
        } else {
            System.out.println("Wrong player. This should not be reached.");
        }
    }

    /**
     * Calculates a player's score based on a certain board.
     * @param board the board to be used.
     * @param playerColor the color of the player being scored.
     * @return the player's score
     */
    public double calculateScore(Board board, int playerColor) {
        return 0.0;
    }

    /**
     * Called when both players have passed after one another.
     */
    public void determineWinner(Board board) {
        double scoreBlack = calculateScore(board, PlayerColor.black.playerColorNumber);
        double scoreWhite = calculateScore(board, PlayerColor.white.playerColorNumber);

        }
        //
}
