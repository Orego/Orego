package orego.heuristic;

import static orego.core.Colors.*;
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
		heuristic.prepare(board);
		assertTrue(heuristic.getBadMoves().contains(at("a1")));
		assertTrue(heuristic.getBadMoves().contains(at("s1")));
		assertTrue(heuristic.getBadMoves().contains(at("c15")));
		assertTrue(heuristic.getBadMoves().contains(at("h13")));
		assertTrue(heuristic.getBadMoves().contains(at("g8")));
		assertTrue(heuristic.getBadMoves().contains(at("o8")));
		assertFalse(heuristic.getBadMoves().contains(at("e3")));
		assertFalse(heuristic.getBadMoves().contains(at("q16")));
	}
	
	@Test
	public void testClone() throws Exception {
		SelfAtariHeuristic copy = heuristic.clone();
		
		assertNotSame(heuristic, copy);
		assertTrue(copy instanceof SelfAtariHeuristic);
	}

}
