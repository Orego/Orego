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
		// Set can hold any points on the board
		set = new IntSet(FIRST_POINT_BEYOND_BOARD);
	}

	@Test
	public void testAdd() {
		// The set should initially be empty
		assertEquals(0, set.size());
		// Add some elements
		set.add(23);
		set.add(99);
		set.add(99);
		// Verify that the set has size 2
		assertEquals(2, set.size());
		// Verify that there were no side effects
		assertEquals(2, set.size());
		// Verify that some element is present
		assertTrue(set.contains(23));
		// Verify that some element is not present
		assertFalse(set.contains(0));
	}

	@Test
	public void testAddKnownAbsent() {
		// Add elements
		set.add(1);
		set.add(2);
		set.add(3);
		// Add a known absent element
		set.addKnownAbsent(4);
		// Verify that it was added
		assertEquals(4, set.size());
		assertEquals("{1, 2, 3, 4}", set.toString());
	}

	@Test
	public void testCopy() {
		// Add elements
		set.add(40);
		set.add(42);
		// Make a copy
		IntSet copy = new IntSet(100);
		copy.copyDataFrom(set);
		// Verify that an element is present
		assertTrue(copy.contains(42));
	}

	@Test
	public void testOverwrite() {
		// Add elements
		set.add(1);
		set.add(2);
		// Remove one
		set.remove(1);
		// Verify that removal worked
		assertTrue(set.contains(2));
		assertFalse(set.contains(1));
		assertEquals(1, set.size());
		// Add a new element
		set.add(3);
		// Verify that this worked
		assertTrue(set.contains(2));
		assertTrue(set.contains(3));
		assertFalse(set.contains(1));
		assertEquals(2, set.size());
		// Remove the same element again
		set.remove(1);
		// Verify that this has no effect
		assertEquals(2, set.size());
		// Add the removed element
		set.add(1);
		// Verify that all elements are present
		assertTrue(set.contains(2));
		assertTrue(set.contains(3));
		assertTrue(set.contains(1));
		assertEquals(3, set.size());
	}

	@Test
	public void testRemove() {
		// Add elements
		set.add(23);
		set.add(99);
		// Remove one
		set.remove(23);
		// Remove a nonexistent element
		set.remove(40);
		// Verify that only the correct element remains
		assertEquals(1, set.size());
		assertFalse(set.contains(23));
	}

	@Test
	public void testRemoveKnownPresent() {
		// Add elements
		set.add(1);
		set.add(2);
		set.add(3);
		// Remove one
		set.removeKnownPresent(2);
		// Verify that this worked
		assertEquals(2, set.size());
		assertEquals("{1, 3}", set.toString());
	}

	@Test
	public void testClear() {
		// Initial set should be empty
		assertEquals(0, set.size());
		// Add an element
		set.add(42);
		// Set should now have size 1
		assertEquals(1, set.size());
		// Clear set
		set.clear();
		// Set should now have size 0 again
		assertEquals(0, set.size());
	}

	@Test
	public void testGet() {
		// Add elements
		set.add(4);
		set.add(5);
		set.add(9);
		// Are they in the right places?
		assertEquals(4, set.get(0));
		assertEquals(5, set.get(1));
		assertEquals(9, set.get(2));
		// Remove one
		set.remove(4);
		// Was the last element moved into its place?
		assertEquals(9, set.get(0));
	}

	@Test
	public void testToString() {
		// Test on empty set
		assertEquals("{}", set.toString());
		// Add elements
		set.add(8);
		set.add(3);
		// Test on resulting set
		assertEquals("{8, 3}", set.toString());		
	}

	@Test
	public void testToStringAsPoints() {
		// Test on empty set
		assertEquals("0: {}", set.toStringAsPoints());
		// Add elements
		set.add(at("e2"));
		set.add(at("b1"));
		// Test on resulting set
		assertEquals("2: {E2, B1}", set.toStringAsPoints());
	}

	@Test
	public void testAddAll() {
		// Add elements
		set.add(4);
		set.add(5);
		// Create a second set
		IntSet other = new IntSet(12);
		other.add(4);
		other.add(7);
		other.add(6);
		// Take the union
		set.addAll(other);
		// Verify that it worked
		assertEquals(4, set.size());
		for (int i = 4; i <= 7; i++) {
			assertTrue(set.contains(i));
		}
	}

	@Test
	public void testEquals() {
		// Set should not be equals() to null
		assertFalse(set.equals(null));
		// Objects of different classes are not equals()
		assertFalse(set.equals("a String"));
		// Add elements
		set.add(4);
		set.add(5);
		// Test reflexivity
		assertTrue(set.equals(set));
		// Create a second set
		IntSet other = new IntSet(FIRST_POINT_BEYOND_BOARD);
		other.add(5);
		other.add(7);
		other.add(6);
		// Test inequality
		assertTrue(other.equals(other));
		assertFalse(set.equals(other));
		// Make the sets equal (but in different orders)
		other.add(4);
		other.remove(6);
		other.remove(7);
		// Test symmetry
		assertTrue(other.equals(set));
		assertTrue(set.equals(other));
		// Sets from different-sized universes should not be equals()
		IntSet other2 = new IntSet(12);
		other2.add(4);
		other2.add(5);
		assertFalse(other2.equals(set));
		assertFalse(other2.equals(other));
		// Different sets of the same size should not be equals()
		IntSet other3 = new IntSet(FIRST_POINT_BEYOND_BOARD);
		other3.add(4);
		other3.add(9);
		assertFalse(other3.equals(set));
		assertFalse(set.equals(other3));
	}
	
	@Test
	public void testSubset() {
		// Add elements
		set.add(4);
		set.add(5);
		// Create a superset
		IntSet other = new IntSet(FIRST_POINT_BEYOND_BOARD);
		other.add(4);
		other.add(5);
		other.add(3);
		// These two sets should not be equals()
		assertFalse(set.equals(other));
		assertFalse(other.equals(set));
	}

}
