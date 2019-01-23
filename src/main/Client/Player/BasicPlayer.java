package main.Client.Player;

import main.Logic.Board;
import main.Logic.ValidityChecker;

public class BasicPlayer implements Player {
    private int allowedCalculationTime;
    private ValidityChecker checker;

    public BasicPlayer () {
        this.checker = new ValidityChecker();
    }
    @Override
    public Integer determineMove(Board board) {
        Integer calculatedMove = null;
        int possibleMove = 0;
        //Check validity
        checker.checkMove(possibleMove, board);
        //Further calculations
        return calculatedMove;
    }
}
