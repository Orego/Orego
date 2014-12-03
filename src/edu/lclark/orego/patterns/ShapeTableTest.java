package edu.lclark.orego.patterns;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class ShapeTableTest {

	ShapeTable table;
	
	@Before
	public void setUp() throws Exception {
		table = new ShapeTable();
	}

	@Test
	public void testOverlap() {
		float f = table.getScalingFactor();
		table.update(1, true);
		assertEquals(f * 0.5 + 1 * (1 - f), table.getWinRate(1), 0.0001f);
		table.update(375299968947541L, false);
		assertEquals(f * 0.5 + 0 * (1 - f), table.getWinRate(375299968947541L), 0.0001f);
		// This number overlaps with the first one in 2 of the 4 tables
		table.update(70000, true);
		float a = f * 0.5f + 1 * (1 - f);
		float b = f * a + 1 * (1 - f);
		assertEquals((a + (b*2)) / 3, table.getWinRate(70000), 0.0001f);
	}
	
	@Test
	public void testTigersMouth() {
		// Playing into a Tiger's mouth should be a terrible idea
		table = new ShapeTable(OREGO_ROOT
				+ "patterns/patterns3stones-SHAPE-sf999.data", 0.999f);
		Board board = new Board(9);
		CoordinateSystem coords = board.getCoordinateSystem();
		board.play("d5");
		board.pass();
		board.play("f5");
		board.pass();
		board.play("e6");
		// TODO Should getHash really be a static method?
		long hash = PatternFinder.getHash(board, coords.at("e5"), 3, coords.at("a1"));
		// Note that this test would fail if the last move (a1 above) were changed
		// to one of the nearby stones, because such a move is so rare that the table
		// slot would consist entirely of noise.
		assertTrue(table.getWinRate(hash) < 0.3);
	}
	


}
