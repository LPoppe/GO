import main.Logic.Board;
import main.Logic.Group;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**Test for the board class.*/
public class BoardTest {

    private Board board9;

    @Before
    public void setUp() {
        this.board9 = new Board(9);
    }

    @Test
    public void testHistory() {
        board9.addToHistory("01234567890");
        assertEquals("01234567890", board9.getBoardHistory().get(board9.getBoardHistory().size() - 1));
    }

    @Test
    public void testBoardUpdate() {
        board9.setBoardState(1, 0);
        assertSame(board9.getTileContent(0), Board.TileColor.black.getTileColorNumber());
        board9.setBoardState(2, 1);
        assertSame(board9.getTileContent(1), Board.TileColor.white.getTileColorNumber());
    }

    @Test
    public void testGroupAddition() {
        board9.setBoardState(1, 0);
        assertEquals(1, board9.getAllGroups().size());
        board9.setBoardState(1, 1);
        assertEquals(1, board9.getAllGroups().size());
        board9.setBoardState(1, 4);
        assertEquals(2, board9.getAllGroups().size());
    }

    @Test
    public void testGroupRemoval() {
        board9.setBoardState(1, 0);
        board9.setBoardState(1, 1);
        board9.setBoardState(1, 2);
        board9.setBoardState(1, 9);
        board9.setBoardState(1, 11);
        board9.setBoardState(1, 18);
        board9.setBoardState(1, 19);
        board9.setBoardState(1, 20);
        board9.setBoardState(2, 3);
        board9.setBoardState(2, 3);
        board9.setBoardState(2, 12);
        board9.setBoardState(2, 21);
        board9.setBoardState(2, 27);
        board9.setBoardState(2, 28);
        board9.setBoardState(2, 29);

        assertEquals(3, board9.getAllGroups().size());
        board9.setBoardState(2, 10);
        assertEquals(3, board9.getAllGroups().size());
        for (Group group : board9.getAllGroups()) {
            assertEquals(group.getTileColor(), Board.TileColor.white);
        }
    }
}
