package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class LearnedPatternHeuristicTest {

	private Board board;
	
	private LearnedPatternHeuristic heuristic;

	/**
	 * Good pattern tests
	 */
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new LearnedPatternHeuristic(1);
	}

	@Test
	public void testLocalHane1and2() {
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
					"...................",// 8
					"...................",// 7
					"...................",// 6
					"...................",// 5
					"...................",// 4
					".................#O",// 3
					"................#.#",// 2
					"................###"// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			board.play(at("r3"));
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("s2")));
	}
	
	@Test
	public void testClone() {
		LearnedPatternHeuristic copy = heuristic.clone();
		
		assertNotSame(heuristic, copy);
	}
}
