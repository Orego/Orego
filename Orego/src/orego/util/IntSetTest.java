package orego.util;

import static orego.core.Coordinates.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class IntSetTest {

	private IntSet set;

	@Before
	public void setUp() throws Exception {
		set = new IntSet(LAST_POINT_ON_BOARD + 1);
	}

	@Test
	public void testAdd() {
		set.add(23);
		set.add(99);
		set.add(99);
		assertEquals(2, set.size());
		assertEquals(2, set.size()); // Second test to make sure size() didn't
										// destroy the data
		assertTrue(set.contains(23));
		assertFalse(set.contains(0));
	}

	@Test
	public void testAddKnownAbsent() {
		set.add(1);
		set.add(2);
		set.add(3);
		set.addKnownAbsent(4);
		assertEquals(4, set.size());
		assertEquals("{1, 2, 3, 4}", set.toString());
	}

	@Test
	public void testCopy() {
		set.add(40);
		set.add(42);
		IntSet copy = new IntSet(100);
		copy.copyDataFrom(set);
		assertTrue(copy.contains(42));
	}

	@Test
	public void testOverwrite() {
		set.add(1);
		set.add(2);
		set.remove(1);
		assertTrue(set.contains(2));
		assertFalse(set.contains(1));
		assertEquals(1, set.size());
		set.add(3);
		assertTrue(set.contains(2));
		assertTrue(set.contains(3));
		assertFalse(set.contains(1));
		assertEquals(2, set.size());
		set.remove(1);
		assertEquals(2, set.size());
		set.add(1);
		assertTrue(set.contains(2));
		assertTrue(set.contains(3));
		assertTrue(set.contains(1));
		assertEquals(3, set.size());
	}

	@Test
	public void testRemove() {
		set.add(23);
		set.add(99);
		set.remove(23);
		set.remove(40);
		assertEquals(1, set.size());
		assertFalse(set.contains(23));
	}

	@Test
	public void testRemoveKnownPresent() {
		set.add(1);
		set.add(2);
		set.add(3);
		set.removeKnownPresent(2);
		assertEquals(2, set.size());
		assertEquals("{1, 3}", set.toString());
	}

	@Test
	public void testClear() {
		set.add(42);
		assertFalse(set.isEmpty());
		set.clear();
		assertTrue(set.isEmpty());
	}

	@Test
	public void testGet() {
		set.add(4);
		set.add(5);
		set.add(9);
		assertEquals(4, set.get(0));
		assertEquals(5, set.get(1));
		assertEquals(9, set.get(2));
		set.remove(4);
		assertEquals(9, set.get(0));
	}

	@Test
	public void testToStringAsPoints() {
		set.add(at("e2"));
		set.add(at("b1"));
		assertEquals("2: {E2 B1}", set.toStringAsPoints());
	}

	@Test
	public void testUnion() {
		set.add(4);
		set.add(5);
		IntSet other = new IntSet(12);
		other.add(4);
		other.add(7);
		other.add(6);
		set.union(other);
		assertEquals(4, set.size());
		for (int i = 4; i <= 7; i++) {
			assertTrue(set.contains(i));
		}
	}

	@Test
	public void testEquals() {
		assertFalse(set.equals(null));
		IntSet other = new IntSet(LAST_POINT_ON_BOARD + 1);
		set.add(4);
		assertTrue(set.equals(set));
		set.add(5);
		// Reflexivity
		assertTrue(set.equals(set));
		assertTrue(other.equals(other));
		other.add(4);
		other.add(7);
		other.add(6);
		assertTrue(other.equals(other));
		assertFalse(set.equals(other));
		other.add(5);
		other.remove(6);
		other.remove(7);
		// Symmetric
		assertTrue(other.equals(set));
		assertTrue(set.equals(other));
		IntSet other2 = new IntSet(12);
		other2.add(4);
		other2.add(5);
		assertFalse(other2.equals(set) || other2.equals(other));
		IntSet other3 = new IntSet(LAST_POINT_ON_BOARD + 1);
		other3.add(5);
		other3.add(4);
		// Transitivity
		assertTrue(set.equals(other) && other.equals(other3)
				&& set.equals(other3));
	}
}
