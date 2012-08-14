package orego.core;

import static org.junit.Assert.*;
import static orego.core.Coordinates.*;
import org.junit.Test;

public class CoordinatesTest {

	@Test
	public void testAt() {
		assertEquals(at("b19"), at(0, 1));
		assertEquals(PASS, at("PASS"));
		assertEquals(RESIGN, at("RESIGN"));
	}

	@Test
	public void testRowAndCol() {
		int p = at(5, 3);
		assertEquals(5, row(p));
		assertEquals(3, column(p));
	}

	@Test
	public void testRowAndColumnToString() {
			int p = at("p14");
			assertEquals("14", rowToString(row(p)));
			assertEquals("P", columnToString(column(p)));
	}
	
	@Test 
	public void testRowAndColumnToChar() {
			int p = at("p14");
			assertEquals('n', rowToChar(row(p)));
			assertEquals('o', columnToChar(column(p)));
			p = at("a1");
			assertEquals('a', rowToChar(row(p)));
			assertEquals('a', columnToChar(column(p)));
			p = at("t19");
			assertEquals('s', rowToChar(row(p)));
			assertEquals('s', columnToChar(column(p)));
	}

	@Test
	public void testIs3or4() {
			// Moves should be legal if 3 or 4 from both edges
			assertFalse(THIRD_OR_FOURTH_LINE[at("a1")]);
			assertTrue(THIRD_OR_FOURTH_LINE[at("e4")]);
			assertFalse(THIRD_OR_FOURTH_LINE[at("b2")]);
			assertFalse(THIRD_OR_FOURTH_LINE[at("j9")]);
			assertTrue(THIRD_OR_FOURTH_LINE[at("c4")]);
			assertFalse(THIRD_OR_FOURTH_LINE[at("a7")]);
			assertTrue(THIRD_OR_FOURTH_LINE[at("q4")]);
			assertTrue(THIRD_OR_FOURTH_LINE[at("q17")]);
			assertTrue(THIRD_OR_FOURTH_LINE[at("r16")]);
			assertFalse(THIRD_OR_FOURTH_LINE[at("t7")]);
	}

	@Test
	public void testIsOnBoard() {
			int p = at("a1");
			assertTrue(ON_BOARD[p]);
			assertTrue(ON_BOARD[north(p)]);
			assertTrue(ON_BOARD[east(p)]);
			assertFalse(ON_BOARD[south(p)]);
			assertFalse(ON_BOARD[west(p)]);
			int p2 = at("t19");
			assertTrue(ON_BOARD[p2]);
			assertFalse(ON_BOARD[north(p2)]);
			assertFalse(ON_BOARD[east(p2)]);
			assertTrue(ON_BOARD[south(p2)]);
	}

	@Test
	public void testPointToString() {
		assertEquals("E3", pointToString(at("e3")));
	}

	@Test
	public void testGetDistance() {
		assertEquals(3, distance(at("a1"), at("d1")), .001);
		assertEquals(Math.sqrt(13), distance(at("a1"), at("c4")), .001);
	}

	@Test
	public void testLargeKnightsMoveNeighbors() {
		assertEquals(36, LARGE_KNIGHT_NEIGHBORHOOD[at("e5")].length);
		assertEquals(12, LARGE_KNIGHT_NEIGHBORHOOD[at("a1")].length);
	}

	@Test
	public void testThirdFourthLineArray() {
		// Confirm the size of the array
		int count = 0;
		for (int i = 0; i < EXTENDED_BOARD_AREA; i++) {
			if (THIRD_OR_FOURTH_LINE[i]) {
				count++;
			}
		}
		assertEquals(count, THIRD_AND_FOURTH_LINE_POINTS.length);
		// Confirm the contents of the array
		for (int i = 0; i < THIRD_AND_FOURTH_LINE_POINTS.length; i++) {
			assertTrue(THIRD_OR_FOURTH_LINE[THIRD_AND_FOURTH_LINE_POINTS[i]]);
		}
	}
	
	@Test
	public void testSgfToPoint() {
		assertEquals(at("e15"), sgfToPoint("ee"));
		assertEquals(at("t1"), sgfToPoint("ss"));
		assertEquals(at("a19"), sgfToPoint("aa"));
		assertEquals(at("t19"), sgfToPoint("sa"));
		assertEquals(at("a1"), sgfToPoint("as"));
	}

	@Test
	public void testSquareNeighborhood() {
		assertEquals(29, SQUARE_NEIGHBORHOOD[3][at("c2")].length);
	}
}
