package orego.core;

import static orego.core.Colors.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class ColorsTest {

	@Test
	public void testIsAPlayerColor() {
		// Only BLACK and WHITE are player colors
		assertTrue(isAPlayerColor(BLACK));
		assertTrue(isAPlayerColor(WHITE));
		assertFalse(isAPlayerColor(VACANT));
		assertFalse(isAPlayerColor(OFF_BOARD_COLOR));
	}

	@Test
	public void testOpposite() {
		// BLACK and WHITE are opposites
		assertEquals(BLACK, opposite(WHITE));
		assertEquals(WHITE, opposite(BLACK));
	}

	@Test
	public void testColorToChar() {
		// Convert colors to chars
		assertEquals('#', colorToChar(BLACK));
		assertEquals('O', colorToChar(WHITE));
		assertEquals('.', colorToChar(VACANT));
		assertEquals('*', colorToChar(OFF_BOARD_COLOR));
		assertEquals('?', colorToChar(-1));
		assertEquals('?', colorToChar(4));
	}

	@Test
	public void testCharToColor() {
		// Convert chars to colors
		assertEquals(BLACK, charToColor('#'));
		assertEquals(WHITE, charToColor('O'));
		assertEquals(VACANT, charToColor('.'));
		assertEquals(OFF_BOARD_COLOR, charToColor('*'));
	}

	@Test
	public void testColorToString() {
		// Convert colors to Strings
		assertEquals("#", colorToString(BLACK));
		assertEquals("O", colorToString(WHITE));
		assertEquals(".", colorToString(VACANT));
		assertEquals("*", colorToString(OFF_BOARD_COLOR));
	}

}
