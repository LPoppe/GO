import main.Logic.Board;
import main.Logic.ValidityChecker;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**Test for the validity checker class.*/
public class ValidityTest {

    private Board board5;
    private Board boardWithTile;
    private Board boardForKo;
    private ValidityChecker checker;


    @Before
    public void setUp() {
        this.checker = new ValidityChecker();
        this.board5 = new Board(5);
        this.boardWithTile = new Board(19);
        boardWithTile.setBoardState(1, 5);
        //Placing a tile of the same color on index 3 removes the other tiles,
        // and recreates the initial board state.
        this.boardForKo = new Board(2);
        boardForKo.setBoardState(2, 0);
        boardForKo.setBoardState(2, 1);
        boardForKo.setBoardState(2, 2);
    }

    @Test
    public void testDeepCopy() {
        board5.setBoardState(2, 5);
        Board newBoard = checker.deepCopyBoard(board5);
        //Assert the boards themselves are different objects.
        assertNotSame(board5.getAllGroups(), newBoard.getAllGroups());
        //If the copied groups are still associated with the old board, this will fail.
        assertNotEquals("Move breaks Ko rule.", checker.checkMove(1, 3, boardForKo));
        //Assert the board contents match.
        assertEquals(board5.getBoardHistory().get(board5.getBoardHistory().size() - 1),
                newBoard.getBoardHistory().get(newBoard.getBoardHistory().size() - 1));
        assertEquals(board5.getBoardState(), newBoard.getBoardState());
        assertEquals(board5.getCurrentBoard()[9], newBoard.getCurrentBoard()[9]);
        //However, objects should not.
        //TODO this does not test the content of the group set, but I'm not sure how to add that.
        assertNotSame(board5.getCurrentBoard(), newBoard.getCurrentBoard());
        assertNotSame(board5.getAllGroups(), newBoard.getAllGroups());
        assertNotSame(board5.getCurrentBoard(), newBoard.getCurrentBoard());
        //Alter the copy's state. Now neither should match.
        newBoard.setBoardState(2, 9);
        assertNotEquals(board5.getBoardHistory().get(board5.getBoardHistory().size() - 1),
                newBoard.getBoardHistory().get(newBoard.getBoardHistory().size() - 1));
        assertNotEquals(board5.getCurrentBoard()[9], newBoard.getCurrentBoard()[9]);
        assertNotEquals(board5.getBoardState(), newBoard.getBoardState());
        assertNotEquals(board5.getAllGroups(), newBoard.getAllGroups());
    }

    @Test
    public void testCoordinateCheck() {
        assertTrue(checker.isValidCoordinate(0, board5));
        assertTrue(checker.isValidCoordinate(24, board5));
        assertFalse(checker.isValidCoordinate(25, board5));
        assertFalse(checker.isValidCoordinate(-1, board5));
    }

    @Test
    public void testTileContentCheck() {
        assertTrue(checker.isTileEmpty(3, boardWithTile));
        assertTrue(checker.isTileEmpty(3, boardForKo));
        assertFalse(checker.isTileEmpty(5, boardWithTile));
        assertFalse(checker.isTileEmpty(0, boardForKo));
    }

    @Test
    public void testSuperKoCheck() {
        Board newBoard = checker.deepCopyBoard(boardForKo);
        newBoard.setBoardState(2, 3);
        Board anotherBoard = checker.deepCopyBoard(boardWithTile);
        anotherBoard.setBoardState(2, 30);
        assertFalse(checker.doesNotBreakKoRule(newBoard.getBoardState(), boardForKo));
        assertTrue(checker.doesNotBreakKoRule(anotherBoard.getBoardState(), boardWithTile));
    }

    @Test
    public void testFullMoveCheck() {
        assertEquals("VALID",
                checker.checkMove(1, 0, board5));
        assertEquals("Coordinate is not a valid location on the board.",
                checker.checkMove(1, 26, board5));
        assertEquals("Chosen tile is already occupied.",
                checker.checkMove(1, 5, boardWithTile));
        assertEquals("Chosen tile is already occupied.",
                checker.checkMove(2, 5, boardWithTile));
        assertEquals("Move breaks Ko rule.", checker.checkMove(2, 3, boardForKo));
    }

}
