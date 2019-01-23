package main.Logic;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Board {

    private Integer boardSize;
    private String boardState;
    private Integer[] currentBoard;
    private List<String> boardHistory = new ArrayList<>();

    /**Updates the list of board strings (boardHistory).
     * @param theBoardState
     */
    public void addToHistory(String theBoardState) {
        boardHistory.add(theBoardState);
    }

    public List<String> getBoardHistory() {
        return boardHistory;
    }


    public enum Tiles {
        empty(0), black(1), white(2);
        private final int tileNumber;
        Tiles(int i) {
            this.tileNumber = i;
        }
    }

    private static Integer[] boardFields;

    public Board(Integer preferredSize) {
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

    public Pair<Integer, Integer> getTileCoordinates(int tileIndex) {
        Integer x = tileIndex % boardSize;
        Integer y = tileIndex / boardSize;
        return new Pair<>(x, y);
    }

    /**Returns the tile content at a certain location on the board.
     */
    public Integer getTileContent(int x, int y) {
        return this.currentBoard[x + y * boardSize];
    }

    /**Creates an array of indexes for all tile location and an initial empty board.
     */
    private void initializeBoard() {
        boardFields = new Integer[boardSize * boardSize];
        Arrays.setAll(boardFields, index -> 1 + index);
        currentBoard = new Integer[boardSize * boardSize];
        Arrays.fill(currentBoard, Tiles.empty.tileNumber);
    }

    /**Updates the board with a move.
     * @param playerColorNumber the tile color to be added. Requires the playerColor to be 1 or 2.
     * @param playerMove the location of the new tile.
     */
    public void setBoardState(int playerColorNumber, int playerMove) {
        if (playerColorNumber == Tiles.black.tileNumber || playerColorNumber == Tiles.white.tileNumber) {
            updateBoard(playerColorNumber, playerMove);
        } else {
            System.out.println("Unknown player sent move.");
        }
    }

    private void updateBoard(int tileColor, int tileIndex) {
        if (tileColor == Tiles.black.tileNumber) {

        } else {

        }
    }

    /**
     * Draw a string representation of the current board state, which may be used in the TUI.
     * @return a string picturing the current board.
     */
    public String drawBoard() {
        int x = 0;
        int y = 0;
        StringBuilder boardDrawing = new StringBuilder();
        for (Integer field : currentBoard) {
            if (x < boardSize - 1) {
                boardDrawing.append(field);
                        //.append("-");
                x++;
            } else if (x == boardSize - 1 && y != boardSize - 1) {
                x = 0;
                y++;
                boardDrawing.append(field)
                        .append("\n");
                        //.append(String.join(" ", Collections.nCopies(boardSize, ".")))
                        //.append("\n");
            } else if (x == boardSize - 1 && y == boardSize - 1) {
                boardDrawing.append(field);
            } else {
                System.out.println("Drawing loop not working as expected.");
            }
        }
        return boardDrawing.toString();
    }
}
