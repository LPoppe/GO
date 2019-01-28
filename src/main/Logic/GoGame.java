package main.Logic;

import javafx.util.Pair;
import main.Client.Player.Player;
import main.Server.ClientHandler;

import java.util.*;

/**
 * Keeps track of the turns in an ongoing GO game. Used by the server's GameHandler and the client.
 */
public class GoGame {

    private Board goBoard;

    //if Black: 1, if White: 2
    public enum PlayerColor {
        black(1), white (2);
        final int playerColorNumber;
        PlayerColor(int i) {
            this.playerColorNumber = i;
        }
        public int getPlayerColorNumber() {
            return playerColorNumber;
        }
    }

    private PlayerColor currentPlayerColor;
    private ClientHandler blackClient;
    private ClientHandler whiteClient;

    public Integer turnTimer;

    public GoGame(ClientHandler player1, PlayerColor color, Integer boardSize) {
        this.goBoard = new Board(boardSize);
        setPlayerOneColor(player1, color);
        this.currentPlayerColor = PlayerColor.black;
        this.turnTimer = 0;
    }

    private void setPlayerOneColor(ClientHandler playerOne, PlayerColor playerOneColor) {
        if (playerOneColor == PlayerColor.black) {
            this.blackClient = playerOne;
        } else {
            this.whiteClient = playerOne;
        }
    }

    //Must be called after setPlayerOneColor().
    public void setPlayerTwo(ClientHandler playerTwo) {
        if (this.blackClient != null) {
            this.whiteClient = playerTwo;
        } else {
            this.blackClient = playerTwo;
        }
    }

    public int getCurrentPlayerColorNumber() {
        return currentPlayerColor.playerColorNumber;
    }

    public ClientHandler getCurrentClient() {
        return getClientByColor(currentPlayerColor);
    }

    public String getBoardState() {
        return goBoard.getBoardState();
    }

    public Integer getBoardSize() {
        return goBoard.getBoardSize();
    }

    public Board getBoard() {
        return this.goBoard;
    }

    //Requires player to exist in the map.
    //Can only be called after setPlayerOneColor() and setPlayerTwoColor().
    public PlayerColor getColorByClient(ClientHandler player) {
        return player == this.blackClient ? PlayerColor.black : PlayerColor.white;
    }

    public ClientHandler getClientByColor(PlayerColor color) {
        return color == PlayerColor.black ? this.blackClient : this.whiteClient;
    }

    public List<String> getBoardHistory() {
        return goBoard.getBoardHistory();
    }

    public void changePlayer() {
        if (currentPlayerColor == PlayerColor.black) {
            currentPlayerColor = PlayerColor.white;
        } else {
            currentPlayerColor = PlayerColor.black;
        }
    }

    /**
     * Determine new board state after player has placed a tile on the current board.
     * @param messagingPlayer should be the current player.
     * @param playerMove the tile index and player color associated with a player's move.
     * @return the new boardString to be sent to the players.
     */
    public void changeBoardState(ClientHandler messagingPlayer, int playerMove) {
        if (getColorByClient(messagingPlayer) == currentPlayerColor && playerMove != -1) {
            goBoard.setBoardState(getColorByClient(messagingPlayer).playerColorNumber, playerMove);
            changePlayer();
        } else if (getColorByClient(messagingPlayer) == currentPlayerColor && playerMove == -1) {
            changePlayer();
        } else {
            System.out.println("Wrong player. This should not be reached.");
        }
    }

    /**
     * Calculates a player's score based on a certain board.
     * Static to allow an AI to use it outside of the flow of the actual game.
     * Should not be called before the game is over.
     * @param board the board to be used.
     * @return a pair containing the scores of the black and white players
     */
    //TODO Calculate scores not implemented yet. Don't forget Group.getNeighbors!
    private static Pair<Double, Double> calculateScores(Board board) {
        //Add the empty tiles to the groups of the game board.
        board.determineEmptyGroups();
        Pair<Double, Double> playerScores;
        Double blackScore = 0.0;
        Double whiteScore = 0.0;
        for (Group group : board.getAllGroups()) {
            if (group.getTileColor().getTileColorNumber() == PlayerColor.black.getPlayerColorNumber()) {
                blackScore = blackScore + group.getGroupTiles().size();
            }
            if (group.getTileColor().getTileColorNumber() == PlayerColor.white.getPlayerColorNumber()) {
                whiteScore = whiteScore + group.getGroupTiles().size();
            }
            if (group.getTileColor() == Board.TileColor.empty) {
                boolean whiteFound = false;
                boolean blackFound = false;
                for (int tile : group.getGroupTiles()) {
                    List<Integer> neighbors = Group.getNeighborTiles(tile, board);
                    for (Integer neighbor : neighbors) {
                        if (board.getTileContent(neighbor) == Board.TileColor.black.getTileColorNumber()) {
                            blackFound = true;
                        } else if (board.getTileContent(neighbor) == Board.TileColor.white.getTileColorNumber()) {
                            whiteFound = true;
                        }
                    }
                }
                if (blackFound && !whiteFound) {
                    System.out.println("Empty tile size: " + group.getGroupTiles().size());
                    blackScore = blackScore + group.getGroupTiles().size();
                } else if (whiteFound && !blackFound) {
                    System.out.println("Empty tile size: " + group.getGroupTiles().size());
                    whiteScore = whiteScore + group.getGroupTiles().size();
                }
            }
        }
        whiteScore = whiteScore + 0.5;
        playerScores = new Pair<>(blackScore, whiteScore);
        return playerScores;
    }

    /**Called when the game is over.
     * (e.g. disconnect, or both players have passed after one another).
     */
    public static Pair<PlayerColor, Pair<Double, Double>> determineWinner(Board board) {
        Pair<Double, Double> scores = calculateScores(board);
        if (scores.getKey().compareTo(scores.getValue()) > 0) {
            return new Pair<>(PlayerColor.black, scores);
        } else {
            return new Pair<>(PlayerColor.white, scores);
        }
    }
}
