package main.Client.Player;

import main.Client.GoController;
import main.Logic.Board;
import main.Logic.ValidityChecker;

public class BasicPlayer implements Player {
    private int allowedCalculationTime;
    private ValidityChecker checker;
    private GoController gameController;

    public BasicPlayer (GoController controller) {
        this.checker = new ValidityChecker();
        this.gameController = controller;
    }

    @Override
    public void determineMove() {
        Integer calculatedMove = null;
        int possibleMove = 0;
        //Check validity
        Board board = null;
        checker.checkMove(possibleMove, board);
        //Further calculations
        //Send move to server
    }

    @Override
    public void userTileClicked() {
        //AI does not use user input.
    }

    @Override
    public void notifyTurn() {

    }
}
