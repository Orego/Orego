package orego.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static orego.core.Coordinates.*;
import org.junit.Before;
import org.junit.Test;

public class IntListTest {

	private IntList list;

	@Before
	public void setUp() throws Exception {
		list = new IntList(100);
	}

	@Test
	public void testAdd() {
		list.add(23);
		list.add(99);
		assertEquals(2, list.size());
		assertEquals(2, list.size()); // Second test to make sure size() didn't
										// destroy the data
		assertTrue(list.contains(23));
		assertFalse(list.contains(0));
		assertEquals("(23, 99)", list.toString());
	}

	@Test
	public void testAddIfNotPresent() {
		list.addIfNotPresent(23);
		list.addIfNotPresent(99);
		list.addIfNotPresent(99);
		list.addIfNotPresent(23);
		assertEquals(2, list.size());
		assertEquals(2, list.size()); // Second test to make sure size() didn't
										// destroy the data
		assertTrue(list.contains(23));
		assertFalse(list.contains(0));
		assertEquals("(23, 99)", list.toString());
	}

	@Test
	public void testCapacity() {
		assertEquals(100, list.capacity());
	}

	@Test
	public void testClear() {
		list.add(42);
		list.clear();
		assertEquals(0, list.size());
		assertFalse(list.contains(42));
	}

	@Test
	public void testCopyDataFrom() {
		list.add(1);
		list.add(10);
		list.add(-5);
		IntList list2 = new IntList(100);
		list2.copyDataFrom(list);
		assertEquals(3, list2.size());
		assertTrue(list2.contains(-5));
		assertEquals(1, list2.get(0));
	}

	@Test
	public void testRemoveAt() {
		list.add(5);
		list.add(8);
		list.add(3);
		assertEquals(3, list.removeAt(0));
		assertEquals(2, list.size());
		assertEquals(3, list.get(0));
		assertEquals(8, list.get(1));
	}

	@Test
	public void testRemoveLast() {
		list.add(5);
		list.add(8);
		list.add(3);
		assertEquals(3, list.removeLast());
		assertEquals(2, list.size());
		assertEquals(5, list.get(0));
		assertEquals(8, list.get(1));
	}

	@Test
	public void testSet() {
		list.add(5);
		list.add(8);
		list.add(3);
		list.set(1, 100);
		assertEquals(100, list.get(1));
	}

	@Test
	public void testToStringAsPoints() {
		list.add(at("e2"));
		list.add(at("b1"));
		assertEquals("(E2 B1)", list.toStringAsPoints());
	}

}
