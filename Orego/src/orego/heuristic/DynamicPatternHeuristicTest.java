package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class DynamicPatternHeuristicTest {
	
	private Board board;
	
	private DynamicPatternHeuristic heuristic;

	private MersenneTwisterFast random;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		DynamicPatternHeuristic.setTestMode(true);
		heuristic = new DynamicPatternHeuristic(1);
		random = new MersenneTwisterFast();
	}

	@Test
	public void testEvaluate() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"...................",// 16
					"...................",// 15
					"...................",// 14
					"...................",// 13
					"...................",// 12
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"O#O.............##O",// 8
					"#.#.............#.#",// 7
					"###..............#O",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"...................",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			heuristic.prepare(board, random);
			assertEquals(1, heuristic.evaluate(at("b7"), board));
			assertEquals(1, heuristic.evaluate(at("s7"), board));
			assertEquals(0, heuristic.evaluate(at("k16"), board));
		}
		DynamicPatternHeuristic.setTestMode(false);
	}

}
