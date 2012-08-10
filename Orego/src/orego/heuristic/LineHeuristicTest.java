package orego.heuristic;

import static org.junit.Assert.*;
import static orego.core.Coordinates.*;
import orego.core.Board;
import org.junit.Before;
import org.junit.Test;

public class LineHeuristicTest {

	private Board board;
	
	private LineHeuristic heuristic;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new LineHeuristic(1);
	}

	@Test
	public void testEvaluate() {
		heuristic.prepare(board);
		assertTrue(heuristic.getBadMoves().contains(at("a1")));
		assertTrue(heuristic.getBadMoves().contains(at("c2")));
		assertFalse(heuristic.getBadMoves().contains(at("c3")));
	}
	
	@Test
	public void testClone() {
		LineHeuristic copy = heuristic.clone();
		
		assertNotSame(heuristic, copy);
	}

}
