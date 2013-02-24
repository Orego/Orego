package orego.core;

import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.EXTENDED_BOARD_AREA;
import static orego.core.Coordinates.LARGE_KNIGHT_NEIGHBORHOOD;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.ON_BOARD;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.RESIGN;
import static orego.core.Coordinates.THIRD_AND_FOURTH_LINE_POINTS;
import static orego.core.Coordinates.THIRD_OR_FOURTH_LINE;
import static orego.core.Coordinates.at;
import static orego.core.Coordinates.column;
import static orego.core.Coordinates.columnToChar;
import static orego.core.Coordinates.columnToString;
import static orego.core.Coordinates.distance;
import static orego.core.Coordinates.east;
import static orego.core.Coordinates.manhattanDistance;
import static orego.core.Coordinates.north;
import static orego.core.Coordinates.pointToString;
import static orego.core.Coordinates.row;
import static orego.core.Coordinates.rowToChar;
import static orego.core.Coordinates.rowToString;
import static orego.core.Coordinates.setBoardWidth;
import static orego.core.Coordinates.sgfToPoint;
import static orego.core.Coordinates.south;
import static orego.core.Coordinates.west;
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
			// Test whether various points are on line 3 or 4 from both edges
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
			// a1 has two neighbors on the board
			int p = at("a1");
			assertTrue(ON_BOARD[p]);
			assertTrue(ON_BOARD[north(p)]);
			assertTrue(ON_BOARD[east(p)]);
			assertFalse(ON_BOARD[south(p)]);
			assertFalse(ON_BOARD[west(p)]);
			// t19 has a different two neighbors on the board
			int p2 = at("t19");
			assertTrue(ON_BOARD[p2]);
			assertFalse(ON_BOARD[north(p2)]);
			assertFalse(ON_BOARD[east(p2)]);
			assertTrue(ON_BOARD[south(p2)]);
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
	public void testSetBoardWidth(){
		setBoardWidth(9);
		assertEquals(9, BOARD_WIDTH);
		setBoardWidth(19);
		assertEquals(19, BOARD_WIDTH);
		try{
			setBoardWidth(8);
		}catch(IndexOutOfBoundsException e){
		}
	}

}
