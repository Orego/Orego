package edu.lclark.orego.mcts;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;

public class RaveNodeTest {

	private RaveNode node;
	
	private CoordinateSystem coords;
	
	/** Delegate method to call at on player's board. */
	private short at(String label) {
		return coords.at(label);
	}
	
	@Before
	public void setUp() throws Exception {
		Board board = new Board(5);
		coords = board.getCoordinateSystem();
		node = new RaveNode(coords);
		node.clear(0L, coords);
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
		node.recordPlayout(1, new short[] {at("a1"), at("b2"), at("a2")}, 0, 3, new ShortSet(coords.getFirstPointBeyondBoard()));
		assertEquals(3, node.getRaveRuns(at("a1")), 0.001);
		assertEquals(2, node.getRaveWins(at("a1")), 0.001);
		assertEquals(3, node.getRaveRuns(at("a2")), 0.001);
		assertEquals(2, node.getRaveWins(at("a2")), 0.001);
	}
	
	@Test
	public void testRecordTiedPlayout() {
		node.recordPlayout((float) 0.5, new short[] {at("a1"), at("b2"), at("a2")}, 0, 3, new ShortSet(coords.getFirstPointBeyondBoard()));
		assertEquals(3, node.getRaveRuns(at("a1")), 0.001);
		assertEquals(1.5, node.getRaveWins(at("a1")), 0.001);
		assertEquals(3, node.getRaveRuns(at("a2")), 0.001);
		assertEquals(1.5, node.getRaveWins(at("a2")), 0.001);
	}
	
	@Test
	public void testToString() {
		node.recordPlayout((float) 0.5, new short[] {at("a1"), at("b2"), at("a2")}, 0, 3, new ShortSet(coords.getFirstPointBeyondBoard()));
		node.toString();
		// The toString() function should not crash. (previous versions had it crash due to format not working right)
	}
	
	@Test
	public void testBug1() {
		// The -2 below means we won't try to play the very last point, which would be suicide.
		short[] moves = new short[coords.getArea() * 2 - 2];
		// Addresses bug where playedMoves would overflow
		int i = 0;
		for (short p : coords.getAllPointsOnBoard()) {
			if (i < moves.length) {
				moves[i] = p;
				i++;
				moves[i] = p;
				i++;
			}
		}
		node.recordPlayout((float) 0.5, moves, 0, moves.length, new ShortSet(coords.getFirstPointBeyondBoard()));		
		// If no exception has been thrown, we're good.
	}

}
