package orego.heuristic;

import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import orego.core.Board;
import org.junit.Before;
import org.junit.Test;

public class SpecificPointHeuristicTest {

	private Board board;

	private SpecificPointHeuristic heuristic;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new SpecificPointHeuristic(1);
	}

	@Test
	public void testEvaluate() {
		heuristic.prepare(board);
		assertTrue(heuristic.getGoodMoves().contains(at("c5")));
		assertEquals(1, heuristic.getGoodMoves().size());
		assertEquals(0, heuristic.getBadMoves().size());
	}

}
