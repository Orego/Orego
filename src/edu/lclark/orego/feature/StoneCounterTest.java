package edu.lclark.orego.feature;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortList;

public class StoneCounterTest {

	private Board board;
	
	private CoordinateSystem coords;
	
	private StoneCounter counter;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		counter = new StoneCounter(board);
	}

	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}

	@Test
	public void testCount() {
		String[] diagram = {
				"##.#O",
				"OO.O.",
				"....O",
				".#...",
				".#...",
		};
		board.setUpProblem(diagram, WHITE);
		board.play(at("c5"));
		assertEquals(2, counter.getCount(BLACK));
		assertEquals(6, counter.getCount(WHITE));
	}

}
