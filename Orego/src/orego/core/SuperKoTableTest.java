package orego.core;

import static org.junit.Assert.*;
import static orego.core.Board.MAX_MOVES_PER_GAME;
import org.junit.Before;
import org.junit.Test;

public class SuperKoTableTest {

	private SuperKoTable table;

	@Before
	public void setUp() throws Exception {
		table = new SuperKoTable();
	}

	@Test
	public void testAdd() {
		assertFalse(table.contains(-3));
		table.add(-3);
		table.add(3);
		table.add(3);
		table.add(4);
		table.add(3 + (2 * MAX_MOVES_PER_GAME));
		assertTrue(table.contains(-3));
		long key = -3 + (2 * MAX_MOVES_PER_GAME);
		assertFalse(table.contains(key));
		table.add(key);
		assertTrue(table.contains(key));
		assertTrue(table.contains(-3));
		assertTrue(table.contains(3 + (2 * MAX_MOVES_PER_GAME)));
	}

	@Test
	public void testCopyDataFrom() {
		SuperKoTable table2 = new SuperKoTable();
		table.add(-3);
		long key = -3 + (2 * MAX_MOVES_PER_GAME);
		table.add(key);
		table2.copyDataFrom(table);
		assertTrue(table2.contains(key));
		assertTrue(table2.contains(-3));
	}

	@Test
	public void testZero() {
		assertFalse(table.contains(0));
		table.add(0);
		assertTrue(table.contains(0));
		SuperKoTable table2 = new SuperKoTable();
		assertFalse(table2.contains(0));
		table2.copyDataFrom(table);
		assertTrue(table2.contains(0));
	}

	@Test
	public void testNegativeAbsoluteValue() {
		// Math.abs(Integer.MIN_VALUE) is negative; test that we handle this correctly
		int abomination = Integer.MIN_VALUE;
		assertFalse(table.contains(abomination));
		table.add(abomination);
		assertTrue(table.contains(abomination));
	}

}
