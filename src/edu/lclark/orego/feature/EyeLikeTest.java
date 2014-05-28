package edu.lclark.orego.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import static edu.lclark.orego.core.StoneColor.*;

public class EyeLikeTest {

	/** Delegate method to call at on board. */
	private short at(String label) {
		return board.getCoordinateSystem().at(label);
	}

	private Board board;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
	}

	@Test
	public void testCorner() {
		String[] before = {
				".#...",
				"#O...",
				".....",
				"#...O",
				".#.O.",
		};
		board.setUpProblem(before, BLACK);
		assertTrue(EyeLike.isEyeLike(at("a1"), board));
		assertFalse(EyeLike.isEyeLike(at("e1"), board));
		assertFalse(EyeLike.isEyeLike(at("a5"), board));
		assertFalse(EyeLike.isEyeLike(at("a3"), board));
	}

	@Test
	public void testEdge() {
		String[] before = {
				".O.O.",
				"..O..",
				".....",
				"..O#.",
				".O.O.",
		};
		board.setUpProblem(before, WHITE);
		assertTrue(EyeLike.isEyeLike(at("c5"), board));
		assertFalse(EyeLike.isEyeLike(at("c1"), board));
	}

	@Test
	public void testCenter() {
		String[] before = {
				".....",
				".#O#.",
				"#.#.#",
				".#.#O",
				".....",
		};
		board.setUpProblem(before, BLACK);
		assertTrue(EyeLike.isEyeLike(at("b3"), board));
		assertFalse(EyeLike.isEyeLike(at("d3"), board));
		assertFalse(EyeLike.isEyeLike(at("c2"), board));
	}
	
}
