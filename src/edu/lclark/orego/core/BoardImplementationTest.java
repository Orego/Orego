package edu.lclark.orego.core;

import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class BoardImplementationTest {

	private BoardImplementation board;

	/**
	 * Returns a single String built from diagram, analogous to that produced by
	 * BoardImplementation.toString.
	 */
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

	@Test
	public void testSimpleKo() {
		String[] before = {
				".....",
				".....",
				".....",
				"#O...",
				".#O..",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(OK, board.play(at("a1")));
		assertEquals(KO_VIOLATION, board.play(at("b1")));
		String[] after = {
				".....",
				".....",
				".....",
				"#O...",
				"O.O..",
		};
		assertEquals(asOneString(after), board.toString());
	}

	@Test
	public void testPositionalSuperKo() {
		String[] before = {
				".....",
				".....",
				".....",
				"O##..",
				".O.#.",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(OK, board.play(at("c1")));
		assertEquals(OK, board.play(at("a1")));
		assertEquals(KO_VIOLATION, board.play(at("b1")));
		String[] after = {
				".....",
				".....",
				".....",
				"O##..",
				"#..#.",
		};
		assertEquals(asOneString(after), board.toString());
	}
	
	@Test
	public void testMaxMovesPerGame() {
		short[] points = board.getAllPointsOnBoard();
		int i;
		for (i = 0; i < points.length - 2; i++) {
			assertEquals(OK, board.play(points[i]));
			board.pass();
		}
		assertEquals(OK, board.play(points[i]));
		i++;
		assertEquals(OK, board.play(points[i]));
		board.pass();
		int n = board.getArea() * 2 - 1;
		for (i = 0; i < points.length - 2; i++) {
			assertEquals(OK, board.play(points[i]));
			n++;
			if (n == board.getMaxMovesPerGame() - 2) {
				break;
			}
			board.pass();
			n++;
			if (n == board.getMaxMovesPerGame() - 2) {
				break;
			}
		}
		i++;
		assertEquals(GAME_TOO_LONG, board.play(points[i]));
		assertEquals(OK, board.play(PASS));
		i++;
		assertEquals(GAME_TOO_LONG, board.play(points[i]));
		assertEquals(OK, board.play(PASS));
		return;
	}

	@Test
	public void testPasses() {
		assertEquals(0, board.getPasses());
		board.pass();
		assertEquals(1, board.getPasses());
		board.play(at("c4"));
		assertEquals(0, board.getPasses());
		board.pass();
		assertEquals(1, board.getPasses());
		board.pass();
		assertEquals(2, board.getPasses());
	}
}
