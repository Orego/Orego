package orego.heuristic;

import static orego.core.Colors.WHITE;
import orego.core.Coordinates;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import orego.core.Board;
import orego.play.UnknownPropertyException;

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
		heuristic.setMaxVacancies(19*19);
		heuristic.prepare(board);
		assertFalse(heuristic.getGoodMoves().contains(at("b2")));
		assertFalse(heuristic.getGoodMoves().contains(at("r17")));
		assertTrue(heuristic.getGoodMoves().contains(at("q2")));
		assertTrue(heuristic.getGoodMoves().contains(at("k8")));
	}
	
	@Test
	public void testSetProperty() throws UnknownPropertyException {
		heuristic.setProperty("threshold", "5.13");
		assertTrue(heuristic.threshold == 5.13);
		heuristic.setProperty("maxVacancies", "7237");
		assertTrue(heuristic.maxVacancies == 7237);
		heuristic.setProperty("neighborhood", "knight");
		assertTrue(heuristic.neighborhood == Coordinates.KNIGHT_NEIGHBORHOOD);
	}

}
