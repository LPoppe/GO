package main.Client.Player;

import main.Client.GoController;
import main.Logic.Board;
import main.Logic.GoGame;
import main.Logic.ValidityChecker;

public class HumanPlayer implements Player {
    private ValidityChecker checker;
    private boolean isMyTurn = false;
    private GoController gameController;
    private Board gameBoard;
    private GoGame.PlayerColor myPlayerColor;


    public HumanPlayer(GoController controller, Board board, GoGame.PlayerColor thisPlayerColor) {
        this.checker = new ValidityChecker();
        this.gameController = controller;
        this.gameBoard = board;
        this.myPlayerColor = thisPlayerColor;
    }

    private void determineMove(int xCoordinate, int yCoordinate) {
        int tileIndex = gameBoard.getTileIndex(xCoordinate, yCoordinate);
        //Returns "VALID" if valid, else an error message.
        String moveValidity = checker.checkMove(myPlayerColor.getPlayerColorNumber(), tileIndex, gameBoard);
        //If the move was valid, the player sends the move to the controller,
        // and no further input will be allowed until until isMyTurn is set to true again.
        if (moveValidity.equals("VALID")) {
            gameController.sendMoveToClient(tileIndex);
            isMyTurn = false;
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
}