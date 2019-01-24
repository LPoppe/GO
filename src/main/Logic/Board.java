package main.Logic;

import javafx.util.Pair;

import java.util.*;

public class Board {

    private Integer boardSize;
    private String boardState;
    //tiles on the current board
    private Integer[] currentBoard;
    private Map<Integer, Group> tileGroups = new HashMap<>();

    //contains the index per location on the board
    private static Integer[] boardFields;
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

    public Board(Integer preferredSize) {
        this.boardSize = preferredSize;
        this.boardState = String.join("", Collections.nCopies(boardSize * boardSize, "0"));
        initializeBoard();
    }

    Board(Board board) {
        this.boardSize = board.getBoardSize();
        this.boardState = board.getBoardState();
        this.currentBoard = board.currentBoard.clone();
        this.boardHistory = new ArrayList<>(board.getBoardHistory());
    }

    public String getBoardState() {
        return this.boardState;
    }
    public Integer getBoardSize() {
        return this.boardSize;
    }

    //Getters for the group mappings.
    public Map<Integer, Group> getAllGroups() {
        return this.tileGroups;
    }

    /**Returns the x and y coordinate given a tile index.*/
    public Pair<Integer, Integer> getTileCoordinates(int tileIndex) {
        Integer x = tileIndex % boardSize;
        Integer y = tileIndex / boardSize;
        return new Pair<>(x, y);
    }

    /**Returns the tile index given an x and y coordinate.*/
    public Integer getTileIndex(int xCoordinate, int yCoordinate) {
        return boardSize * xCoordinate + yCoordinate;
    }
    /**Returns the tile content at a certain location on the board.*/
    public Integer getTileContent(int x, int y) {
        return this.currentBoard[x + y * boardSize];
    }
    public Integer getTileContent(int tileIndex) {
        return this.currentBoard[tileIndex * boardSize];
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
            System.out.println("Player does not match known tile color.");
        }
    }

    private void updateBoard(int tileColor, int tileIndex) {
        if (tileColor == Tiles.black.tileNumber) {
            currentBoard[tileIndex] = tileColor;
            addToGroup(Tiles.black, tileIndex);
        } else {
            currentBoard[tileIndex] = tileColor;
            addToGroup(Tiles.white, tileIndex);
        }
        List<Integer> deadTiles = updateGroupFreedoms();
        for (Integer tile : deadTiles) {
            currentBoard[tile] = 0;
        }
        this.boardState = Arrays.toString(currentBoard);
    }

    private void addToGroup(Tiles tile, int tileIndex) {
        List<Integer> tileNeighbors = Group.getNeighborTiles(tileIndex, this);
        boolean groupFound = false;
        for (Integer neighbor: tileNeighbors) {
            if (tileGroups.keySet().contains(neighbor) && tile == tileGroups.get(neighbor).getTileColor()) {
                tileGroups.put(tileIndex, tileGroups.get(neighbor));
                tileGroups.get(neighbor).addTileToGroup(tileIndex);
                groupFound = true;
            }
        }
        if (!groupFound) {
            tileGroups.put(tileIndex, new Group(tile, tileIndex, this));
        }
    }

    private List<Integer> updateGroupFreedoms() {
        List<Integer> deadTiles = new ArrayList<>();
        for (Group group : tileGroups.values()) {
            group.determineFreedoms();
            if (group.getNumberOfFreedoms() == 0) {
                deadTiles.addAll(group.getTiles());
                //Removes all items containing the group.
                tileGroups.values().removeAll(Collections.singleton(group));
            }
        }
        return deadTiles;
    }

    /**Draws a string representation of the current board state, which may be used in the TUI.
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
