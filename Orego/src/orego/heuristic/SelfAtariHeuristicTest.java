package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class SelfAtariHeuristicTest {

	private Board board;

	private SelfAtariHeuristic heuristic;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new SelfAtariHeuristic(1);
	}

	@Test
	public void testEvaluate1() {
		String[] problem = new String[] { //
				"...................",// 19
				".###..#####........",// 18
				".#O#..#OOO#...#O#..",// 17
				".#O#..#OO##...#.#..",// 16
				".#.#..#OO#....#O#..",// 15
				"......#OO#.........",// 14
				"......#.##.........",// 13
				".............#.....",// 12
				"............#O#....",// 11
				"............#O#....",// 10
				".....##.....#O#....",// 9
				".....O.#....#.#....",// 8
				".....##.....#O#....",// 7
				"............#O#....",// 6
				"............#O#....",// 5
				"....#.......#O#....",// 4
				"...#...............",// 3
				"#................##",// 2
				"..................O"// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		assertEquals(-1, heuristic.evaluate(at("a1"), board));
		assertEquals(-2, heuristic.evaluate(at("s1"), board));
		assertEquals(-3, heuristic.evaluate(at("c15"), board));
		assertEquals(-10, heuristic.evaluate(at("h13"), board));
		assertEquals(-2, heuristic.evaluate(at("g8"), board));
		assertEquals(-8, heuristic.evaluate(at("o8"), board));
		assertEquals(0, heuristic.evaluate(at("e3"), board));
		assertEquals(0, heuristic.evaluate(at("q16"), board));


	}
}
