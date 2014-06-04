package edu.lclark.orego.feature;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class CaptureSuggesterTest {

	private Board board;
	
	private CoordinateSystem coords;

	private CaptureSuggester movesToCapture;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		movesToCapture = new CaptureSuggester(board);
	}
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}
	
	@Test
	public void testGet() {
		String[] diagram = {
				"O#...",
				"....#",
				"...#O",
				".....",
				".....",
		};
		board.setUpProblem(diagram, BLACK);
		assertTrue(movesToCapture.get().contains(at("e2")));
		assertTrue(movesToCapture.get().contains(at("a4")));
	}

	@Test
	public void testMultipleCaptures() {
		String[] diagram = {
				".#O#.",
				"##O##",
				"OO.OO",
				"##O##",
				".#O#.",
		};
		board.setUpProblem(diagram, BLACK);
		assertEquals(1, movesToCapture.get().size());
		assertTrue(movesToCapture.get().contains(at("c3")));
	}

}
