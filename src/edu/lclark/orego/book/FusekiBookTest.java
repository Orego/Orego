package edu.lclark.orego.book;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class FusekiBookTest {

	private FusekiBook book;

	private Board board;

	private CoordinateSystem coords;

	@Before
	public void setUp() {
		final FusekiBookBuilder builder = new FusekiBookBuilder(20, 2, "test-books",
				false);
		// We process the files twice because FusekiBookBuilder requires that a
		// move be seen at least twice
		builder.processFiles(new File("sgf-test-files/19"));
		builder.processFiles(new File("sgf-test-files/19"));
		builder.writeRawBook();
		builder.buildFinalBook();
		book = new FusekiBook("test-books");
		board = new Board(19);
		coords = board.getCoordinateSystem();
	}

	@Test
	public void testFusekiBook1() {
		String[] correct;
		correct = new String[] { "Q4", "D16", "C4" };
		for (final String move : correct) {
			final short m = book.nextMove(board);
			assertEquals(move, coords.toString(m));
			board.play(m);
		}
		board.play("h8");
		assertEquals(NO_POINT, book.nextMove(board));
	}

}
