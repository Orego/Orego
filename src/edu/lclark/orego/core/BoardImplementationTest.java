package edu.lclark.orego.core;

import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.StoneColor.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class BoardImplementationTest {

	private BoardImplementation board;

	@Before
	public void setUp() throws Exception {
		board = new BoardImplementation(5);
	}

	@Test
	public void testSimplePlay() {
		short a2 = board.at("a2");
		short b3 = board.at("b3");
		assertEquals(OK, board.play(a2));
		assertEquals(OK, board.play(b3));
		assertEquals(BLACK, board.getColorAt(a2));
		assertEquals(WHITE, board.getColorAt(b3));
	}

}
