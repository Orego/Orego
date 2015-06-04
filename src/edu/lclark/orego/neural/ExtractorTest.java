package edu.lclark.orego.neural;

import static edu.lclark.orego.core.StoneColor.WHITE;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

public class ExtractorTest {

	private Board board;
	
	private Extractor extractor;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		extractor = new Extractor(board);
	}

	@Test
	public void testIsBlack() {
		String[] diagram = {
				"...#.",
				".....",
				".....",
				".....",
				".....",
		};
		board.setUpProblem(diagram, WHITE);
		assertEquals(0.0, extractor.isBlack(1, 7), 0.001);
		assertEquals(1.0, extractor.isBlack(0, 3), 0.001);
		assertEquals(0.0, extractor.isBlack(2, 3), 0.001);
	}

}
