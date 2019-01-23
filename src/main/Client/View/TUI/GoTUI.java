package main.Client.View.TUI;

import main.Logic.Board;

import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

public class GoTUI {

    public void startGame() {
        Scanner clientScanner = new Scanner(System.in);
    }

    public void drawGame(Board board) {
        System.out.println(board.drawBoard());
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
