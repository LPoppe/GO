package main.Client;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import main.Client.Player.BasicPlayer;
import main.Client.Player.HumanPlayer;
import main.Client.Player.Player;
import main.Client.View.NedapGUI.GoGuiIntegrator;
import main.Logic.Board;
import main.Logic.GoGame;

import java.util.Arrays;
import java.util.List;

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

    private void updateBoard(int playerColorNumber, int tileIndex) {
        gameBoard.setBoardState(playerColorNumber, tileIndex);
        gameBoard.addToHistory(gameBoard.getBoardState());
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
        //for (tileToChange : tiles) {
        //tileToChange[0], tileToChange[1], tileToChange[2]
        //goGui.addStone(x, y, isWhite);
        //}
    }

    /**Receives data about the board state sent by the server and updates the board.
     * @param currentPlayerColorNumber The number corresponding to the player's tile color
     * @param move the move to be added to the board, containing the tile's color (int)
     *             and a tile index for the new tile.
     * @param newBoard used to check if the boardHistory addition matches the server's.
     */
    void updateTurnFromServer(int currentPlayerColorNumber, String move, String newBoard) {
        String[] moveDetails = move.split(";");
        int playerColorNumber  = Integer.valueOf(moveDetails[1]);
        int tileIndex = Integer.valueOf(moveDetails[0]);
        updateBoard(playerColorNumber, tileIndex);

        List<String> gameBoardHistory = gameBoard.getBoardHistory();
        //TODO remove board history check, or make it more sensible
        if (!gameBoardHistory.get(gameBoardHistory.size() - 1).equals(newBoard)) {
            throw new RuntimeException("Added game history does not match server's board state.");
        }
        System.out.println("Move made: " + Arrays.toString(moveDetails));

        isClientsTurn = currentPlayerColorNumber == thisPlayerColor.getPlayerColorNumber();
        player.notifyTurn();
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
        gameBoard.addToHistory(gameBoard.getBoardState());
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
