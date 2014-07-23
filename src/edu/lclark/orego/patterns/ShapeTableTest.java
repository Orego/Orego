package edu.lclark.orego.patterns;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

public class ShapeTableTest {

	ShapeTable table;
	
	@Before
	public void setUp() throws Exception {
		table = new ShapeTable();
	}

	@Test
	public void test() {
		table.update(1, true);
		assertEquals(0.505f, table.getWinRate(1), 0.0001f);
		table.update(375299968947541L, false);
		assertEquals(0.495f, table.getWinRate(375299968947541L), 0.0001f);
		table.update(70000, true);
		assertEquals(0.507475f, table.getWinRate(70000), 0.0001f);
	}
	
	public void testCollisions(){
		HashMap<String, Float> map = new HashMap<>();
		HashMap<String, Long> hashMap = new HashMap<>();
		Board board = new Board(19);
		table = new ShapeTable("patterns" + File.separator
				+ "patterns3x3-SHAPE-sf90.data");
		int centerColumn = 11;
		int centerRow = 11;
		int patternRadius = 1;
		int minStoneCount = 0;
		int maxStoneCount = 8;
		ArrayList<Short> stones = new ArrayList<>();
		PatternFinder.generatePatternMap(board, map, hashMap, table, stones, minStoneCount, maxStoneCount,
				centerRow, centerColumn, patternRadius);
	}

}
