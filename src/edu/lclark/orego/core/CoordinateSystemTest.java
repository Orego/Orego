package edu.lclark.orego.core;

import static edu.lclark.orego.core.CoordinateSystem.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.mcts.CopiableStructure;

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
		assertEquals("P", CoordinateSystem.columnToString(c19.column(p)));
	}

	@Test
	public void testNeighborsOnBoard() {
			short p = c19.at("a1");
			assertTrue(c19.isOnBoard(p));
			assertTrue(c19.isOnBoard(c19.getNeighbors(p)[NORTH_NEIGHBOR]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p)[WEST_NEIGHBOR]));
			assertTrue(c19.isOnBoard(c19.getNeighbors(p)[EAST_NEIGHBOR]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p)[SOUTH_NEIGHBOR]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p)[NORTHWEST_NEIGHBOR]));
			assertTrue(c19.isOnBoard(c19.getNeighbors(p)[NORTHEAST_NEIGHBOR]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p)[SOUTHWEST_NEIGHBOR]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p)[SOUTHEAST_NEIGHBOR]));
			short p2 = c19.at("t19");
			assertTrue(c19.isOnBoard(p2));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p2)[NORTH_NEIGHBOR]));
			assertTrue(c19.isOnBoard(c19.getNeighbors(p2)[WEST_NEIGHBOR]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p2)[EAST_NEIGHBOR]));
			assertTrue(c19.isOnBoard(c19.getNeighbors(p2)[SOUTH_NEIGHBOR]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p2)[NORTHWEST_NEIGHBOR]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p2)[NORTHEAST_NEIGHBOR]));
			assertTrue(c19.isOnBoard(c19.getNeighbors(p2)[SOUTHWEST_NEIGHBOR]));
			assertFalse(c19.isOnBoard(c19.getNeighbors(p2)[SOUTHEAST_NEIGHBOR]));
	}

	@Test
	public void testPointToString() {
		assertEquals("E3", c19.toString(c19.at("e3")));
		assertEquals("PASS", c19.toString(CoordinateSystem.PASS));
		assertEquals("NO_POINT", c19.toString(CoordinateSystem.NO_POINT));
		assertEquals("RESIGN", c19.toString(CoordinateSystem.RESIGN));
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

	@Test
	public void testGetFirstPointBeyondBoard() {
		assertEquals((5 * 7) + 1, c5.getFirstPointBeyondBoard());
		assertEquals((19 * 21) + 1, c19.getFirstPointBeyondBoard());
		assertEquals(c19.getNeighbors(c19.at("t1"))[EAST_NEIGHBOR], c19.getFirstPointBeyondBoard());
	}
	
	@Test
	public void testNeighbors() {
		short[] neighbors = c5.getNeighbors(c5.at("b2"));
		assertEquals(c5.at("b3"), neighbors[NORTH_NEIGHBOR]);
		assertEquals(c5.at("b1"), neighbors[SOUTH_NEIGHBOR]);
		assertEquals(c5.at("c2"), neighbors[EAST_NEIGHBOR]);
		assertEquals(c5.at("a2"), neighbors[WEST_NEIGHBOR]);
	}

	@Test
	public void testReadResolve() {
		CopiableStructure stuff = new CopiableStructure().add(c19);
		CoordinateSystem c19again = stuff.copy().get(CoordinateSystem.class);
		assertSame(c19, c19again);
	}

	@Test
	public void testIsOnBoard() {
		assertFalse(c19.isOnBoard(c19.getFirstPointBeyondBoard()));
	}
}
