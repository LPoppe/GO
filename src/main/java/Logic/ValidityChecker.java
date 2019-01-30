package Logic;

public class ValidityChecker {
    public ValidityChecker() {
    }

    /**
     * Determine if a move made by a player is valid. Uses the current board.
     * @param playerMove the tile index and player color associated with a player's move.
     * @return "VALID" if the player's move is valid. Otherwise the appropriate error message.
     */
    public String checkMove(int playerColorNumber, int playerMove, Board board) {
        //return ERROR MESSAGE if:
        //move is not a location on the board.
        if (!isValidCoordinate(playerMove, board)) {
            return "Coordinate is not a valid location on the board.";
        }
        //tile is not empty
        if (!isTileEmpty(playerMove, board)) {
            return "Chosen tile is already occupied.";
        }
        Board boardCopy = deepCopyBoard(board);
        boardCopy.setBoardState(playerColorNumber, playerMove);

        //tile does not recreate previous board state.
        if (!doesNotBreakKoRule(boardCopy.getBoardState(), board)) {
            return "Move breaks Ko rule.";
        }
        //if move is valid, return "VALID".
        //else return errorMessage.
        return "VALID";
    }

    /**Makes a deep copy of the provided board, so that it can safely be
     * altered without changing the actual game board.
     * @param board the game board to be copied
     * @return a new Board object.
     */
    public Board deepCopyBoard(Board board) {
        return new Board(board);
    }

    public boolean isValidCoordinate(int playerMove, Board board) {
        return board.getBoardFields().length > playerMove && playerMove >= 0;
    }

    public boolean isTileEmpty(int playerMove, Board board) {
        return board.getTileContent(playerMove) == 0;
    }

    /**Uses last turn's board to check if a previous board state is recreated by a move.
     */
    public boolean doesNotBreakKoRule(String newBoardState, Board board) {
        return !board.getBoardHistory().contains(newBoardState);
    }
}
