package main.Logic;

import java.util.Arrays;
import java.util.Collections;
import main.Logic.GoGame;
public class Board {

    private Integer boardSize;
    private String boardState;
    private Integer[] currentBoard;

    public enum Tiles {
        empty(0), black(1), white(2);
        private final int tileNumber;
        Tiles(int i) {
            this.tileNumber = i;
        }
    }
    private static String[] boardShape;
    private static Integer[] boardFields;
    private GoGame game;

    public Board(GoGame game, Integer preferredSize) {
        this.game = game;
        this.boardSize = preferredSize;
        this.boardState = String.join("", Collections.nCopies(boardSize * boardSize, "0"));
        initializeBoard();
    }

    public String getBoardState() {
        return this.boardState;
    }

    public Integer getBoardSize() {
        return this.boardSize;
    }
    /**Updates the board with a move.
     * @param playerColor the tile color to be added.
     * @param playerMove the location of the new tile.
     */
    public void setBoardState(int playerColor, int playerMove) {
        currentBoard[playerMove] = playerColor;
        updateBoard();
    }

    /**Creates an array of indexes for all tile location and an initial empty board.
     */
    private void initializeBoard() {
        boardFields = new Integer[boardSize * boardSize];
        Arrays.setAll(boardFields, index -> 1 + index);
        currentBoard = new Integer[boardSize * boardSize];
        Arrays.fill(currentBoard, Tiles.empty.tileNumber);
    }

    /**Updates the board view.
     */
    private void updateBoard() {
        //warning to listener or whatever
    }

    public String drawBoard() {
        return null;
    }
}
