package orego.heuristic;

import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class SpecificPointHeuristicTest {

	private Board board;

	private SpecificPointHeuristic heuristic;

	private MersenneTwisterFast random;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new SpecificPointHeuristic(1);
		random = new MersenneTwisterFast();
	}

	@Test
	public void testEvaluate() {
		board.play(at("a1"));
		board.play(at("a2"));
		heuristic.prepare(board, random);
		assertEquals(2, heuristic.evaluate(at("c5"), board));
		assertEquals(0, heuristic.evaluate(at("c4"), board));
	}

}
