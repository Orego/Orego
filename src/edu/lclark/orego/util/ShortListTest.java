package edu.lclark.orego.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

import org.junit.Before;
import org.junit.Test;

public class ShortListTest {

	private ShortList list;

	@Before
	public void setUp() throws Exception {
		list = new ShortList(100);
	}

	@Test
	public void testAdd() {
		// The list should initially have size 0
		assertEquals(0, list.size());
		// Test toString() for an empty list
		assertEquals("()", list.toString());		
		// Add a couple of elements
		list.add((short)23);
		list.add((short)99);
		// Verify that the size is now 2
		assertEquals(2, list.size());
		// Verify that there were no side effects
		assertEquals(2, list.size());
		// Verify that some element is present
		assertTrue(list.contains((short)23));
		// Verify that some element is not present
		assertFalse(list.contains((short)0));
		// Test toString()
		assertEquals("(23, 99)", list.toString());
	}

	@Test
	public void testAddIfNotPresent() {
		// Add some elements
		list.addIfNotPresent((short)23);
		list.addIfNotPresent((short)99);
		// Now add them again
		list.addIfNotPresent((short)99);
		list.addIfNotPresent((short)23);
		// Verify that the size is now 2
		assertEquals(2, list.size());
		// Verify that there were no side effects
		assertEquals(2, list.size());
		// Verify that some element is present
		assertTrue(list.contains((short)23));
		// Verify that some element is not present
		assertFalse(list.contains((short)0));
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
		list.add((short)42);
		list.clear();
		// Clearing should leave an empty list
		assertEquals(0, list.size());
		// The list should not contain a past element
		assertFalse(list.contains((short)42));
	}

	@Test
	public void testCopyDataFrom() {
		// Add elements to one list
		list.add((short)1);
		list.add((short)10);
		list.add((short)-5);
		// Make a copy
		ShortList list2 = new ShortList(100);
		list2.copyDataFrom(list);
		// They should have the same size
		assertEquals(3, list2.size());
		// An element should be in the copy
		assertTrue(list2.contains((short)-5));
		// The first element in the copy should be the same
		assertEquals(1, list2.get(0));
	}

	@Test
	public void testRemoveLast() {
		// Add elements
		list.add((short)5);
		list.add((short)8);
		list.add((short)3);
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
		list.add((short)5);
		list.add((short)8);
		list.add((short)3);
		// Replace one
		list.set(1, (short)100);
		// Verify that it worked
		assertEquals(100, list.get(1));
		assertEquals("(5, 100, 3)", list.toString());
	}

	@Test
	public void testToStringAsPoints() {
		CoordinateSystem coords = CoordinateSystem.forWidth(19);
		// Test toString() for the empty list
		assertEquals("()", list.toString(coords));
		// Add elements
		list.add(coords.at("e2"));
		list.add(coords.at("b1"));
		// Test toString() on resulting list
		assertEquals("(E2, B1)", list.toString(coords));
	}

	@Test(expected=AssertionError.class)
	public void testInvalidGetIndex() {
		list.add((short)5);
		list.get(1);
	}
	
	@Test(expected=AssertionError.class)
	public void testInvalidSetIndex() {
		list.add((short)5);
		list.set(1, (short)8);
	}
	
	@Test
	public void testRemoveRandom() {
		list.add((short)5);
		list.add((short)8);
		list.add((short)3);
		int firstSize = list.size();
		int random = list.removeRandom(new MersenneTwisterFast());
		assertEquals(firstSize - 1, list.size()); 
		assertTrue((random == (short)5) || (random == (short)8) || (random == (short)3));
	}

}
