package main.Client.Player;

import main.Client.GoController;
import main.Logic.Board;
import main.Logic.GoGame;
import main.Logic.ValidityChecker;

import java.util.List;

public class TerritorialPlayer implements Player {
    private int allowedCalculationTime;
    private ValidityChecker checker;
    private GoController gameController;
    private Board gameBoard;
    private GoGame.PlayerColor myPlayerColor;
    //move time in seconds?
    private int moveTime;
    private int turnTimer;

    public TerritorialPlayer(GoController controller, Board board, GoGame.PlayerColor thisPlayerColor) {
        this.checker = new ValidityChecker();
        this.gameController = controller;
        this.gameBoard = board;
        this.myPlayerColor = thisPlayerColor;
    }

    /**Determines a valid move using some criteria (see the MoveChoiceLogic class).
     * If it is the player, the move is sent onwards to the server. If it is the hint AI,
     * the move is instead sent to the controller to be displayed on the GUI as a hint.
     */
    @Override
    public void determineMove() {
        //Create list of valid options.
        List<Integer> validOptions = MoveChoiceLogic.checkValidity(gameBoard, checker, myPlayerColor);
        //Remove tiles that cause own group capture from options.
        List<Integer> filteredOptions = MoveChoiceLogic.preventOwnCapture(gameBoard, validOptions, myPlayerColor);
        if (this == gameController.getPlayer()) {
            if (filteredOptions.isEmpty()) {
                //Send -1 for passing if no valid options are available.
                gameController.sendMoveToClient(-1);
            } else {
                //Choose a random tile from the remaining options.
                Integer calculatedMove = null;
                //Get a possible opening move if in the first few moves.
                //Check if an opponent's group can be captured
                Integer bestCaptureOption = MoveChoiceLogic.checkForCapture(gameBoard, filteredOptions, myPlayerColor);
                Integer opponentCapture = MoveChoiceLogic.predictOpponentCapture(gameBoard, filteredOptions, myPlayerColor);
                if (bestCaptureOption != null && opponentCapture != null) {
                    calculatedMove = MoveChoiceLogic.compareMoves(bestCaptureOption, opponentCapture);
                } else if (bestCaptureOption != null) {
                    calculatedMove = bestCaptureOption;
                } else if (opponentCapture != null) {
                    calculatedMove = opponentCapture;
                } else {
                    Integer bestAttack = MoveChoiceLogic.findWeakestLink(gameBoard, filteredOptions, myPlayerColor);
                    if (bestAttack != null) {
                        calculatedMove = bestAttack;
                    } else {
                        Integer eyeCreator = MoveChoiceLogic.stabilizeGroup(gameBoard, filteredOptions, myPlayerColor);
                        if (eyeCreator != null) {
                            calculatedMove = eyeCreator;
                        } else {
                            calculatedMove = filteredOptions.get((int) Math.floor(Math.random() * filteredOptions.size()));
                        }
                    }
                }
                gameController.sendMoveToClient(calculatedMove);
            }
        } else {
            if (filteredOptions.isEmpty()) {
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
