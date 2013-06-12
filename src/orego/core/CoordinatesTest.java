package orego.core;

import static orego.core.Coordinates.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
			// These methods are for sgf
			int p = at("p14");
			assertEquals('n', rowToChar(row(p)));
			assertEquals('o', columnToSgfChar(column(p)));
			p = at("a1");
			assertEquals('a', rowToChar(row(p)));
			assertEquals('a', columnToSgfChar(column(p)));
			p = at("t19");
			assertEquals('s', rowToChar(row(p)));
			assertEquals('s', columnToSgfChar(column(p)));
	}

	@Test
	public void testIs3or4() {
			// Test whether various points are on line 3 or 4 from both edges
			assertFalse(getThirdOrFourthLine()[at("a1")]);
			assertTrue(getThirdOrFourthLine()[at("e4")]);
			assertFalse(getThirdOrFourthLine()[at("b2")]);
			assertFalse(getThirdOrFourthLine()[at("j9")]);
			assertTrue(getThirdOrFourthLine()[at("c4")]);
			assertFalse(getThirdOrFourthLine()[at("a7")]);
			assertTrue(getThirdOrFourthLine()[at("q4")]);
			assertTrue(getThirdOrFourthLine()[at("q17")]);
			assertTrue(getThirdOrFourthLine()[at("r16")]);
			assertFalse(getThirdOrFourthLine()[at("t7")]);
	}

	@Test
	public void testIsOnBoard() {
			// a1 has two neighbors on the board
			int p = at("a1");
			assertTrue(getOnBoard()[p]);
			assertTrue(getOnBoard()[getNeighbors(p)[0]]);
			assertFalse(getOnBoard()[getNeighbors(p)[1]]);
			assertTrue(getOnBoard()[getNeighbors(p)[2]]);
			assertFalse(getOnBoard()[getNeighbors(p)[3]]);
			// t19 has a different two neighbors on the board
			int p2 = at("t19");
			assertTrue(getOnBoard()[p2]);
			assertFalse(getOnBoard()[getNeighbors(p2)[0]]);
			assertTrue(getOnBoard()[getNeighbors(p2)[1]]);
			assertFalse(getOnBoard()[getNeighbors(p2)[2]]);
			assertTrue(getOnBoard()[getNeighbors(p2)[3]]);
	}

	@Test
	public void testPointToString() {
		assertEquals("E3", pointToString(at("e3")));
		assertEquals("PASS", pointToString(PASS));
		assertEquals("NO_POINT", pointToString(NO_POINT));
		assertEquals("RESIGN", pointToString(RESIGN));
	}

	@Test
	public void testGetDistance() {
		// Test Euclidean distance
		assertEquals(3, distance(at("a1"), at("d1")), .001);
		assertEquals(Math.sqrt(13), distance(at("a1"), at("c4")), .001);
	}

	@Test
	public void testLargeKnightsMoveNeighbors() {
		// Point on the edge have fewer large knight neighbors than
		// points in the center
		assertEquals(36, getLargeKnightNeighborhood(at("e5")).length);
		assertEquals(12, getLargeKnightNeighborhood(at("a1")).length);
	}

	@Test
	public void testThirdFourthLineArray() {
		// Confirm the size of the array
		int count = 0;
		for (int i = 0; i < getExtendedBoardArea(); i++) {
			if (getThirdOrFourthLine()[i]) {
				count++;
			}
		}
		assertEquals(count, getThirdAndFourthLinePoints().length);
		// Confirm the contents of the array
		for (int i = 0; i < getThirdAndFourthLinePoints().length; i++) {
			assertTrue(getThirdOrFourthLine()[getThirdAndFourthLinePoints()[i]]);
		}
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
	
	@Test
	public void testManhattanDistance() {
		assertEquals(7, manhattanDistance(at("c4"), at("f8")));
	}
	
	/** THis test will throw an IndexOutOfBounds exception, and therefore fail, 
	 * on the attempt to set BOARD_WIDTH to 8. This means setBoardWidth is working correctly.
	 * 
	 */
	@Test(expected=IndexOutOfBoundsException.class)
	public void testSetBoardWidthFailure() throws IndexOutOfBoundsException{
		setBoardWidth(-8);
	}
	
	@Test
	public void testSetBoardWidth() {
		setBoardWidth(9);
		assertEquals(9, getBoardWidth());
		setBoardWidth(19);
		assertEquals(19, getBoardWidth());
	}

}
