package edu.lclark.orego.sgf;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class SgfParserTest {

	SgfParser parser;
	Board board;
	CoordinateSystem coords;
	
	@Before
	public void setUp() {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		parser = new SgfParser(coords);
	}
	
	/** Delegate method to call at on coords. */
	private short at(String label) {
		return coords.at(label);
	}

	@Test
	public void testSgfToPoint() {
		// Test conversion of sgf (and human-readable strings) to ints
		assertEquals(at("e15"), parser.sgfToPoint("ee"));
		assertEquals(at("t1"), parser.sgfToPoint("ss"));
		assertEquals(at("a19"), parser.sgfToPoint("aa"));
		assertEquals(at("t19"), parser.sgfToPoint("sa"));
		assertEquals(at("a1"), parser.sgfToPoint("as"));
	}

}
