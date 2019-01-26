package main.Logic;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Group {

    //A group is alive when it can no longer be removed from the board.
    // A group is dead when it can no longer be saved.
    private enum Status { unsettled, alive, dead }
    private Status groupStatus;
    private Board gameBoard;
    private Board.Tiles tileColor;

    private List<Integer> freedomTiles = new ArrayList<>();
    private List<Integer> groupTiles = new ArrayList<>();

    public Group(Board.Tiles tile, int firstTileIndex, Board board) {
        groupTiles.add(firstTileIndex);
        this.groupStatus = Status.unsettled;
        this.gameBoard = board;
        this.tileColor = tile;
        determineFreedoms();
    }

    public void determineFreedoms() {
        for (int tile : groupTiles) {
            for (Integer neighbor : getNeighborTiles(tile, gameBoard)) {
                if (gameBoard.getTileContent(neighbor) == 0) {
                    if (!freedomTiles.contains(neighbor)) {
                        freedomTiles.add(neighbor);
                    }
                } else if (gameBoard.getTileContent(neighbor) != 0) {
                    freedomTiles.remove(neighbor);
                }
            }
        }
    }

    /**Creates a list of the tiles surrounding a certain tile on the board.
     * @param tileIndex the location of the tile in the 1D array
     * @param board the board the tiles are on.
     * @return a list of the neighboring tile indices.
     */
    //TODO add option to search at greater distance to determine a good move position.
    static List<Integer> getNeighborTiles(int tileIndex, Board board) {
        Pair<Integer, Integer> tileCoordinates = board.getTileCoordinates(tileIndex);
        List<Integer> tileNeighbors = new ArrayList<>();
        int xCoordinate = tileCoordinates.getKey();
        int yCoordinate = tileCoordinates.getValue();

        for (int colNum = yCoordinate - 1; colNum <= (yCoordinate + 1); colNum += 1) {
            for (int rowNum = xCoordinate - 1; rowNum <= (xCoordinate + 1); rowNum += 1) {
                if (!((colNum == yCoordinate) && (rowNum == xCoordinate))) {
                    //Check if coordinate is in board, and if it is directly connected.
                    if (isWithinBoard(colNum, rowNum, board) &&
                            (colNum == yCoordinate || rowNum == xCoordinate)) {
                        Integer neighborTile = board.getTileIndex(rowNum, colNum);
                        tileNeighbors.add(neighborTile);
                    }
                }
            }
        }
        return tileNeighbors;
    }

    private static boolean isWithinBoard(int row, int col, Board board) {
        if (row < 0 || col < 0) {
            return false;
        } else {
            return row < board.getBoardSize() && col < board.getBoardSize();
        }
    }

    public int getGroupSize() {
        return groupTiles.size();
    }

    public List<Integer> getTiles() {
        return this.groupTiles;
    }

    public int getNumberOfFreedoms() {
        return freedomTiles.size();
    }

    public Board.Tiles getTileColor() {
        return tileColor;
    }

    public void addTileToGroup(Integer tileIndex) {
        freedomTiles.remove(tileIndex);
        groupTiles.add(tileIndex);
    }

    //TODO Check if a group can no longer be killed.
    public void checkIfAlive() {
        //if so:
        //this.groupStatus = Status.alive;
    }

    //TODO Check if a group can no longer be saved.
    public void checkIfDead() {
        //if so:
        //this.groupStatus = Status.dead;
    }

}
