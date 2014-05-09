package edu.lclark.orego.core;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CoordinateSystemTest {

	private CoordinateSystem c5;
	
	private CoordinateSystem c19;
	
	@Before
	public void setUp() {
		c5 = CoordinateSystem.forWidth(5);
		c19 = CoordinateSystem.forWidth(19);
	}

	@Test
	public void testAt() {
		assertEquals(c19.at("b19"), c19.at(0, 1));
		assertEquals(c5.at("b5"), c5.at(0, 1));
		assertEquals(CoordinateSystem.PASS, c5.at("PASS"));
		assertEquals(CoordinateSystem.RESIGN, c5.at("RESIGN"));
	}

	@Test
	public void testRowAndCol() {
		short p = c5.at(4, 2);
		assertEquals(4, c5.row(p));
		assertEquals(2, c5.column(p));
		p = c19.at(4, 2);
		assertEquals(4, c19.row(p));
		assertEquals(2, c19.column(p));
	}

	@Test
	public void testRowAndColumnToString() {
		short p = c19.at("p14");
		assertEquals("14", c19.rowToString(c19.row(p)));
		assertEquals("P", c19.columnToString(c19.column(p)));
	}
	
	@Test
	public void testIs3or4() {
			// Test whether various points are on line 3 or 4 from both edges
			assertFalse(c19.isOnThirdOrFourthLine(c19.at("a1")));
			assertTrue(c19.isOnThirdOrFourthLine(c19.at("e4")));
			assertFalse(c19.isOnThirdOrFourthLine(c19.at("b2")));
			assertFalse(c19.isOnThirdOrFourthLine(c19.at("j9")));
			assertTrue(c19.isOnThirdOrFourthLine(c19.at("c4")));
			assertFalse(c19.isOnThirdOrFourthLine(c19.at("a7")));
			assertTrue(c19.isOnThirdOrFourthLine(c19.at("q4")));
			assertTrue(c19.isOnThirdOrFourthLine(c19.at("q17")));
			assertTrue(c19.isOnThirdOrFourthLine(c19.at("r16")));
			assertFalse(c19.isOnThirdOrFourthLine(c19.at("t7")));
	}

	@Test
	public void testIsOnBoard() {
			// a1 has two neighbors on the board
			short p = c19.at("a1");
			assertTrue(c19.isOnBoard(p));
			assertTrue(c19.isOnBoard(c19.getNeighbors(p)[0]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p)[1]));
			assertTrue(c19.isOnBoard(c19.getNeighbors(p)[2]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p)[3]));
			// t19 has a different two neighbors on the board
			short p2 = c19.at("t19");
			assertTrue(c19.isOnBoard(p2));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p2)[0]));
			assertTrue(c19.isOnBoard(c19.getNeighbors(p2)[1]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p2)[2]));
			assertTrue(c19.isOnBoard(c19.getNeighbors(p2)[3]));
	}

	@Test
	public void testPointToString() {
		assertEquals("E3", c19.pointToString(c19.at("e3")));
		assertEquals("PASS", c19.pointToString(CoordinateSystem.PASS));
		assertEquals("NO_POINT", c19.pointToString(CoordinateSystem.NO_POINT));
		assertEquals("RESIGN", c19.pointToString(CoordinateSystem.RESIGN));
	}

	@Test
	public void testManhattanDistance() {
		assertEquals(7, c19.manhattanDistance(c19.at("c4"), c19.at("f8")));
	}

	@Test
	public void testGetAllPointsOnBoard() {
		assertEquals(25, c5.getAllPointsOnBoard().length);
		assertEquals(361, c19.getAllPointsOnBoard().length);
	}

}
