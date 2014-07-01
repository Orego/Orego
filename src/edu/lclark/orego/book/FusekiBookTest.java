package edu.lclark.orego.book;

import static org.junit.Assert.*;

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
		FusekiBookBuilder builder = new FusekiBookBuilder(20, 1);
		// Uncomment the next line to build the book from scratch.
		builder.analyzeFiles(new File("SgfTestFiles/19"), "TestBooks");
		builder.buildFinalBook("TestBooks", "TestBooks");
		book = new FusekiBook("TestBooks");
		board = new Board(19);
		coords = board.getCoordinateSystem();
	}

	@Test
	public void testFusekiBook1() {
		String[] correct;
			correct = new String[] { "Q4", "D16", "C4" };
		for (String move : correct) {
			short m = book.nextMove(board);
			assertEquals(move, coords.toString(m));
			board.play(m);
		}
	}
}
