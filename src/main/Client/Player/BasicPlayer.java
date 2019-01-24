package main.Client.Player;

import main.Client.GoController;
import main.Logic.Board;
import main.Logic.GoGame;
import main.Logic.ValidityChecker;

public class BasicPlayer implements Player {
    private int allowedCalculationTime;
    private ValidityChecker checker;
    private GoController gameController;
    private Board gameBoard;
    private GoGame.PlayerColor myPlayerColor;

    public BasicPlayer (GoController controller, Board board, GoGame.PlayerColor thisPlayerColor) {
        this.checker = new ValidityChecker();
        this.gameController = controller;
        this.gameBoard = board;
        this.myPlayerColor = thisPlayerColor;
    }

    @Override
    public void determineMove() {
        int calculatedMove;
        String moveValidity;
        //Check validity
        do {
            calculatedMove = (int) Math.floor(Math.random() * gameBoard.getBoardSize());
            moveValidity = checker.checkMove(myPlayerColor.getPlayerColorNumber(), calculatedMove, gameBoard);
        } while (!moveValidity.equals("VALID"));
        gameController.sendMoveToClient(calculatedMove);
        //Further calculations
        //Send move to server
    }

    @Override
    public void notifyTurn() {
        //Upon being notified of its turn, the AI calculates a move to send to the server.
        determineMove();
    }
}
