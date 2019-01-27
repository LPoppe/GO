package main.Logic;

import javafx.util.Pair;

import java.util.*;

public class Board {

    private Integer boardSize;
    private String boardState;
    //tiles on the current board
    private Integer[] currentBoard;
    private HashSet<Group> tileGroupSet = new HashSet<>();

    //contains the index per location on the board
    private Integer[] boardFields;
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

    public enum TileColor {
        empty(0), black(1), white(2);
        private final int tileNumber;
        TileColor(int i) {
            this.tileNumber = i;
        }
    }

    public Board(Integer preferredSize) {
        this.boardSize = preferredSize;
        initializeBoard();
    }

    Board(Board board) {
        this.boardSize = board.getBoardSize();
        this.boardState = board.getBoardState();
        this.currentBoard = board.currentBoard.clone();
        this.boardFields = board.getBoardFields().clone();
        this.boardHistory = new ArrayList<>(board.getBoardHistory());
        this.tileGroupSet = new HashSet<>();

        for (Group group : board.getAllGroups()) {
            this.tileGroupSet.add(group.myClone());
        }
    }

    public String getBoardState() {
        return this.boardState;
    }
    public Integer getBoardSize() {
        return this.boardSize;
    }
    public Integer[] getCurrentBoard() {
        return this.currentBoard;
    }
    public Integer[] getBoardFields() {
        return this.boardFields;
    }

    //Getters for the group mappings.
    public Set<Group> getAllGroups() {
        return this.tileGroupSet;
    }

    /**Returns the x and y coordinate given a tile index.*/
    public Pair<Integer, Integer> getTileCoordinates(int tileIndex) {
        Integer x = tileIndex % boardSize;
        Integer y = tileIndex / boardSize;
        return new Pair<>(x, y);
    }

    /**Returns the tile index given an x and y coordinate.*/
    public Integer getTileIndex(int xCoordinate, int yCoordinate) {
        return xCoordinate + (yCoordinate * boardSize);
    }
    /**Returns the tile content at a certain location on the board.*/
    public Integer getTileContent(int x, int y) {
        return this.currentBoard[getTileIndex(x, y)];
    }
    public Integer getTileContent(int tileIndex) {
        return this.currentBoard[tileIndex];
    }

    /**Creates an array of indexes for all tile location and an initial empty board.
     */
    private void initializeBoard() {
        boardFields = new Integer[boardSize * boardSize];
        Arrays.setAll(boardFields, index -> index);
        currentBoard = new Integer[boardSize * boardSize];
        Arrays.fill(currentBoard, TileColor.empty.tileNumber);
        updateBoardState();
    }

    /**Updates the board with a move.
     * @param playerColorNumber the tile color to be added. Requires the playerColor to be 1 or 2.
     * @param playerMove the location of the new tile.
     */
    public void setBoardState(int playerColorNumber, int playerMove) {
        if (playerColorNumber == TileColor.black.tileNumber || playerColorNumber == TileColor.white.tileNumber) {
            updateBoard(playerColorNumber, playerMove);
        } else {
            System.out.println("Player does not match known tile color.");
        }
    }

    private void updateBoard(int tileColor, int tileIndex) {
        if (tileColor == TileColor.black.tileNumber) {
            currentBoard[tileIndex] = tileColor;
            addToGroup(TileColor.black, tileIndex);
        } else {
            currentBoard[tileIndex] = tileColor;
            addToGroup(TileColor.white, tileIndex);
        }
        List<Integer> deadTiles = updateGroupFreedoms();
        for (Integer tile : deadTiles) {
            currentBoard[tile] = 0;
        }
        updateBoardState();
    }

    /**Updates the String boardState by replacing it with a new string built from currentBoard.
     */
    private void updateBoardState() {
        StringBuilder boardBuilder = new StringBuilder();
        for (int tile : currentBoard) {
            boardBuilder.append(tile);
        }
        this.boardState = boardBuilder.toString();
        addToHistory(boardState);
    }

    private void addToGroup(TileColor tile, int tileIndex) {
        List<Integer> tileNeighbors = Group.getNeighborTiles(tileIndex, this);
        int groupsFound = 0;
        for (Integer neighbor: tileNeighbors) {
            for (Group group : tileGroupSet) {
                if (group.getGroupTiles().contains(neighbor) && group.getTileColor() == tile) {
                    groupsFound++;
                    group.addTileToGroup(tileIndex);
                }
            }
        }

        if (groupsFound == 0) {
            tileGroupSet.add(new Group(tile, tileIndex, this));
        } else if (groupsFound > 1) {
            mergeGroups(tile, tileIndex);
        }
    }

    /**Creates a new group containing the tiles of all groups containing a certain tile index.
     * Removes the old groups from the board's set of groups.
     * @param tile the tile color associated with the tile index.
     * @param tileIndex the tile index being added.
     */
    private void mergeGroups(TileColor tile, Integer tileIndex) {
        Group mergedGroup = new Group(tile, tileIndex, this);
        List<Group> oldGroups = new ArrayList<>();
        for (Group group : tileGroupSet) {
            if (group.getGroupTiles().contains(tileIndex)) {
                oldGroups.add(group);
                mergedGroup.getGroupTiles().addAll(group.getGroupTiles());
            }
        }
        tileGroupSet.removeAll(oldGroups);
        mergedGroup.determineFreedoms();
        tileGroupSet.add(mergedGroup);
    }

    /**Determines the group freedoms of all groups on the board.
     * If any group has 0 freedoms left, the tiles are added to a list of dead tiles.
     * The dead groups are removed from the board's set of groups.
     * @return a list of dead tiles.
     */
    private List<Integer> updateGroupFreedoms() {
        List<Integer> deadTiles = new ArrayList<>();
        List<Group> deadGroups = new ArrayList<>();

        for (Group group : tileGroupSet) {
            group.determineFreedoms();
            if (group.groupHasNoFreedoms()) {
                deadGroups.add(group);
                deadTiles.addAll(group.getGroupTiles());
            }
        }
        tileGroupSet.removeAll(deadGroups);
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
