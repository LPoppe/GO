package main.Client;

import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import main.Client.Player.BasicPlayer;
import main.Client.Player.HumanPlayer;
import main.Client.Player.Player;
import main.Client.View.NedapGUI.GoGuiIntegrator;
import main.Logic.Board;
import main.Logic.GoGame;

import java.util.Arrays;

public class GoController {

    private GoClient gameClient;
    private GoGuiIntegrator goGui;

    //Information about the game
    private Integer gameID;
    private String opponentPlayerName;
    private Board gameBoard;
    private boolean isClientsTurn;
    //Information about the player
    private Player player;
    private String thisPlayerName;
    private GoGame.PlayerColor thisPlayerColor;
    private GoGame.PlayerColor opponentPlayerColor;

    public static void main(String[] args) {
        GoController controller = new GoController();
    }


    public GoController() {
        this.gameClient = new GoClient(this);
    }

     //* Initializes the correct player object (AI or human, depending on the user input).*/
    private void initPlayer(String choice) {
        if (choice.equals("H")) {
            this.player = new HumanPlayer(this, this.gameBoard, thisPlayerColor);
        } else if (choice.equals("A")) {
            this.player = new BasicPlayer(this, this.gameBoard, thisPlayerColor);
        }
        if (isClientsTurn) {
            player.notifyTurn();
        }
    }

    /**Starts the game GUI. Called after connecting to the server using the TUI
     * and configuring the game.
     */
    private void startGUI(int boardSize) {
        this.goGui = new GoGuiIntegrator(true, true, boardSize);
        goGui.startGUI();
        goGui.setBoardSize(boardSize);

        goGui.getPrimaryStage().addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            int x = (int) (Math.round((mouseEvent.getSceneX() / goGui.getInitialSquareSize()) - 1));
            int y = (int) (Math.round((mouseEvent.getSceneY() / goGui.getInitialSquareSize()) - 1));
            System.out.println("X = " + mouseEvent.getSceneX() + ", Y = " + mouseEvent.getSceneY());
            System.out.println("int X = " + x + ", int Y = " + y);
//                goGui.addStone(x, y, true);
            player.userTileClicked(x, y);
        });
    }

    private void updateControlBoard(int playerColorNumber, int tileIndex) {
        gameBoard.setBoardState(playerColorNumber, tileIndex);
        updateGUI();
    }

    /**Fully refreshes the content of the GUI board.
     */
    private void updateGUI() {
        for (int tile : gameBoard.getBoardFields()) {
            Pair<Integer, Integer> tileCoordinates = gameBoard.getTileCoordinates(tile);
            int xCoordinate = tileCoordinates.getKey();
            int yCoordinate = tileCoordinates.getValue();
            int tileColor = gameBoard.getTileContent(tile);
            if (tileColor == 0) {
                goGui.removeStone(xCoordinate, yCoordinate);
            } else if (tileColor == 1) {
                goGui.addStone(xCoordinate, yCoordinate, false);
            } else if (tileColor == 2) {
                goGui.addStone(xCoordinate, yCoordinate, true);
            } else {
                throw new RuntimeException("In updateGUI: Tile color does not exist.");
            }
        }
    }

    /**Receives data about the board state sent by the server and updates the board.
     * Enables a move by the user if it is the player's turn.
     * @param currentPlayerColorNumber The number corresponding to the player's tile color
     * @param move the move to be added to the board, containing the tile's color (int)
     *             and a tile index for the new tile.
     * @param newBoard used to check if the boardHistory addition matches the server's.
     */
    void updateTurnFromServer(int currentPlayerColorNumber, String move, String newBoard, String nGroups) {
        String[] moveDetails = move.split(";");
        int playerColorNumber  = Integer.valueOf(moveDetails[1]);
        int tileIndex = Integer.valueOf(moveDetails[0]);
        if (tileIndex != -1) {
            updateControlBoard(playerColorNumber, tileIndex);
        }

        //TODO remove move made print
        System.out.println("Move received: " + Arrays.toString(moveDetails));
        if (gameBoard.getAllGroups().size() != Integer.parseInt(nGroups)) {
            throw new RuntimeException("Number of groups does not match:\n" + "Server: " + nGroups + "\n" +
                    "Client: " + gameBoard.getAllGroups().size());
        }
        System.out.println("N groups: " + gameBoard.getAllGroups().size());
        if (!gameBoard.getBoardHistory().get(gameBoard.getBoardHistory().size() - 1).equals(newBoard)) {
            throw new RuntimeException("Game board does not match received string after update:\n"
            + newBoard + "\n" + gameBoard.getBoardHistory().get(gameBoard.getBoardHistory().size() - 1));
        }

        isClientsTurn = currentPlayerColorNumber == thisPlayerColor.getPlayerColorNumber();
        if (isClientsTurn) {
            player.notifyTurn();
        }
    }

    /**
     * Initialises a game board.
     * @param boardSize the size of the board.
     */
    void setGame(Integer boardSize, String opponent, int colorNumber) {
        setBoard(boardSize);
        setOpponent(opponent);
        setPlayerColor(colorNumber);
        String typeChoice = gameClient.askForPlayerType();
        initPlayer(typeChoice);
        startGUI(boardSize);
    }
    private void setBoard(Integer boardSize) {
        this.gameBoard = new Board(boardSize);
        System.out.println("Boardsize set to: " + boardSize);
    }

    private void setOpponent(String opponent) {
        this.opponentPlayerName = opponent;
        System.out.println("Your opponent's name is: " + opponentPlayerName);
    }

    private void setPlayerColor(int colorNumber) {
        if (colorNumber == 1) {
            this.thisPlayerColor = GoGame.PlayerColor.black;
            this.opponentPlayerColor = GoGame.PlayerColor.white;
            isClientsTurn = true;
            System.out.println("You start as black.");
        } else if (colorNumber == 2) {
            this.thisPlayerColor = GoGame.PlayerColor.white;
            this.opponentPlayerColor = GoGame.PlayerColor.black;
            isClientsTurn = false;
            System.out.println("You start as white.");
        } else {
            System.out.println("Impossible tile color assigned to player.");
        }
    }

    public void sendMoveToClient(int tileIndex) {
        gameClient.sendMove(tileIndex);
    }

    void retryMove() {
        player.notifyTurn();
    }
}
