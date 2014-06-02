package edu.lclark.orego.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;

public class BitBoardTest {
	
	private CoordinateSystem coords;
	
	private BitBoard bitBoard;
	
	// TODO Should we put this in one central place?
	/**
	 * Returns a single String made by concatenating all the strings in diagram.
	 */
	private static String asOneString(String[] diagram) {
		String result = "";
		for (String s : diagram) {
			result += s + "\n";
		}
		return result;
	}

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
	
	@Test
	public void testRemove(){
		bitBoard.set(at("c2"));
		bitBoard.set(at("c3"));
		bitBoard.set(at("c4"));
		bitBoard.remove(at("c3"));
		String[] correct = {
				".....",
				"..X..",
				".....",
				"..X..",
				".....",
		};
		assertEquals(asOneString(correct), bitBoard.toString());
	}

}
