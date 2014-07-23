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

}
