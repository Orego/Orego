package edu.lclark.orego.core;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class SuperKoTableTest {

	private SuperKoTable table;

	@Before
	public void setUp() throws Exception {
		table = new SuperKoTable(CoordinateSystem.forWidth(19));
	}

	@Test
	public void testAdd() {
		// Add a value and make sure it appears
		assertFalse(table.contains(-3));
		table.add(-3);
		assertTrue(table.contains(-3));
		// Make sure a redundant add doesn't hurt
		assertFalse(table.contains(3));
		table.add(3);
		assertTrue(table.contains(3));
		table.add(3);
		assertTrue(table.contains(3));
	}

	@Test
	public void testCopyDataFrom() {
		SuperKoTable table2 = new SuperKoTable(CoordinateSystem.forWidth(19));
		table.add(-3);
		table2.copyDataFrom(table);
		assertTrue(table2.contains(-3));
	}

	@Test
	public void testZero() {
		assertTrue(table.contains(0));
	}

	@Test
	public void testNegativeAbsoluteValue() {
		// Math.abs(Integer.MIN_VALUE) is negative; test that we handle this correctly
		int abomination = Integer.MIN_VALUE;
		assertFalse(table.contains(abomination));
		table.add(abomination);
		assertTrue(table.contains(abomination));
	}

	@Test
	public void testCollision() {
		// Add two values that would end up in the same slot
		table.add(5);
		int key = 5 + table.capacity();
		table.add(key);
		assertTrue(table.contains(5));
		assertTrue(table.contains(key));
	}

}
