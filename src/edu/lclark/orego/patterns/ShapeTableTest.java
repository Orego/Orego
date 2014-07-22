package edu.lclark.orego.patterns;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ShapeTableTest {

	ShapeTable table;
	
	@Before
	public void setUp() throws Exception {
		table = new ShapeTable();
	}

	@Test
	public void test() {
		float f = table.getScalingFactor();
		table.update(1, true);
		assertEquals(f * 0.5 + 1 * (1 - f), table.getWinRate(1), 0.0001f);
		table.update(375299968947541L, false);
		assertEquals(f * 0.5 + 0 * (1 - f), table.getWinRate(375299968947541L), 0.0001f);
		// This number overlaps with the first one in 2 of the 4 tables
		table.update(70000, true);
		float a = f * 0.5f + 1 * (1 - f);
		float b = f * a + 1 * (1 - f);
		assertEquals((a + b) / 2, table.getWinRate(70000), 0.0001f);
	}

}
