package main.Client;

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
            this.player = new HumanPlayer();
        } else if (choice.equals("A")) {
            this.player = new BasicPlayer();
        }
    }

    /**Starts the game GUI. Called after connecting to the server using the TUI
     * and configuring the game.
     */
    private void startGUI(int boardSize) {
        this.goGui = new GoGuiIntegrator(true, true, boardSize);
        goGui.startGUI();
        goGui.setBoardSize(boardSize);
    }

    private void updateGuiBoard() {
//        for (stone : gameBoard) {
//            goGui.addStone();
//        }
    }

    /**
     *
     * @param currentPlayerColorNumber
     * @param move
     * @param newBoard used to check if the boardHistory addition matches the server's.
     */
    void updateAfterTurn(int currentPlayerColorNumber, String move, String newBoard) {
        String[] moveDetails = move.split(";");
        gameBoard.setBoardState(Integer.valueOf(moveDetails[0]), Integer.valueOf(moveDetails[1]));
        gameBoard.addToHistory(gameBoard.getBoardState());
        List<String> gameBoardHistory = gameBoard.getBoardHistory();
        //TODO remove board history check, or make it more sensible
        if (!gameBoardHistory.get(gameBoardHistory.size() - 1).equals(newBoard)) {
            throw new RuntimeException("Added game history does not match server's board state.");
        }

        updateGuiBoard();
        System.out.println("Move made: " + Arrays.toString(moveDetails));

        isClientsTurn = currentPlayerColorNumber == thisPlayerColor.getPlayerColorNumber();
        if (isClientsTurn) {
            int playerMoveIndex = player.determineMove(gameBoard);
            gameClient.sendMove(playerMoveIndex);
        }
    }

    /**
     * Initialises a game board.
     * @param boardSize the size of the board.
     */
    public void setGame(Integer boardSize, String opponent, int colorNumber) {
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
