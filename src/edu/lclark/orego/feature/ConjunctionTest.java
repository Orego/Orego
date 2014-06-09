package edu.lclark.orego.feature;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.StoneColor.*;

public class ConjunctionTest {

	private Board board;
	
	private Conjunction conjunction;

	private CoordinateSystem coords;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new Board(7);
		coords = board.getCoordinateSystem();
		conjunction = new Conjunction(new NotEyeLike(board), OnThirdOrFourthLine.forWidth(coords.getWidth()));
	}

	@Test
	public void testAt() {
		String[] before = {
				"......#",
				".....#.",
				"..#...#",
				".#.#...",
				"..#....",
				".......",
				".......",
		};
		board.setUpProblem(before, BLACK);
		assertFalse(conjunction.at(at("g6")));
		assertFalse(conjunction.at(at("c4")));
		assertFalse(conjunction.at(at("a2")));
		assertTrue(conjunction.at(at("d3")));
	}

}
