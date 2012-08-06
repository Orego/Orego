package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import orego.core.Board;
import org.junit.Before;
import org.junit.Test;

public class ManhattanPatternHeuristicTest {

	private Board board;
	
	private ManhattanPatternHeuristic heuristic;

	/**
	 * Good pattern tests
	 */
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new ManhattanPatternHeuristic(1);
	}

	@Test
	public void testLocalHane1and2() {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"...#O#.............",// 16
					"...................",// 15
					"...#O..............",// 14
					"...................",// 13
					"...................",// 12
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"...................",// 8
					"...................",// 7
					"...................",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"...................",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			board.play(at("g15"));
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("e15")));
			assertFalse(heuristic.getGoodMoves().contains(at("g15")));
	}
	
	@Test
	public void testLocalHane3() {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..#.#..............",// 16
					"..#................",// 15
					"..#O...............",// 14
					"...................",// 13
					"...................",// 12
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"...................",// 8
					"...................",// 7
					"...................",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"...................",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			board.play(at("g15"));
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
	}



}
