package main.Logic;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Group implements Cloneable {

    public Group myClone() {
        return new Group(this.groupStatus, this.gameBoard, this.tileColor, this.freedomTiles, this.groupTiles);
    }

    //A group is alive when it can no longer be removed from the board.
    // A group is dead when it can no longer be saved.
    private enum Status { unsettled, alive, dead }
    private Status groupStatus;
    private Board gameBoard;
    private Board.TileColor tileColor;

    private Set<Integer> freedomTiles = new HashSet<>();
    private Set<Integer> groupTiles = new HashSet<>();

    private Group(Status status, Board gameBoard, Board.TileColor tileColor, Set<Integer> freedomTiles, Set<Integer> groupTiles){
        this.groupStatus = status;
        this.gameBoard = gameBoard;
        this.tileColor = tileColor;
        this.freedomTiles = new HashSet<>(freedomTiles);
        this.groupTiles = new HashSet<>(groupTiles);
    }

    public Group(Board.TileColor tile, int firstTileIndex, Board board) {
        groupTiles.add(firstTileIndex);
        this.groupStatus = Status.unsettled;
        this.gameBoard = board;
        this.tileColor = tile;
        determineFreedoms();
    }

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
    static List<Integer> getNeighborTiles(int tileIndex, Board board) {
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
