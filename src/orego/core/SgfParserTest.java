package orego.core;

import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import static orego.core.SgfParser.*;

import org.junit.Before;
import org.junit.Test;

public class SgfParserTest {
	
	SgfParser parser;
	Board board;
	
	@Before
	public void setUp() {
		parser = new SgfParser();
		board = new Board();
		
	}

	@Test
	public void testSgfToPoint() {
		// Test conversion of sgf (and human-readable strings) to ints
		assertEquals(at("e15"), sgfToPoint("ee"));
		assertEquals(at("t1"), sgfToPoint("ss"));
		assertEquals(at("a19"), sgfToPoint("aa"));
		assertEquals(at("t19"), sgfToPoint("sa"));
		assertEquals(at("a1"), sgfToPoint("as"));
	}
	
}
