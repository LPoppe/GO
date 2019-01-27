package main.Client.Player;

public interface Player {
    default void determineMove() {

    }

    default void userTileClicked(int x, int y) {

    }

    void notifyTurn();

}
