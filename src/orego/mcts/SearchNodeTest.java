package orego.mcts;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import orego.util.*;

public class SearchNodeTest {

	private SearchNode node;

	@Before
	public void setUp() throws Exception {
		node = new SearchNode();
		node.reset(0L);
	}

	@Test
	public void testInitialValues() {
		assertEquals(2, node.getRuns(at("a3")));
		assertEquals(1, node.getWins(at("a3")), 0.001);
	}

	@Test
	public void testIsFresh() {
		assertTrue(node.isFresh());
		node.recordPlayout(1, new int[] { PASS }, 0, 1, new IntSet(
				FIRST_POINT_BEYOND_BOARD));
	}

	@Test
	public void testToString() {
		node.recordPlayout(1, new int[] { at("a1") }, 0, 1, new IntSet(
				FIRST_POINT_BEYOND_BOARD));
		node.recordPlayout(1, new int[] { PASS }, 0, 1, new IntSet(
				FIRST_POINT_BEYOND_BOARD));
		int base = (2 * BOARD_AREA) + 12;
		assertEquals(
				"Total runs: "
						+ base
						+ "\nA1:       2/      3 (0.6667)\nPASS:       2/     11 (0.1818)\n",
				node.toString());
	}

	@Test
	public void addWins() {
		node.addWins(at("b7"), 2);
		assertEquals(3, node.getWins(at("b7")), 0.001);
		assertEquals("B7 wins 3.0/4 = 0.75", node.bestWinCountReport());
	}

	@Test
	public void testOverallWinRate() {
		assertEquals((BOARD_AREA + 1.0) / ((2 * BOARD_AREA) + 10),
				node.overallWinRate(), 0.001);
		node.exclude(at("e3"));
		assertEquals((BOARD_AREA) / ((2 * BOARD_AREA) + 8.0),
				node.overallWinRate(), 0.001);
	}

	@Test
	public void testInitialWins() {
		node.reset(0L);
		assertEquals(1, node.getWins(FIRST_POINT_ON_BOARD), 0.001);
		assertEquals(2, node.getRuns(FIRST_POINT_ON_BOARD));
	}

	@Test
	public void testGetWinningMove() {
		assertEquals(NO_POINT, node.getWinningMove());
		node.recordPlayout(1, new int[] { at("a1") }, 0, 1, new IntSet(
				FIRST_POINT_BEYOND_BOARD));
		assertEquals(at("a1"), node.getWinningMove());
		node.recordPlayout(0, new int[] { at("a1") }, 0, 1, new IntSet(
				FIRST_POINT_BEYOND_BOARD));
		assertEquals(NO_POINT, node.getWinningMove());
	}

	@Test
	public void testTieUpdate() {
		node.reset(0L);
		node.recordPlayout(0.5, new int[] { at("a1") }, 0, 1, new IntSet(
				FIRST_POINT_BEYOND_BOARD));
		assertEquals(3, node.getRuns(at("a1")));
		assertEquals(1.5, node.getWins(at("a1")), 0.001);
		assertEquals(0.5, node.getWinRate(at("a1")), 0.001);
	}
}
