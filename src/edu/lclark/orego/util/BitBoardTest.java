package edu.lclark.orego.util;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.util.TestingTools.*;

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
		String[] correct = {
				".....",
				"..X..",
				".XXX.",
				"..X..",
				".....",
		};
		assertEquals(asOneString(correct), bitBoard.toString());
		bitBoard.clear();
		bitBoard.set(at("a2"));
		bitBoard.set(at("e5"));
		bitBoard.expand();
		correct = new String[] {
				"...XX",
				"....X",
				"X....",
				"XX...",
				"X....",		
		};
		assertEquals(asOneString(correct), bitBoard.toString());
		bitBoard.expand();
		correct = new String[] {
				"..XXX",
				"X..XX",
				"XX..X",
				"XXX..",
				"XX...",		
		};
		assertEquals(asOneString(correct), bitBoard.toString());
	}

}
