package main.Client.Player;

public interface Player {
    default void determineMove() {

    }

    void notifyTurn();

    //Default to prevent needing to match implementation between HumanPlayer and the AI.
    default void userClickedPass() {
    }

    default void userTileClicked(int x, int y) {
    }
}
