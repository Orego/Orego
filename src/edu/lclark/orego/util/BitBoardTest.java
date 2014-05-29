package edu.lclark.orego.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;

public class BitBoardTest {
	
	private CoordinateSystem coords;
	
	private BitBoard bitBoard;
	
	@Before
	public void setUp() throws Exception {
		coords = CoordinateSystem.forWidth(5);
		bitBoard = new BitBoard(coords);
	}

	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}

	@Test
	public void testSet() {
		bitBoard.set(at("b3"));
		assertTrue(bitBoard.get(at("b3")));
		assertFalse(bitBoard.get(at("a2")));
		bitBoard.set(at("a2"));
		assertTrue(bitBoard.get(at("b3")));
		assertTrue(bitBoard.get(at("a2")));
	}
	
	@Test
	public void testClear() {
		bitBoard.set(at("b3"));
		bitBoard.clear();
		assertFalse(bitBoard.get(at("b3")));
	}
	
	@Test
	public void testExpand() {
		bitBoard.set(at("c3"));
		bitBoard.expand();
		assertEquals("00000\n00100\n01110\n00100\n00000\n", bitBoard.toString());
		
		bitBoard.clear();
		bitBoard.set(at("a2"));
		bitBoard.set(at("e5"));
		bitBoard.expand();
		assertEquals("00011\n00001\n10000\n11000\n10000\n", bitBoard.toString());
		bitBoard.expand();
		assertEquals("00111\n10011\n11001\n11100\n11000\n", bitBoard.toString());
	}
	
	@Test
	public void testToString() {
		String after = "00000\n00000\n10000\n00000\n00000\n";
		bitBoard.set(at("a3"));
		assertEquals(after, bitBoard.toString());
	}

}
