package orego.book;

import org.junit.Before;
import org.junit.Test;
import static orego.core.Coordinates.*;
import orego.play.Player;
import orego.play.UnknownPropertyException;
import orego.core.Board;
import static org.junit.Assert.*;

public class CombinedBookTest {

	private Player player;
	private CombinedBook gen;
	private Board board;

	@Before
	public void setUp() throws Exception {

		player = new Player();
		gen = new CombinedBook("SgfTestFiles");
		board = new Board();
		board.clear();
		player.reset();
	}

	@Test
	public void testCombinedBook1() {
		String[] correct;

		correct = new String[] { "q4", "d16", "c4" };
		for (String move : correct) {
			assertEquals(pointToString(at(move)),
					pointToString(gen.nextMove(board)));
			board.play(move);
		}
	}

	@Test
	public void testOpeningBook() throws UnknownPropertyException {

		player.setOpeningBook(gen);
		int p = player.bestMove();
		assertEquals(at("q4"), p);
	}
}
