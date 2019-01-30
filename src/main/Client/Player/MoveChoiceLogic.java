package main.Client.Player;

import javafx.util.Pair;
import main.Logic.Board;
import main.Logic.GoGame;
import main.Logic.Group;
import main.Logic.ValidityChecker;

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
                group.checkIfAlive();
                if (group.getGroupStatus() == Group.Status.alive) {
                    for (Integer freedom : group.getFreedomTiles()) {
                        List<Integer> freedomNeigbors = Group.getFullNeighborTiles(freedom, gameBoard);
                        boolean foundJustMe = true;
                        for (Integer freedomNeigbor : freedomNeigbors) {
                            if (gameBoard.getTileContent(freedomNeigbor) != myPlayerColor.getPlayerColorNumber()) {
                                foundJustMe = false;
                            }
                        }
                        if (foundJustMe) {
                            filteredList.remove(freedom);
                        }
                    }
                }

                //Don't cause the removal of your own group.
                if (group.getFreedomTiles().size() == 1) {
                    Integer tile = group.getFreedomTiles().iterator().next();
                    filteredList.remove(tile);
                } else {
                    for (Integer tile : group.getFreedomTiles()) {
                        List<Integer> neighbors = Group.getNeighborTiles(tile, gameBoard);
                        int neighborsMine = 0;
                        for (Integer neighbor : neighbors) {
                            if (gameBoard.getTileContent(neighbor) == myPlayerColor.getPlayerColorNumber()) {
                               neighborsMine++;
                            }
                        }
                        if (neighborsMine == 4) {
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
            //If a group can be prevented from gaining an eye in the next turn, choose this.
            if (myPlayerColor == GoGame.PlayerColor.black) {
                Integer potentialStability = stabilizeGroup(gameBoard, validOptions, GoGame.PlayerColor.white);
                if (potentialStability != null) {
                    return potentialStability;
                }
            } else {
                Integer potentialStability = stabilizeGroup(gameBoard, validOptions, GoGame.PlayerColor.black);
                if (potentialStability != null) {
                    return potentialStability;
                }
            }

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

    //TODO Way too slow.
    public static Integer gainTerritory(Board gameBoard, List<Integer> validOptions, GoGame.PlayerColor myPlayerColor) {
        Board boardCopy = new Board(gameBoard);
        Integer bestScoringMove = null;
        Double bestScore = null;
        for (Integer option : validOptions) {
            boardCopy.setBoardState(myPlayerColor.getPlayerColorNumber(), option);
            Pair<Double, Double> scores = GoGame.calculateScores(boardCopy);
            if (bestScore == null) {
                if (myPlayerColor == GoGame.PlayerColor.black) {
                    bestScoringMove = option;
                    bestScore = scores.getKey() - scores.getValue();
                } else {
                    bestScoringMove = option;
                    bestScore = scores.getValue() - scores.getValue();
                }
            } else if (myPlayerColor == GoGame.PlayerColor.black) {
                if (scores.getKey() - scores.getValue() >= bestScore) {
                    bestScoringMove = option;
                    bestScore = scores.getKey();
                }
            } else {
                if (scores.getValue() - scores.getKey() >= bestScore) {
                    bestScoringMove = option;
                    bestScore = scores.getValue();
                }
            }
        }
        return bestScoringMove;
    }

    /**Checks if any groups can gain an eye in the next move.*/
    public static Integer stabilizeGroup(Board gameBoard, List<Integer> validOptions, GoGame.PlayerColor aPlayerColor) {
        Integer stabilityMove = null;
        Group stabilizedGroup = null;
        for (Group group : gameBoard.getAllGroups()) {
            if (group.getTileColor().getTileColorNumber() == aPlayerColor.getPlayerColorNumber()) {
                for (Integer tile : group.getGroupTiles()) {
                    List<Integer> neighbors = Group.getFullNeighborTiles(tile, gameBoard);
                    for (Integer neighbor : neighbors) {
                        if (validOptions.contains(neighbor)) {
                            Board boardCopy = new Board(gameBoard);
                            boardCopy.setBoardState(aPlayerColor.getPlayerColorNumber(), neighbor);
                            for (Group copyGroup : boardCopy.getAllGroups()) {
                                copyGroup.checkIfAlive();
                                if (copyGroup.getGroupStatus() == Group.Status.alive) {
                                    if (stabilizedGroup == null) {
                                        stabilizedGroup = group;
                                        stabilityMove = neighbor;
                                    } else if (group.getGroupSize() > stabilizedGroup.getGroupSize()) {
                                        stabilityMove = neighbor;
                                        stabilizedGroup = group;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return stabilityMove;
    }

    public static Integer predictOpponentCapture(Board gameBoard, List<Integer> validOptions, GoGame.PlayerColor aPlayerColor) {
        Integer capturePreventionTile = null;
        for (Group group : gameBoard.getAllGroups()) {
            if (group.getTileColor().getTileColorNumber() == aPlayerColor.getPlayerColorNumber() && group.getFreedomTiles().size() == 2) {
                Board boardCopy = new Board(gameBoard);
            }
        }
        return capturePreventionTile;
    }

    public static Integer compareMoves(Integer moveOne, Integer moveTwo) {
        return null;
    }
}
