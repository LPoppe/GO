package main.Client.Player;

import main.Client.GoController;
import main.Logic.Board;
import main.Logic.ValidityChecker;

public class HumanPlayer implements Player {
    private ValidityChecker checker;
    private boolean isMyTurn = false;
    private GoController gameController;


    public HumanPlayer(GoController controller) {
        this.checker = new ValidityChecker();
        this.gameController = controller;
    }

    @Override
    public void determineMove() {
        //Send input to server.
    }

    @Override
    public void userTileClicked() {
        if (isMyTurn) {
            determineMove();
            isMyTurn = false;
        }
    }

    @Override
    public void notifyTurn() {
        isMyTurn = true;
    }
}