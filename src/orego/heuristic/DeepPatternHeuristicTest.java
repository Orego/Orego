package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;
import org.junit.Before;
import org.junit.Test;

public class DeepPatternHeuristicTest {
	
	private Board board;

	private DeepPatternHeuristic heuristic;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new DeepPatternHeuristic(1);
	}
	
	
	
	@Test
	public void testPattern() {
		String[] problem = new String[] { //
				"...................",// 19
				"...................",// 18
				"..O................",// 17
				"...#...............",// 16
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
		board.setUpProblem(BLACK, problem); //set up board with black to play
		board.play(at("d17")); //black plays at d18, c17 should be suggested by pattern tables (verify via gogui)
		heuristic.prepare(board,false,0);
		assertTrue(heuristic.getGoodMoves().contains(at("c16")));
	}
	

	@Test
	public void testSetProperty() throws Exception {
		heuristic = new DeepPatternHeuristic(1);
		
		assertEquals(orego.heuristic.DeepPatternHeuristic.DEFAULT_MAX_TREE_DEPTH, heuristic.getMaxTreeDepth());
		
		heuristic.setProperty("maxTreeDepth", "3");
		
		assertEquals(3, heuristic.getMaxTreeDepth());
		
	}

	/** does nothing - see testMaxDepth() in MctsPlayerTest
	 * (cannot test this in DeepPatternHeuristicTest because
	 * stuff we need is only available in different class)
	 */
	@Test
	public void testMaxTreeDepth() {
		assertEquals(1,1); // do nothing
	}

}
