package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class ProximityHeuristicTest {

	private Board board;
	
	private ProximityHeuristic heuristic;
	
	private MersenneTwisterFast random;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new ProximityHeuristic(1);
		random = new MersenneTwisterFast();
	}

	@Test
	public void testEvaluate() {
		board.play("j10");
		heuristic.prepare(board, random);
		assertEquals(1, heuristic.evaluate(at("j13"), board));
		assertEquals(1, heuristic.evaluate(at("h12"), board));
		assertEquals(1, heuristic.evaluate(at("j12"), board));
		assertEquals(1, heuristic.evaluate(at("k12"), board));
		assertEquals(1, heuristic.evaluate(at("l11"), board));
		assertEquals(1, heuristic.evaluate(at("k8"), board));
		assertEquals(1, heuristic.evaluate(at("l9"), board));
		assertEquals(1, heuristic.evaluate(at("j13"), board));
		assertEquals(0, heuristic.evaluate(at("g12"), board));
		assertEquals(0, heuristic.evaluate(at("k13"), board));
		assertEquals(0, heuristic.evaluate(at("h13"), board));
		assertEquals(0, heuristic.evaluate(at("n10"), board));
		assertEquals(0, heuristic.evaluate(at("k14"), board));
		assertEquals(0, heuristic.evaluate(at("f8"), board));
		assertEquals(0, heuristic.evaluate(at("k13"), board));
		int sum = 0;
		for (int p : ALL_POINTS_ON_BOARD) {
			if (p != at("j10")) {
				sum += heuristic.evaluate(p, board);
			}
		}
		assertEquals(24, sum);
	}

	@Test
	public void testNearCorner() {
		board.play("b3");
		heuristic.prepare(board, random);
		int sum = 0;
		for (int p : ALL_POINTS_ON_BOARD) {
			if (p != at("b3")) {
				sum += heuristic.evaluate(p, board);
			}
		}
		assertEquals(19, sum);
	}

}
