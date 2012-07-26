package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class LinesOneTwoHeuristicTest {

	private Board board;
	private LinesOneTwoHeuristic heuristic;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new LinesOneTwoHeuristic();
	}

	@Test
	public void testEvaluate() {
		String[] problem = new String[] { //
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
				"...................",// 3
				"...................",// 2
				"..................."// 1
			  // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		assertEquals(-1, heuristic.evaluate(at("A1"), board));
		assertEquals(-1, heuristic.evaluate(at("S18"), board));
		assertEquals(-1, heuristic.evaluate(at("A10"), board));
		assertEquals(-1, heuristic.evaluate(at("B9"), board));
		assertEquals(-1, heuristic.evaluate(at("K19"), board));
		assertEquals(-1, heuristic.evaluate(at("M18"), board));
		assertEquals(-1, heuristic.evaluate(at("T11"), board));
	}

}
