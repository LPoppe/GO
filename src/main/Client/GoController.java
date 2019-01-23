package main.Client;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
    void initPlayer(String choice) {
        if (choice.equals("H")) {
            this.player = new HumanPlayer(this);
        } else if (choice.equals("A")) {
            this.player = new BasicPlayer(this);
        }
    }

    /**Starts the game GUI. Called after connecting to the server using the TUI
     * and configuring the game.
     */
    private void startGUI(int boardSize) {
        this.goGui = new GoGuiIntegrator(true, true, boardSize);
        goGui.startGUI();
        goGui.setBoardSize(boardSize);

        goGui.getPrimaryStage().addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                System.out.println("Mouse click detected! " + mouseEvent.getSource());
                System.out.println("X = " + mouseEvent.getSceneX() + ", Y = " + mouseEvent.getSceneY());

                int x = (int) (Math.round((mouseEvent.getSceneX() / goGui.getInitialSquareSize()) - 1));
                int y = (int) (Math.round((mouseEvent.getSceneY() / goGui.getInitialSquareSize()) - 1));
                System.out.println("int X = " + x + ", int Y = " + y);
//                goGui.addStone(x, y, true);
                player.userTileClicked();
            }
        });
    }

    private void updateBoard(int playerColorNumber, int tileIndex) {
        gameBoard.setBoardState(playerColorNumber, tileIndex);
        gameBoard.addToHistory(gameBoard.getBoardState());
        updateGUI();
    }

    private void updateGUI() {
        //for (tileToChange : tiles) {
        //tileToChange[0], tileToChange[1], tileToChange[2]
        //goGui.addStone(x, y, isWhite);
        //}
    }

    /**Receives data about the boardstate sent by the server and updates the board.
     * @param currentPlayerColorNumber The number corresponding to the player's tile color
     * @param move the move to be added to the board, containing the tile's color (int)
     *             and a tile index for the new tile.
     * @param newBoard used to check if the boardHistory addition matches the server's.
     */
    void updateTurnFromServer(int currentPlayerColorNumber, String move, String newBoard) {
        String[] moveDetails = move.split(";");
        int playerColorNumber  = Integer.valueOf(moveDetails[0]);
        int tileIndex = Integer.valueOf(moveDetails[1]);
        updateBoard(playerColorNumber, tileIndex);

        List<String> gameBoardHistory = gameBoard.getBoardHistory();
        //TODO remove board history check, or make it more sensible
        if (!gameBoardHistory.get(gameBoardHistory.size() - 1).equals(newBoard)) {
            throw new RuntimeException("Added game history does not match server's board state.");
        }
        System.out.println("Move made: " + Arrays.toString(moveDetails));

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
    }
    private void setBoard(Integer boardSize) {
        this.gameBoard = new Board(boardSize);
        System.out.println("Boardsize set to: " + boardSize);
        gameBoard.addToHistory(gameBoard.getBoardState());
        startGUI(boardSize);
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
}
