package edu.lclark.orego.core;

import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class BoardImplementationTest {

	private BoardImplementation board;

	/** Returns a single String built from diagram, analogous to that produced by BoardImplementation.toString. */
	private static String asOneString(String[] diagram) {
		String result = "";
		for (String s : diagram) {
			result += s + "\n";
		}
		return result;
	}

	/** Delegate method to call at on board. */
	private short at(String label) {
		return board.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new BoardImplementation(5);
	}

	@Test
	public void testSimplePlay() {
		assertEquals(OK, board.play(at("a2")));
		assertEquals(OK, board.play(at("b3")));
		assertEquals(BLACK, board.getColorAt(at("a2")));
		assertEquals(WHITE, board.getColorAt(at("b3")));
	}
	
	@Test(expected = AssertionError.class)
	public void testOffBoard() {
		// p is not on the board
		short p = board.getNeighbors(at("e2"))[EAST_NEIGHBOR];
		board.play(p);
	}

	@Test
	public void testOccupied() {
		board.play(at("c1"));
		assertEquals(OCCUPIED, board.play(at("c1")));
	}

	@Test
	public void testPass() {
		assertEquals(OK, board.play(at("pass")));
		assertEquals(WHITE, board.getColorToPlay());
	}

	@Test
	public void testSuicide() {
		String[] before = {
				".O.#.",
				".##..",
				".....",
				".##..",
				"#O.#.",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(SUICIDE, board.play(at("c1")));
		assertEquals(OK, board.play(at("c5")));
		String[] after = {
				".OO#.",
				".##..",
				".....",
				".##..",
				"#O.#.",
		};
		assertEquals(asOneString(after), board.toString());
	}

	@Test
	public void testCapture() {
		String[] before = {
				".....",
				".....",
				".....",
				"..#..",
				"#OO#.",
		};
		board.setUpProblem(before, BLACK);
		assertEquals(OK, board.play(at("b2")));
		String[] after = {
				".....",
				".....",
				".....",
				".##..",
				"#..#.",
		};
		assertEquals(asOneString(after), board.toString());
	}

	@Test
	public void testMultipleCapture() {
		String[] before = {
				".O#O.",
				"OO#O.",
				"##.##",
				"OO#OO",
				".O#O.",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(OK, board.play(at("c3")));
		String[] after = {
				".O.O.",
				"OO.O.",
				"..O##",
				"OO.OO",
				".O.O.",
		};
		assertEquals(asOneString(after), board.toString());
	}

}
