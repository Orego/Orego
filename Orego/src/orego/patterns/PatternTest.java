package orego.patterns;

import static org.junit.Assert.*;
import static orego.core.Colors.*;
import static orego.heuristic.AbstractPatternHeuristic.isPossibleNeighborhood;

import org.junit.Before;
import org.junit.Test;
import static orego.patterns.Pattern.*;

public class PatternTest {

	private Pattern pattern;

	@Before
	public void setUp() {
		pattern = new ColorSpecificPattern("????????", BLACK);
	}

	@Test
	public void testNeighborhoodToDiagram() {
		assertEquals("##.\nO .\n***",
				neighborhoodToDiagram(diagramToNeighborhood("##.\nO .\n***")));
	}

	@Test
	public void testCountsAsInAnyOrientation() {
		pattern.setColors(".O..#...", BLACK);
		assertTrue(pattern
				.countsAsInAnyOrientation(diagramToNeighborhood(".O#\n. .\n...")));
	}

	@Test
	public void testPatternPrinter() {
		pattern.patternPrinter();
	}

	@Test
	public void testArrayToString() {
		char p = diagramToNeighborhood("***\n* O\n*#.");
		int[] n = Pattern.neighborhoodToArray(p);
		String string = neighborhoodToDiagram(arrayToNeighborhood(n));
		assertEquals("***\n* O\n*#.", string);
		assertEquals("**O#***.", arrayToString(n));
	}

	@Test
	public void testArrayToNeighborhood() {
		for (int p = Character.MIN_VALUE; p <= Character.MAX_VALUE; p++) {
			if (isPossibleNeighborhood((char) p)) {
				int[] n = Pattern.neighborhoodToArray((char) p);
				char i = Pattern.arrayToNeighborhood(n);
				assertEquals(i, p);
			}
		}
	}

	@Test
	public void testRotate90() {
		assertArrayEquals(new int[] { 1, 3, 0, 2, 6, 4, 7, 5 },
				pattern.rotate90(new int[] { 0, 1, 2, 3, 4, 5, 6, 7 }));
	}

	@Test
	public void testReflect() {
		assertArrayEquals(new int[] { 0, 2, 1, 3, 5, 4, 7, 6 },
				pattern.reflect(new int[] { 0, 1, 2, 3, 4, 5, 6, 7 }));
	}

	@Test
	public void testCountsAs() {
		assertTrue(pattern.countsAs(BLACK, BLACK));
		assertFalse(pattern.countsAs(BLACK, WHITE));
		assertFalse(pattern.countsAs(BLACK, VACANT));
		assertFalse(pattern.countsAs(BLACK, OFF_BOARD_COLOR));
		assertFalse(pattern.countsAs(WHITE, BLACK));
		assertTrue(pattern.countsAs(WHITE, WHITE));
		assertFalse(pattern.countsAs(WHITE, VACANT));
		assertFalse(pattern.countsAs(WHITE, OFF_BOARD_COLOR));
		assertFalse(pattern.countsAs(VACANT, BLACK));
		assertFalse(pattern.countsAs(VACANT, WHITE));
		assertTrue(pattern.countsAs(VACANT, VACANT));
		assertFalse(pattern.countsAs(VACANT, OFF_BOARD_COLOR));
		assertFalse(pattern.countsAs(OFF_BOARD_COLOR, BLACK));
		assertFalse(pattern.countsAs(OFF_BOARD_COLOR, WHITE));
		assertFalse(pattern.countsAs(OFF_BOARD_COLOR, VACANT));
		assertTrue(pattern.countsAs(OFF_BOARD_COLOR, OFF_BOARD_COLOR));
		assertTrue(pattern.countsAs(IGNORE_COLOR, BLACK));
		assertTrue(pattern.countsAs(IGNORE_COLOR, WHITE));
		assertTrue(pattern.countsAs(IGNORE_COLOR, VACANT));
		assertFalse(pattern.countsAs(IGNORE_COLOR, OFF_BOARD_COLOR));
		assertFalse(pattern.countsAs(NOT_BLACK, BLACK));
		assertTrue(pattern.countsAs(NOT_BLACK, WHITE));
		assertTrue(pattern.countsAs(NOT_BLACK, VACANT));
		assertFalse(pattern.countsAs(NOT_BLACK, OFF_BOARD_COLOR));
		assertTrue(pattern.countsAs(NOT_WHITE, BLACK));
		assertFalse(pattern.countsAs(NOT_WHITE, WHITE));
		assertTrue(pattern.countsAs(NOT_WHITE, VACANT));
		assertFalse(pattern.countsAs(NOT_WHITE, OFF_BOARD_COLOR));
	}

}
