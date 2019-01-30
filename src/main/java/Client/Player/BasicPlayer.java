package Client.Player;

import Client.GoController;
import Logic.Board;
import Logic.GoGame;
import Logic.ValidityChecker;

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

    /**Determines a random valid move to place on the board.
     * If it is the player, the move is sent onwards to the server. If it is the hint AI,
     * the move is instead sent to the controller to be displayed on the GUI as a hint.
     */
    @Override
    public void determineMove() {
        List<Integer> validOptions = MoveChoiceLogic.checkValidity(gameBoard, checker, myPlayerColor);
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
