package Server;

import Logic.ValidityChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoGame {

    private int boardSize;
    String boardState;

    //if Black: 1, if White: 2
    private enum Players { black(1), white (2);
        private final int playerNumber;
        Players(int i) {
            this.playerNumber = i;
        }
    }

    enum GameState { WAITING, PLAYING, FINISHED }
    Players currentPlayer;
    GameState currentGameState;
    List<String> boardHistory = new ArrayList<>();
    Integer turnTimer;

    GoGame(Integer boardSize) {
        this.boardSize = boardSize;
        this.boardState = String.join("", Collections.nCopies(boardSize * boardSize, "0"));
        this.currentPlayer = Players.black;
        this.currentGameState = GameState.WAITING;
        this.boardHistory.add(boardState);
        this.turnTimer = 0;
    }
    int getCurrentPlayer() {
        return currentPlayer.playerNumber;
    }
    void changePlayer() {
        if (currentPlayer == Players.black) {
            currentPlayer = Players.white;
        } else {
            currentPlayer = Players.black;
        }
    }

    /**
     * Determine new board state after player has placed a tile on the current board.
     * @param messagingPlayer should be the current player.
     * @param playerMove the tile index and player color associated with a player's move.
     * @return the new boardString to be sent to the players.
     */
    void changeBoardState(ClientHandler messagingPlayer, int playerMove) {
        String newBoard = "";
        boardHistory.add(newBoard);
        this.boardState = newBoard;
    }

    /**
     * Calculates a player's score based on a certain board.
     * @param board the board to be used.
     * @param playerColor the color of the player being scored.
     * @return the player's score
     */
    public double calculateScore(main.Client.Board board, int playerColor) {
        return 0;
    }

    /**
     * Called when both players have passed after one another.
     */
    void determineWinner() {
        //calculateScore(black)
        //calculateScore(white)
    }
}
