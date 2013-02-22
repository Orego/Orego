package orego.heuristic;

import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class DisputedHeuristicTest {

	private Board board;
	
	private DisputedHeuristic heuristic;
		
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new DisputedHeuristic(1);
	}

	@Test
	public void testClone() throws Exception {
		DisputedHeuristic copy = heuristic.clone();
		
		// should be a new instance
		assertFalse(copy == heuristic);
	}
	
	@Test
	public void testEvaluate() {
		String[] problem = new String[] {
					"...................",//19
					"..............O.O..",//18
					".............#O....",//17
					".............#OOOO.",//16
					"................#..",//15
					"...................",//14
					"...................",//13
					"...................",//12
					"...................",//11
					"...................",//10
					"........OO.........",//9
					"........#..........",//8
					"........#..........",//7
					"...................",//6
					"...................",//5
					".#O................",//4
					"..##..........###O.",//3
					"...#O...........OO.",//2
					"...#..........OO..."//1
				  // ABCDEFGHJKLMNOPQRST
				};
		board.setUpProblem(WHITE, problem);
		heuristic.prepare(board);
		assertFalse(heuristic.getGoodMoves().contains(at("b2")));
		assertFalse(heuristic.getGoodMoves().contains(at("r17")));
		assertTrue(heuristic.getGoodMoves().contains(at("q2")));
		assertTrue(heuristic.getGoodMoves().contains(at("k8")));
	}

}
