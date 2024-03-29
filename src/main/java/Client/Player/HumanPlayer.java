package Client.Player;

import Client.GoController;
import Logic.Board;
import Logic.GoGame;
import Logic.ValidityChecker;

public class HumanPlayer implements Client.Player.Player {
    private ValidityChecker checker;
    private boolean isMyTurn = false;
    private GoController gameController;
    private Board gameBoard;
    private GoGame.PlayerColor myPlayerColor;
    private Player hintAI;


    public HumanPlayer(GoController controller, Board board, GoGame.PlayerColor thisPlayerColor, Player hintAI) {
        this.checker = new ValidityChecker();
        this.gameController = controller;
        this.gameBoard = board;
        this.myPlayerColor = thisPlayerColor;
        this.hintAI = hintAI;
    }

    private void determineMove(int xCoordinate, int yCoordinate) {
        int tileIndex = gameBoard.getTileIndex(xCoordinate, yCoordinate);
        //Returns "VALID" if valid, else an error message.
        String moveValidity = checker.checkMove(myPlayerColor.getPlayerColorNumber(), tileIndex, gameBoard);
        //If the move was valid, the player sends the move to the controller,
        // and no further input will be allowed until until isMyTurn is set to true again.
        if (moveValidity.equals("VALID")) {
            isMyTurn = false;
            gameController.sendMoveToClient(tileIndex);
        } else {
            System.out.println(moveValidity);
        }
    }

    public void userTileClicked(int xCoordinate, int yCoordinate) {
        if (isMyTurn) {
            determineMove(xCoordinate, yCoordinate);
        }
    }

    public void notifyTurn() {
        isMyTurn = true;
    }

    public void userClickedPass() {
        if (isMyTurn) {
            gameController.sendMoveToClient(-1);
            isMyTurn = false;
        }
    }
}