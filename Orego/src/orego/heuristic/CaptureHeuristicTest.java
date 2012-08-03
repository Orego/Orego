package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

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
		assertTrue(heuristic.getGoodMoves().contains(at("a2")));
		assertTrue(heuristic.getGoodMoves().contains(at("d1")));
		assertTrue(heuristic.getGoodMoves().contains(at("d4")));
		assertEquals(3, heuristic.getGoodMoves().size());
	}

}
