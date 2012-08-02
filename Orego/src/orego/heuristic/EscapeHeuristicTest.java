package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class EscapeHeuristicTest {

	private Board board;

	private EscapeHeuristic heuristic;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new EscapeHeuristic(1);
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
				"..............O##..",// 3
				"..............O#O..",// 2
				"...............O..."// 1
              // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.play(at("s2"));
		heuristic.prepare(board);
		assertEquals(1, heuristic.evaluate(at("r1"), board));
	}

	@Test
	public void testSaveMultipleStones() {
		String[] problem = new String[] { 
				"...................",// 19
				"...................",// 18
				".....O.............",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"............#......",// 12
				"..........#O#......",// 11
				"..........#O#......",// 10
				"..........#O#......",// 9
				"..........#O#......",// 8
				"...........#.......",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		heuristic.prepare(board);
		assertEquals(4, heuristic.evaluate(at("m12"), board));
	}
	
	@Test
	public void testSaveMultipleStones2() {
		String[] problem = new String[] { 
				"...................",// 19
				"...................",// 18
				".....O.............",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"..........#O#......",// 11
				"..........#O#......",// 10
				"..........#O#......",// 9
				"..........#O#......",// 8
				"...........#.......",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		heuristic.prepare(board);
		assertEquals(4, heuristic.evaluate(at("m12"), board));
	}
	
	@Test
	public void testConnectGroups() {
		String[] problem = new String[] { 
				"...................",// 19
				"...........O.......",// 18
				".....O.....O.......",// 17
				"..........#O#......",// 16
				"..........#O#......",// 15
				"..........#O#......",// 14
				"..........#O#......",// 13
				"..........#.#......",// 12
				"..........#O#......",// 11
				"..........#O#......",// 10
				"..........#O#......",// 9
				"..........#O#......",// 8
				"...........#.......",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		heuristic.prepare(board);
		assertEquals(4, heuristic.evaluate(at("m12"), board));
	}
	
	@Test
	public void testConnectGroups2() {
		String[] problem = new String[] { 
				"...................",// 19
				"...........O.......",// 18
				".....O.....O.......",// 17
				"..........#O#......",// 16
				"..........#O#......",// 15
				"..........#O#......",// 14
				"..........#O#......",// 13
				"............#......",// 12
				"..........#O#......",// 11
				"..........#O#......",// 10
				"..........#O#......",// 9
				"..........#O#......",// 8
				"...........#.......",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		heuristic.prepare(board);
		assertEquals(4, heuristic.evaluate(at("m12"), board));
	}
	
	@Test
	public void testConnectGroups3() {
		String[] problem = new String[] { 
				"...................",// 19
				"...........O.......",// 18
				".....O.....O.......",// 17
				"..........#O#......",// 16
				"..........#O#......",// 15
				"..........#O#......",// 14
				"..........#O####...",// 13
				"............OOOO#..",// 12
				"..........#O.###...",// 11
				"..........#O#......",// 10
				"..........#O#......",// 9
				"..........#O#......",// 8
				"...........#.......",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.play("n11");
		heuristic.prepare(board);
		assertEquals(8, heuristic.evaluate(at("m12"), board));
	}

	@Test
	public void testConnectGroups4() {
		String[] problem = new String[] { 
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				".........O#........",// 12
				"...######O#........",// 11
				"...#OOOOO.O#.......",// 10
				"...#O#####O#.......",// 9
				"...#O#...#O#.......",// 8
				"...#O#####O#.......",// 7
				"...#O#...#O#.......",// 6
				"...#O#####O#.......",// 5
				"...#OOOOOOO#.......",// 4
				"....#######........",// 3
				"...................",// 2
				"..................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		heuristic.prepare(board);
		assertEquals(23, heuristic.evaluate(at("k10"), board));
	}
	
	@Test
	public void testCapture() {
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
				"..............#O...",// 5
				"..............O#...",// 4
				"...............O...",// 3
				"...................",// 2
				"..................."// 1
			  // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.play("p3");
		heuristic.prepare(board);
		assertEquals(1, heuristic.evaluate(at("o4"), board));
		assertEquals(2, heuristic.evaluate(at("r4"), board));
		// Do it some additional times to catch "memory leak"
		assertEquals(2, heuristic.evaluate(at("r4"), board));
		assertEquals(2, heuristic.evaluate(at("r4"), board));
		assertEquals(2, heuristic.evaluate(at("r4"), board));
		assertEquals(2, heuristic.evaluate(at("r4"), board));
		assertEquals(2, heuristic.evaluate(at("r4"), board));
	}

	@Test
	public void testSaveLargerGroup() {
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
				"............#O#O#..",// 5
				"...........#OO#O#..",// 4
				"............#O.O#..",// 3
				".............#.#...",// 2
				"..................."// 1
			  // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.play("p3");
		heuristic.prepare(board);
		assertEquals(at("o6"), heuristic.getBestMove());
	}

	@Test
	public void testValuesClearedFromPreviousTurn() {
		String[] problem = new String[] { 
				"...................",// 19
				"...........O.......",// 18
				".....O.....O.......",// 17
				"..........#O#......",// 16
				"..........#O#......",// 15
				"..........#O#......",// 14
				"..........#O####...",// 13
				"............OOOO#..",// 12
				"..........#O.###...",// 11
				"..........#O#......",// 10
				"..........#O#......",// 9
				"..........#O#......",// 8
				"...........#.......",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.play("n11");
		heuristic.prepare(board);
		assertEquals(8, heuristic.evaluate(at("m12"), board));		
		heuristic.prepare(board);
		assertEquals(8, heuristic.evaluate(at("m12"), board));		
	}

	@Test
	public void testAvoidMultipleCountOfSameTarget() {
		String[] problem = new String[] { 
				"...................",// 19
				"...........O.......",// 18
				".....O.....O.......",// 17
				"..........#O#......",// 16
				"..........#O#......",// 15
				"..........#O#......",// 14
				"..........#O####...",// 13
				"............OOOO#..",// 12
				"...........O.###O..",// 11
				"...........O#.OO...",// 10
				"...........O#O.....",// 9
				"...........O#O.....",// 8
				"...........#O......",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.play("n11");
		heuristic.prepare(board);
		assertEquals(4, heuristic.evaluate(at("m12"), board));		
		assertEquals(11, heuristic.evaluate(at("o10"), board));		
	}

}
