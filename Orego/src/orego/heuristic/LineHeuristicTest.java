package orego.heuristic;

import static org.junit.Assert.*;

import orego.core.Board;
import orego.core.Coordinates;

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
		heuristic.prepare(board, false);
		assertEquals(1, heuristic.evaluate(Coordinates.at(3,3), board));
		assertEquals(0, heuristic.evaluate(Coordinates.at(9,9), board));
		assertEquals(-3, heuristic.evaluate(Coordinates.at(0,0), board));
	}

}
