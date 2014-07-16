package edu.lclark.orego.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.CoordinateSystem;

public class ShortSetTest {

	private ShortSet set;

	private static final CoordinateSystem COORDS = CoordinateSystem.forWidth(19);
	
	private static final int CAPACITY = COORDS.getFirstPointBeyondBoard();
	
	@Before
	public void setUp() throws Exception {
		// Set can hold any points on the board
		set = new ShortSet(CAPACITY);
	}

	@Test
	public void testAdd() {
		// The set should initially be empty
		assertEquals(0, set.size());
		// Add some elements
		set.add((short)23);
		set.add((short)99);
		set.add((short)99);
		// Verify that the set has size 2
		assertEquals(2, set.size());
		// Verify that there were no side effects
		assertEquals(2, set.size());
		// Verify that some element is present
		assertTrue(set.contains((short)23));
		// Verify that some element is not present
		assertFalse(set.contains((short)0));
	}

	@Test
	public void testAddKnownAbsent() {
		// Add elements
		set.add((short)1);
		set.add((short)2);
		set.add((short)3);
		// Add a known absent element
		set.addKnownAbsent((short)4);
		// Verify that it was added
		assertEquals(4, set.size());
		assertEquals("{1, 2, 3, 4}", set.toString());
	}

	@Test
	public void testCopy() {
		// Add elements
		set.add((short)40);
		set.add((short)42);
		// Make a copy
		ShortSet copy = new ShortSet(100);
		copy.copyDataFrom(set);
		// Verify that an element is present
		assertTrue(copy.contains((short)42));
	}

	@Test
	public void testOverwrite() {
		// Add elements
		set.add((short)1);
		set.add((short)2);
		// Remove one
		set.remove((short)1);
		// Verify that removal worked
		assertTrue(set.contains((short)2));
		assertFalse(set.contains((short)1));
		assertEquals(1, set.size());
		// Add a new element
		set.add((short)3);
		// Verify that this worked
		assertTrue(set.contains((short)2));
		assertTrue(set.contains((short)3));
		assertFalse(set.contains((short)1));
		assertEquals(2, set.size());
		// Remove the same element again
		set.remove((short)1);
		// Verify that this has no effect
		assertEquals(2, set.size());
		// Add the removed element
		set.add((short)1);
		// Verify that all elements are present
		assertTrue(set.contains((short)2));
		assertTrue(set.contains((short)3));
		assertTrue(set.contains((short)1));
		assertEquals(3, set.size());
	}

	@Test
	public void testRemove() {
		// Add elements
		set.add((short)23);
		set.add((short)99);
		// Remove one
		set.remove((short)23);
		// Remove a nonexistent element
		set.remove((short)40);
		// Verify that only the correct element remains
		assertEquals(1, set.size());
		assertFalse(set.contains((short)23));
	}

	@Test
	public void testRemoveKnownPresent() {
		// Add elements
		set.add((short)1);
		set.add((short)2);
		set.add((short)3);
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
		set.add((short)42);
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
		set.add((short)4);
		set.add((short)5);
		set.add((short)9);
		// Are they in the right places?
		assertEquals(4, set.get((short)0));
		assertEquals(5, set.get((short)1));
		assertEquals(9, set.get((short)2));
		// Remove one
		set.remove((short)4);
		// Was the last element moved into its place?
		assertEquals(9, set.get((short)0));
	}

	@Test
	public void testToString() {
		// Test on empty set
		assertEquals("{}", set.toString());
		// Add elements
		set.add((short)8);
		set.add((short)3);
		// Test on resulting set
		assertEquals("{8, 3}", set.toString());		
	}

	@Test
	public void testToStringAsPoints() {
		// Test on empty set
		assertEquals("0: {}", set.toString(COORDS));
		// Add elements
		set.add(COORDS.at("e2"));
		set.add(COORDS.at("b1"));
		// Test on resulting set
		assertEquals("2: {E2, B1}", set.toString(COORDS));
	}

	@Test
	public void testAddAll() {
		// Add elements
		set.add((short)4);
		set.add((short)5);
		// Create a second set
		ShortSet other = new ShortSet(12);
		other.add((short)4);
		other.add((short)7);
		other.add((short)6);
		// Take the union
		set.addAll(other);
		// Verify that it worked
		assertEquals(4, set.size());
		for (int i = 4; i <= 7; i++) {
			assertTrue(set.contains((short)i));
		}
	}

	@Test
	public void testEquals() {
		// Set should not be equals() to null
		assertFalse(set.equals(null));
		// Objects of different classes are not equals()
		assertFalse(set.equals("a String"));
		// Add elements
		set.add((short)4);
		set.add((short)5);
		// Test reflexivity
		assertTrue(set.equals(set));
		// Create a second set
		ShortSet other = new ShortSet(CAPACITY);
		other.add((short)5);
		other.add((short)7);
		other.add((short)6);
		// Test inequality
		assertTrue(other.equals(other));
		assertFalse(set.equals(other));
		// Make the sets equal (but in different orders)
		other.add((short)4);
		other.remove((short)6);
		other.remove((short)7);
		// Test symmetry
		assertTrue(other.equals(set));
		assertTrue(set.equals(other));
		// Sets from different-sized universes should not be equals()
		ShortSet other2 = new ShortSet(12);
		other2.add((short)4);
		other2.add((short)5);
		assertFalse(other2.equals(set));
		assertFalse(other2.equals(other));
		// Different sets of the same size should not be equals()
		ShortSet other3 = new ShortSet(CAPACITY);
		other3.add((short)4);
		other3.add((short)9);
		assertFalse(other3.equals(set));
		assertFalse(set.equals(other3));
	}
	
	@Test
	public void testSubset() {
		// Add elements
		set.add((short)4);
		set.add((short)5);
		// Create a superset
		ShortSet other = new ShortSet(CAPACITY);
		other.add((short)4);
		other.add((short)5);
		other.add((short)3);
		// These two sets should not be equals()
		assertFalse(set.equals(other));
		assertFalse(other.equals(set));
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testHashCode() {
		set.hashCode();
	}

}
