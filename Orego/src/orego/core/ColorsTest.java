package orego.core;

import static orego.core.Colors.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ColorsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testIsAPlayerColor() {
		assertTrue(isAPlayerColor(BLACK));
		assertTrue(isAPlayerColor(WHITE));
		assertFalse(isAPlayerColor(VACANT));
		assertFalse(isAPlayerColor(OFF_BOARD_COLOR));
	}

	@Test
	public void testOpposite() {
		assertEquals(BLACK, opposite(WHITE));
		assertEquals(WHITE, opposite(BLACK));
	}

	@Test
	public void testColorToChar() {
		assertEquals('#', colorToChar(BLACK));
		assertEquals('O', colorToChar(WHITE));
		assertEquals('.', colorToChar(VACANT));
		assertEquals('*', colorToChar(OFF_BOARD_COLOR));
		assertEquals('?', colorToChar(-1));
	}

	@Test
	public void testCharToColor() {
		assertEquals(BLACK, charToColor('#'));
		assertEquals(WHITE, charToColor('O'));
		assertEquals(VACANT, charToColor('.'));
		assertEquals(OFF_BOARD_COLOR, charToColor('*'));
	}

	@Test
	public void testColorToString() {
		assertEquals("#", colorToString(BLACK));
		assertEquals("O", colorToString(WHITE));
		assertEquals(".", colorToString(VACANT));
		assertEquals("*", colorToString(OFF_BOARD_COLOR));
	}

}
