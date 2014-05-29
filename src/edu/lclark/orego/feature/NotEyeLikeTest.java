package edu.lclark.orego.feature;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.Board;
import static edu.lclark.orego.core.StoneColor.*;

public class NotEyeLikeTest {

	/** Delegate method to call at on board. */
	private short at(String label) {
		return board.getCoordinateSystem().at(label);
	}

	private Board board;
	
	private NotEyeLike notEyeLike;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		notEyeLike = new NotEyeLike(board);
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
		assertFalse(notEyeLike.at(at("a1")));
		assertTrue(notEyeLike.at(at("e1")));
		assertTrue(notEyeLike.at(at("a5")));
		assertTrue(notEyeLike.at(at("a3")));
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
		assertFalse(notEyeLike.at(at("c5")));
		assertTrue(notEyeLike.at(at("c1")));
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
		assertFalse(notEyeLike.at(at("b3")));
		assertTrue(notEyeLike.at(at("d3")));
		assertTrue(notEyeLike.at(at("c2")));
	}
	
}
