package main.Client.Player;

public interface Player {

    //updateGroups
        //opponent groups
        //own groups
    //addToGroup
    //updateGroups - check if alive or dead - don't forget false eyes (not to capture groups unless threatened from outside

    //removeFromGroup - dead stones
    //getGroupSize

    //makeMove
        //validity requirements



    //pass

    //getValidMoves
        //stonesTaken
        //koRule -- illegal to place stone which will recreate the position prior to opponents last move

    //findCaptureGroups

    //determineScores
        //intersections occupied by colour
        //any empty intersections surrounded by colour
        //intersections surrounded by both are neutral
}
