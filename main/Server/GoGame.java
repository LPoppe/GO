package Server;

public class GoGame {
    //if Black: 1, if White: 2
    private int currentPlayer;
    private String boardState;

    public GoGame () {
        this.currentPlayer = 1;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Determine if a move made by a player is valid. Uses the current board.
     * @param playerMove
     * @return true if valid.
     */
    public boolean isValidMove(int playerMove) {
        //if move is valid, return true.
        //else return false.
        return false;
    }

    /**
     * Determine new board state after player has placed a tile on the current board.
     * @param playerMove the location of the added tile.
     * @return the new boardString to be sent to the players.
     */
    public String changeBoardState(int playerMove) {
        return null;
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
    public void determineWinner() {
        //calculateScore(black)
        //calculateScore(white)
    }
}
