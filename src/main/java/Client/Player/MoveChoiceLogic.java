package Client.Player;

import Logic.Board;
import Logic.GoGame;
import Logic.Group;
import Logic.ValidityChecker;

import java.util.ArrayList;
import java.util.List;

public class MoveChoiceLogic {

    public static List<Integer> checkValidity(Board gameBoard, ValidityChecker checker, GoGame.PlayerColor myPlayerColor) {
        List<Integer> validOptions = new ArrayList<>();
        for (int option : gameBoard.getBoardFields()) {
            String moveValidity = checker.checkMove(myPlayerColor.getPlayerColorNumber(), option, gameBoard);
            if (moveValidity.equals("VALID")) {
                validOptions.add(option);
            }
        }
        return validOptions;
    }

    public static List<Integer> preventOwnCapture(Board gameBoard, List<Integer> validOptions, GoGame.PlayerColor myPlayerColor) {
        List<Integer> filteredList = new ArrayList<>(validOptions);
        for (Group group : gameBoard.getAllGroups()) {
            if (myPlayerColor.getPlayerColorNumber() == group.getTileColor().getTileColorNumber()) {
                group.determineFreedoms();

                //Don't cause the removal of your own group.
                if (group.getFreedomTiles().size() == 1) {
                    Integer tile = group.getFreedomTiles().iterator().next();
                    filteredList.remove(tile);
                } else {
                    for (Integer tile : group.getFreedomTiles()) {
                        List<Integer> freedomNeigbors = Group.getFullNeighborTiles(tile, gameBoard);
                        boolean foundJustMe = true;
                        for (Integer freedomNeigbor : freedomNeigbors) {
                            if (gameBoard.getTileContent(freedomNeigbor) != myPlayerColor.getPlayerColorNumber()) {
                                foundJustMe = false;
                            }
                        }
                        if (foundJustMe) {
                            filteredList.remove(tile);
                        }
                    }
                }
            }
        }
        return filteredList;
    }

    public static Integer checkForCapture(Board gameBoard, List<Integer> validOptions, GoGame.PlayerColor myPlayerColor) {
        Integer bestCaptureOption = null;
        int bestOptionSize = 0;
        for (Group group : gameBoard.getAllGroups()) {
            if (myPlayerColor.getPlayerColorNumber() != group.getTileColor().getTileColorNumber()) {
                group.determineFreedoms();
                if (group.getFreedomTiles().size() == 1 && group.getGroupSize() > bestOptionSize) {
                    Integer tile = group.getFreedomTiles().iterator().next();
                    if (validOptions.contains(tile)) {
                        bestCaptureOption = group.getFreedomTiles().iterator().next();
                        bestOptionSize = group.getGroupSize();
                    }
                }
            }
        }
        return bestCaptureOption;
    }

    public static Integer findWeakestLink(Board gameBoard, List<Integer> validOptions, GoGame.PlayerColor myPlayerColor) {
        //The final tile to send (or not).
        Integer weakestLinkBestNeighbor = null;
        //First determine the smallest opponent group
        Group largestGroup = null;
        for (Group group : gameBoard.getAllGroups()) {


            if (group.getTileColor().getTileColorNumber() != myPlayerColor.getPlayerColorNumber()) {
                if (largestGroup == null) {
                    largestGroup = group;
                } else if (group.getGroupSize() > largestGroup.getGroupSize()) {
                    largestGroup = group;
                }
            }
        }
        if (largestGroup != null) {

            Integer weakestLink = null;
            int weakestLinkFreedoms = 0;
            for (Integer tile : largestGroup.getGroupTiles()) {
                if (weakestLink == null) {
                    weakestLink = tile;
                } else {
                    int tileFreedoms = 0;
                    List<Integer> neighbors = Group.getNeighborTiles(tile, gameBoard);
                    for (Integer neighbor : neighbors) {
                        if (gameBoard.getTileContent(neighbor) == Board.TileColor.empty.getTileColorNumber()) {
                            tileFreedoms++;
                        }
                    }
                    if (tileFreedoms >= weakestLinkFreedoms) {
                        weakestLink = tile;
                        weakestLinkFreedoms = tileFreedoms;
                    }
                }
            }
            int nearbyFriendliesBest = 0;
            List<Integer> weakestLinkNeighbors = Group.getNeighborTiles(weakestLink, gameBoard);
            //Finds the tile with the most neighbors in the two surrounding rings of tiles.
            for (Integer neighbor : weakestLinkNeighbors) {
                if (validOptions.contains(neighbor)) {
                    if (weakestLinkBestNeighbor == null && validOptions.contains(neighbor)) {
                        weakestLinkBestNeighbor = neighbor;
                    } else {
                        int nearbyFriendlies = 0;
                        if (gameBoard.getTileContent(neighbor) == myPlayerColor.getPlayerColorNumber()) {
                            nearbyFriendlies++;
                        }
                        if (nearbyFriendlies > nearbyFriendliesBest && validOptions.contains(neighbor)) {
                            weakestLinkBestNeighbor = neighbor;
                        }
                    }
                }
            }
        }
        return weakestLinkBestNeighbor;
    }
}
