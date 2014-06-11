package edu.lclark.orego.mcts;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;

public class SearchNodeTest {

	private SearchNode node;

	private CoordinateSystem coords;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		coords = CoordinateSystem.forWidth(19);
		node = new SearchNode(coords);
		node.reset(0L, coords);
	}

	@Test
	public void testInitialValues() {
		assertEquals(2, node.getRuns(at("a3")));
		assertEquals(1, node.getWins(at("a3")), 0.001);
	}

	@Test
	public void testIsFresh() {
		assertTrue(node.isFresh(coords));
		node.recordPlayout(1, new short[] { PASS }, 0, 1, new ShortSet(
				coords.getFirstPointBeyondBoard()));
	}

	@Test
	public void testToString() {
		node.recordPlayout(1, new short[] { at("a1") }, 0, 1, new ShortSet(
				coords.getFirstPointBeyondBoard()));
		node.recordPlayout(1, new short[] { PASS }, 0, 1, new ShortSet(
				coords.getFirstPointBeyondBoard()));
		int base = (2 * coords.getArea()) + 12;
		assertEquals(
				"Total runs: "
						+ base
						+ "\nA1:       2/      3 (0.6667)\nPASS:       2/     11 (0.1818)\n",
				node.toString(coords));
	}

	@Test
	public void addWins() {
		node.addWins(at("b7"), 2);
		assertEquals(3, node.getWins(at("b7")), 0.001);
		assertEquals("B7 wins 3.0/4 = 0.75", node.bestWinCountReport(coords));
	}

	@Test
	public void testOverallWinRate() {
		assertEquals((coords.getArea() + 1.0) / ((2 * coords.getArea()) + 10),
				node.overallWinRate(coords), 0.001);
		node.exclude(at("e3"));
		assertEquals((coords.getArea()) / ((2 * coords.getArea()) + 8.0),
				node.overallWinRate(coords), 0.001);
	}

	@Test
	public void testInitialWins() {
		node.reset(0L, coords);
		assertEquals(1, node.getWins(at("h10")), 0.001);
		assertEquals(2, node.getRuns(at("h10")));
	}

	@Test
	public void testGetWinningMove() {
		assertEquals(NO_POINT, node.getWinningMove());
		node.recordPlayout(1, new short[] { at("a1") }, 0, 1, new ShortSet(
				coords.getFirstPointBeyondBoard()));
		assertEquals(at("a1"), node.getWinningMove());
		node.recordPlayout(0, new short[] { at("a1") }, 0, 1, new ShortSet(
				coords.getFirstPointBeyondBoard()));
		assertEquals(NO_POINT, node.getWinningMove());
	}

	@Test
	public void testTieUpdate() {
		node.reset(0L, coords);
		node.recordPlayout((float) 0.5, new short[] { at("a1") }, 0, 1, new ShortSet(
				coords.getFirstPointBeyondBoard()));
		assertEquals(3, node.getRuns(at("a1")));
		assertEquals(1.5, node.getWins(at("a1")), 0.001);
		assertEquals(0.5, node.getWinRate(at("a1")), 0.001);
	}

}
