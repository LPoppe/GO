package main.Logic;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Group {

    //A group is alive when it can no longer be removed from the board.
    // A group is dead when it can no longer be saved.
    private enum Status { unsettled, alive, dead }
    private Status groupStatus;
    private Board gameBoard;
    private Board.TileColor tileColor;

    private Set<Integer> freedomTiles = new HashSet<>();
    private Set<Integer> groupTiles = new HashSet<>();

    /**Creates a clone of this group.
     * @return a clone of this group.
     */
    public Group myClone(Board board) {
        return new Group(this.groupStatus, board, this.tileColor, this.freedomTiles, this.groupTiles);
    }

    /**The constructor used in the normal flow of the game.
     * @param tile the tile color of the group of stones.
     * @param firstTileIndex the first tile the group is based around.
     * @param board the board the group is on.
     */

    public Group(Board.TileColor tile, int firstTileIndex, Board board) {
        groupTiles.add(firstTileIndex);
        this.groupStatus = Status.unsettled;
        this.gameBoard = board;
        this.tileColor = tile;
        determineFreedoms();
    }

    /**Used to enable deep copying the board.
     */
    private Group(Status status, Board gameBoard, Board.TileColor tileColor,
                  Set<Integer> freedomTiles, Set<Integer> groupTiles) {
        this.groupStatus = status;
        this.gameBoard = gameBoard;
        this.tileColor = tileColor;
        this.freedomTiles = new HashSet<>(freedomTiles);
        this.groupTiles = new HashSet<>(groupTiles);
    }

    public Board getGameBoard() {
        return this.gameBoard;
    }

    /**Calculates the amount of empty tiles surrounding the group.
     * If this reaches 0, the group has been captured and will be removed from the board.
     */
    public void determineFreedoms() {
        for (Integer tile : groupTiles) {
            for (Integer neighbor : getNeighborTiles(tile, gameBoard)) {
                if (gameBoard.getTileContent(neighbor) == 0) {
                    freedomTiles.add(neighbor);
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
    public static List<Integer> getNeighborTiles(int tileIndex, Board board) {
        Pair<Integer, Integer> tileCoordinates = board.getTileCoordinates(tileIndex);
        List<Integer> tileNeighbors = new ArrayList<>();
        int xCoordinate = tileCoordinates.getKey();
        int yCoordinate = tileCoordinates.getValue();

        if (xCoordinate > 0) {
            tileNeighbors.add(board.getTileIndex(xCoordinate - 1, yCoordinate));
        }
        if (xCoordinate < board.getBoardSize() - 1) {
            tileNeighbors.add(board.getTileIndex(xCoordinate + 1, yCoordinate));
        }
        if (yCoordinate > 0) {
            tileNeighbors.add(board.getTileIndex(xCoordinate, yCoordinate - 1));
        }
        if (yCoordinate < board.getBoardSize() - 1) {
            tileNeighbors.add(board.getTileIndex(xCoordinate, yCoordinate + 1));
        }

        return tileNeighbors;
    }

    public int getGroupSize() {
        return groupTiles.size();
    }

    Set<Integer> getGroupTiles() {
        return this.groupTiles;
    }

    Set<Integer> getFreedomTiles() {
        return this.freedomTiles;
    }

    public boolean groupHasNoFreedoms() {
        return freedomTiles.isEmpty();
    }

    public Board.TileColor getTileColor() {
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
