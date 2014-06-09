package edu.lclark.orego.feature;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class DisjunctionTest {

	private Board board;
	
	private Disjunction disjunction;

	private CoordinateSystem coords;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new Board(7);
		coords = board.getCoordinateSystem();
		disjunction = new Disjunction(new NotEyeLike(board), OnThirdOrFourthLine.forWidth(coords.getWidth()));
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
		assertFalse(disjunction.at(at("g6")));
		assertTrue(disjunction.at(at("c4")));
		assertTrue(disjunction.at(at("a2")));
		assertTrue(disjunction.at(at("d3")));
	}

}
