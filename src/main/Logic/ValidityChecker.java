package main.Logic;

public class ValidityChecker {

    public ValidityChecker() {
    }

    /**
     * Determine if a move made by a player is valid. Uses the current board.
     * @param playerMove the tile index and player color associated with a player's move.
     * @return "VALID" if the player's move is valid. Otherwise the appropriate error message.
     */
    public String checkMove(int playerMove, Board board) {
        //return ERROR MESSAGE if:
        //move is not a location on the board.
        isValidCoordinate();
        //tile is not empty
        isTileEmpty();
        //tile does not recreate previous board state.
        doesNotBreakKoRule();
        //if move is valid, return "VALID".
        //else return errorMessage.
        return null;
    }

    private boolean isValidCoordinate() {
        return false;
    }

    private boolean isTileEmpty() {
        return false;
    }

    /**Uses last turn's board to check if a previous board state is recreated by a move.
     */
    private boolean doesNotBreakKoRule() {
        return false;
    }
}
