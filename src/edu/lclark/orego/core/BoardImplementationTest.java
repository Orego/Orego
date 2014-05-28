package edu.lclark.orego.core;

import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class BoardImplementationTest {

	private BoardImplementation board;

	private CoordinateSystem coords;
	
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
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new BoardImplementation(5);
		coords = board.getCoordinateSystem();
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
		short p = coords.getNeighbors(at("e2"))[EAST_NEIGHBOR];
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
		short[] points = coords.getAllPointsOnBoard();
		int i;
		for (i = 0; i < points.length - 2; i++) {
			assertEquals(OK, board.play(points[i]));
			board.pass();
		}
		assertEquals(OK, board.play(points[i]));
		i++;
		assertEquals(OK, board.play(points[i]));
		board.pass();
		int n = coords.getArea() * 2 - 1;
		for (i = 0; i < points.length - 2; i++) {
			assertEquals(OK, board.play(points[i]));
			n++;
			if (n == coords.getMaxMovesPerGame() - 2) {
				break;
			}
			board.pass();
			n++;
			if (n == coords.getMaxMovesPerGame() - 2) {
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
	
	@Test
	public void testGetTurn() {
		assertEquals(0, board.getTurn());
		board.play(at("c1"));
		assertEquals(1, board.getTurn());
		board.pass();
		assertEquals(2, board.getTurn());
	}

	@Test
	public void testBug1() {
		assertEquals(OK, board.play(at("D5")));
		assertEquals(OK, board.play(at("A5")));
		assertEquals(OK, board.play(at("A2")));
		assertEquals(OK, board.play(at("E4")));
		assertEquals(OK, board.play(at("D2")));
		assertEquals(OK, board.play(at("C4")));
		assertEquals(OK, board.play(at("B2")));
		assertEquals(OK, board.play(at("B5")));
		assertEquals(OK, board.play(at("E3")));
		assertEquals(OK, board.play(at("A1")));
		assertEquals(OK, board.play(at("C2")));
		assertEquals(OK, board.play(at("B1")));
		assertEquals(OK, board.play(at("D1")));
		assertEquals(OK, board.play(at("E2")));
		assertEquals(OK, board.play(at("D4")));
		assertEquals(OK, board.play(at("A4")));
		assertEquals(OK, board.play(at("E5")));
		assertEquals(OK, board.play(at("D3")));
		assertEquals(OK, board.play(at("C3")));
		assertEquals(OK, board.play(at("B4")));
		assertEquals(OK, board.play(at("C5")));
		assertEquals(OK, board.play(at("B3")));
		assertEquals(OK, board.play(at("C1")));
		assertEquals(OK, board.play(at("B1")));
		assertEquals(OK, board.play(at("D3")));
		assertEquals(SUICIDE, board.play(at("A1")));
	}

	@Test
	public void testLibertyUpdate() {
		String[] before = {
				".#O#.",
				".#O#.",
				"##.##",
				".OO..",
				".....",
		};
		board.setUpProblem(before, BLACK);
		assertEquals(1, board.getLiberties(at("c4")).size());
		assertEquals(4, board.getLiberties(at("b3")).size());
		assertEquals(5, board.getLiberties(at("d3")).size());
		assertEquals(5, board.getLiberties(at("c2")).size());
		assertEquals(OK, board.play(at("c3")));
		assertEquals(9, board.getLiberties(at("c3")).size());
	}

	@Test
	public void testPlayFast() {
		String[] before = {
				".#O.O",
				"#O.O#",
				"...#.",
				"O#..#",
				".O#..",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(OK, board.playFast(at("a5")));
		assertEquals(OK, board.playFast(at("a1")));
		assertEquals(OK, board.playFast(at("e3")));
		assertEquals(OK, board.playFast(at("b5")));
		assertEquals(OK, board.playFast(at("b1")));
		assertEquals(OK, board.playFast(at("e4")));
		assertEquals(asOneString(before), board.toString());
	}

}
