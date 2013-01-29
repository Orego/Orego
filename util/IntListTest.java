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
		// The list should initially have size 0
		assertEquals(0, list.size());
		// Test toString() for an empty list
		assertEquals("()", list.toString());		
		// Add a couple of elements
		list.add(23);
		list.add(99);
		// Verify that the size is now 2
		assertEquals(2, list.size());
		// Verify that there were no side effects
		assertEquals(2, list.size());
		// Verify that some element is present
		assertTrue(list.contains(23));
		// Verify that some element is not present
		assertFalse(list.contains(0));
		// Test toString()
		assertEquals("(23, 99)", list.toString());
	}

	@Test
	public void testAddIfNotPresent() {
		// Add some elements
		list.addIfNotPresent(23);
		list.addIfNotPresent(99);
		// Now add them again
		list.addIfNotPresent(99);
		list.addIfNotPresent(23);
		// Verify that the size is now 2
		assertEquals(2, list.size());
		// Verify that there were no side effects
		assertEquals(2, list.size());
		// Verify that some element is present
		assertTrue(list.contains(23));
		// Verify that some element is not present
		assertFalse(list.contains(0));
		// Test toString()
		assertEquals("(23, 99)", list.toString());
	}

	@Test
	public void testCapacity() {
		// This capacity is specified in setUp()
		assertEquals(100, list.capacity());
	}

	@Test
	public void testClear() {
		list.add(42);
		list.clear();
		// Clearing should leave an empty list
		assertEquals(0, list.size());
		// The list should not contain a past element
		assertFalse(list.contains(42));
	}

	@Test
	public void testCopyDataFrom() {
		// Add elements to one list
		list.add(1);
		list.add(10);
		list.add(-5);
		// Make a copy
		IntList list2 = new IntList(100);
		list2.copyDataFrom(list);
		// They should have the same size
		assertEquals(3, list2.size());
		// An element should be in the copy
		assertTrue(list2.contains(-5));
		// The first element in the copy should be the same
		assertEquals(1, list2.get(0));
	}

	@Test
	public void testRemoveLast() {
		// Add elements
		list.add(5);
		list.add(8);
		list.add(3);
		// Remove the last one
		assertEquals(3, list.removeLast());
		// Size should be 2
		assertEquals(2, list.size());
		// The front element should be unchanged
		assertEquals(5, list.get(0));
		// The remaining back element should be unchanged
		assertEquals(8, list.get(1));
	}

	@Test
	public void testSet() {
		// Add elements
		list.add(5);
		list.add(8);
		list.add(3);
		// Replace one
		list.set(1, 100);
		// Verify that it worked
		assertEquals(100, list.get(1));
		assertEquals("(5, 100, 3)", list.toString());
	}

	@Test
	public void testToStringAsPoints() {
		// Test toString() for the empty list
		assertEquals("()", list.toStringAsPoints());
		// Add elements
		list.add(at("e2"));
		list.add(at("b1"));
		// Test toString() on resulting list
		assertEquals("(E2, B1)", list.toStringAsPoints());
	}

}
