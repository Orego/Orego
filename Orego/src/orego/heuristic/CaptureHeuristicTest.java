package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class CaptureHeuristicTest {

	private Board board;
	
	private CaptureHeuristic heuristic;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new CaptureHeuristic(1);
	}

	@Test
	public void testEvaluate() {
		String[] problem = new String[] {
					"...................",//19
					"...................",//18
					"...................",//17
					"...................",//16
					"...................",//15
					"...................",//14
					"...................",//13
					"...................",//12
					"...................",//11
					"...................",//10
					"...................",//9
					"...................",//8
					"...................",//7
					"..O................",//6
					".OOO...............",//5
					"OO#................",//4
					"#O#O...............",//3
					".#O#O..............",//2
					"#O................."//1
				  // ABCDEFGHJKLMNOPQRST
				};
		board.setUpProblem(WHITE, problem);
		heuristic.prepare(board);
		assertEquals(3, heuristic.evaluate(at("a2"), board));
	}

	@Test
	public void testMultipleNeighborsInSameGroup() {
		String[] problem = new String[] {
					"...................",//19
					"...................",//18
					"...................",//17
					"...................",//16
					"...................",//15
					"...................",//14
					"...................",//13
					"...................",//12
					"...................",//11
					"...................",//10
					"...................",//9
					"...................",//8
					"...................",//7
					"..O................",//6
					".OOO...............",//5
					"OO#O...............",//4
					"###O...............",//3
					".#O#O..............",//2
					"#O................."//1
				  // ABCDEFGHJKLMNOPQRST
				};
		board.setUpProblem(WHITE, problem);
		heuristic.prepare(board);
		assertEquals(6, heuristic.evaluate(at("a2"), board));
	}
	
	@Test
	public void testBestMove() {
		String[] problem = new String[] {
				"...................",//19
				"...................",//18
				"...................",//17
				"...................",//16
				"...................",//15
				"...................",//14
				"...................",//13
				"...................",//12
				".....O.............",//11
				"....O#O............",//10
				"....O#O...OOO......",//9
				".........O###......",//8
				"....O#O...OOO......",//7
				"....O#O............",//6
				".....O.............",//5
				"...................",//4
				"...................",//3
				"...................",//2
				"..................."//1
			  // ABCDEFGHJKLMNOPQRST
			};
		board.setUpProblem(WHITE, problem);
		heuristic.prepare(board);
		assertEquals(4, heuristic.evaluate(at("f8"), board));		
		assertEquals(3, heuristic.evaluate(at("o8"), board));
		assertEquals(at("f8"), heuristic.getBestMove());
	}

}
