package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
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
		heuristic = new SpecificPointHeuristic(at("g6"));
	}

	@Test
	public void testEvaluate() {
		assertEquals(1, heuristic.evaluate(at("g6"), board));
		assertEquals(0, heuristic.evaluate(at("a1"), board));

	}

	@Test
	public void testSaveMultipleStones() {
		heuristic = new SpecificPointHeuristic();
		assertEquals(1, heuristic.evaluate(at("a1"), board));
		assertEquals(0, heuristic.evaluate(at("g6"), board));
	}

}
