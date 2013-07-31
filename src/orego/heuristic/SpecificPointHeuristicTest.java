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
		heuristic = new SpecificPointHeuristic(7);
	}

	@Test
	public void testEvaluate() {
		heuristic.prepare(board);
		assertTrue(heuristic.getGoodMoves().contains(at("c5")));
		assertEquals(1, heuristic.getGoodMoves().size());
	}
	
	@Test
	public void testSetProperty()  throws Exception {
		heuristic.setProperty("specificPoint", "12");
		assertEquals(12, heuristic.getSpecificPoint());
	}
	
	@Test
	public void testClone() throws Exception {
		SpecificPointHeuristic clone = heuristic.clone();
		clone.setProperty("specificPoint", "19");
		
		// should be a different object
		assertFalse(clone == heuristic);
		assertFalse(clone.getGoodMoves() == heuristic.getGoodMoves());
		
		assertTrue(clone.getSpecificPoint() == 19);
		
		
		assertEquals(7, clone.getWeight());
		
	}

}
