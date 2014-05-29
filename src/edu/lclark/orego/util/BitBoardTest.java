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
	public void testClear(){
		bitBoard.set(at("b3"));
		bitBoard.clear();
		assertFalse(bitBoard.get(at("b3")));
	}

}
