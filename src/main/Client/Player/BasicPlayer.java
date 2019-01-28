package main.Client.Player;

import main.Client.GoController;
import main.Logic.Board;
import main.Logic.GoGame;
import main.Logic.ValidityChecker;

import java.util.ArrayList;
import java.util.List;

public class BasicPlayer implements Player {
    private int allowedCalculationTime;
    private ValidityChecker checker;
    private GoController gameController;
    private Board gameBoard;
    private GoGame.PlayerColor myPlayerColor;
    //move time in seconds?
    private int moveTime;

    public BasicPlayer(GoController controller, Board board, GoGame.PlayerColor thisPlayerColor) {
        this.checker = new ValidityChecker();
        this.gameController = controller;
        this.gameBoard = board;
        this.myPlayerColor = thisPlayerColor;
    }

    @Override
    public void determineMove() {
        List<Integer> validOptions = new ArrayList<>();
        for (int option : gameBoard.getBoardFields()) {
            String moveValidity = checker.checkMove(myPlayerColor.getPlayerColorNumber(), option, gameBoard);
            if (moveValidity.equals("VALID")) {
                validOptions.add(option);
            }
        }
        if (this == gameController.getPlayer()) {
            if (validOptions.isEmpty()) {
                //Send -1 for passing if no valid options are available.
                gameController.sendMoveToClient(-1);
            } else {
                Integer calculatedMove = validOptions.get((int) Math.floor(Math.random() * validOptions.size()));
                gameController.sendMoveToClient(calculatedMove);
            }
        } else {
            if (validOptions.isEmpty()) {
                //Send -1 for passing if no valid options are available.
                System.out.println("No more valid moves found.");
            } else {
                Integer calculatedMove = validOptions.get((int) Math.floor(Math.random() * validOptions.size()));
                gameController.displayHint(calculatedMove);
            }
        }
    }

    @Override
    public void notifyTurn() {
        //Upon being notified of its turn, the AI calculates a move to send to the server.
        determineMove();
    }
}
