package orego.mcts;

import static org.junit.Assert.*;
import static orego.core.Coordinates.*;

import orego.util.IntSet;

import org.junit.Before;
import org.junit.Test;

public class RaveNodeTest {

	private RaveNode node;
	
	@Before
	public void setUp() throws Exception {
		node = new RaveNode();
		node.reset(0L);
	}

	@Test
	public void testInitialValues() {
		assertEquals(2, node.getRaveRuns(at("a1")), 0.001);
		assertEquals(1, node.getRaveWins(at("a1")), 0.001);
	}
	
	@Test
	public void testAddRaveWin() {
		node.addRaveWin(at("a1"));
		assertEquals(3, node.getRaveRuns(at("a1")), 0.001);
		assertEquals(2, node.getRaveWins(at("a1")), 0.001);
		assertEquals(2.0/3.0, node.getRaveWinRate(at("a1")), 0.001);
	}
	
	@Test
	public void testAddRaveLoss() {
		node.addRaveLoss(at("a1"));
		assertEquals(3, node.getRaveRuns(at("a1")), 0.001);
		assertEquals(1, node.getRaveWins(at("a1")), 0.001);
		assertEquals(1.0/3.0, node.getRaveWinRate(at("a1")), 0.001);
	}
	
	@Test
	public void testRecordPlayout() {
		node.recordPlayout(1, new int[] {at("a1"), at("b2"), at("a2")}, 0, 3, new IntSet(getFirstPointBeyondBoard()));
		assertEquals(3, node.getRaveRuns(at("a1")), 0.001);
		assertEquals(2, node.getRaveWins(at("a1")), 0.001);
		assertEquals(3, node.getRaveRuns(at("a2")), 0.001);
		assertEquals(2, node.getRaveWins(at("a2")), 0.001);
	}
	
	@Test
	public void testRecordTiedPlayout() {
		node.recordPlayout((float) 0.5, new int[] {at("a1"), at("b2"), at("a2")}, 0, 3, new IntSet(getFirstPointBeyondBoard()));
		assertEquals(3, node.getRaveRuns(at("a1")), 0.001);
		assertEquals(1.5, node.getRaveWins(at("a1")), 0.001);
		assertEquals(3, node.getRaveRuns(at("a2")), 0.001);
		assertEquals(1.5, node.getRaveWins(at("a2")), 0.001);
	}
	
	@Test
	public void testToString() {
		node.recordPlayout((float) 0.5, new int[] {at("a1"), at("b2"), at("a2")}, 0, 3, new IntSet(getFirstPointBeyondBoard()));
		node.toString();
		// The toString() function should not crash. (previous versions had it crash due to format not working right)
	}
}
