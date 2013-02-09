package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.at;
import static orego.core.Colors.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.*;

public class AdjacentHeuristicTest {

	private Board board;
	
	private AdjacentHeuristic heuristic;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new AdjacentHeuristic(1);
	}
	
	@Test
	public void testClone() throws Exception {
		AdjacentHeuristic copy = heuristic.clone();
		assertFalse(copy == heuristic);
	}
	
	@Test
	public void testEvaluate() {
		board.play(at("d16"));
		board.play(at("e17"));
		heuristic.prepare(board);
		assertTrue(heuristic.getGoodMoves().contains(at("d17")));
		assertTrue(heuristic.getGoodMoves().contains(at("d15")));
		assertTrue(heuristic.getGoodMoves().contains(at("c16")));
		assertTrue(heuristic.getGoodMoves().contains(at("e16")));
	}
	
}
