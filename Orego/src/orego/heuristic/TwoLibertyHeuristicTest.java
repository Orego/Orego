package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class TwoLibertyHeuristicTest {

	private Board board;

	private TwoLibertyHeuristic heuristic;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new TwoLibertyHeuristic(2);
	}

	@Test
	public void testEvaluate1() {
		String[] problem = new String[] { //
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"......###..........",// 13
				".......OO..........",// 12
				"......####.........",// 11
				"...................",// 10
				"...................",// 9
				"......##...........",// 8
				"......OO...........",// 7
				".....###...........",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"OO#................"// 1
		//       ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		heuristic.prepare(board, false);
		assertEquals(2, heuristic.evaluate(at("b2"), board));
		assertEquals(0, heuristic.evaluate(at("j7"), board));
		assertEquals(0, heuristic.evaluate(at("f7"), board));
		assertEquals(2, heuristic.evaluate(at("k12"), board));
		assertEquals(0, heuristic.evaluate(at("g12"), board));
		board.clear();
		board.setUpProblem(WHITE, problem);
		assertEquals(2, heuristic.evaluate(at("b2"), board));
		assertEquals(0, heuristic.evaluate(at("j7"), board));
		assertEquals(0, heuristic.evaluate(at("f7"), board));
		assertEquals(2, heuristic.evaluate(at("k12"), board));
		assertEquals(0, heuristic.evaluate(at("g12"), board));
	}

	@Test
	public void testEvaluate2() {
		String[] problem = new String[] { //
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"........#.#........",// 11
				".......#OO#........",// 10
				".........#.........",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		//       ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		heuristic.prepare(board, false);
		assertEquals(0, heuristic.evaluate(at("k11"), board));
		assertEquals(2, heuristic.evaluate(at("j9"), board));
		board.clear();
		board.setUpProblem(WHITE, problem);
		assertEquals(0, heuristic.evaluate(at("k11"), board));
		assertEquals(2, heuristic.evaluate(at("j9"), board));
	}
	
	@Test
	public void testEvaluate3() {
		String[] problem = new String[] { //
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				".....######........",// 11
				".....##OOOO#.......",// 10
				".....#.OOOO#.......",// 9
				".......OOOO#.......",// 8
				".......####........",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		//       ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		heuristic.prepare(board, false);
		assertEquals(12, heuristic.evaluate(at("g8"), board));
		assertEquals(0, heuristic.evaluate(at("g9"), board));
		board.clear();
		board.setUpProblem(WHITE, problem);
		assertEquals(12, heuristic.evaluate(at("g8"), board));
		assertEquals(0, heuristic.evaluate(at("g9"), board));
	}
	
	@Test
	public void testEvaluate4() {
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
				"...............####",// 4
				"...............#OO.",// 3
				"...............#O#.",// 2
				"...............#..."// 1
		//       ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		heuristic.prepare(board, false);
		assertEquals(3, heuristic.evaluate(at("r1"), board));
		board.clear();
		board.setUpProblem(WHITE, problem);
		assertEquals(0, heuristic.evaluate(at("r1"), board));

	}
}
