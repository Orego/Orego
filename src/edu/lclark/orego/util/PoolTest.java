package edu.lclark.orego.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PoolTest {

	private Pool<ListNode<Integer>> pool;

	@Before
	public void setUp() throws Exception {
		// Create the pool and add five nodes to it
		pool = new Pool<>();
		for (int i = 0; i < 5; i++) {
			pool.free(new ListNode<Integer>());
		}
	}

	@Test
	public void testAllocate() {
		// There should be 5 available nodes
		assertEquals(5, pool.size());
		// Ask for 5 nodes; they should all be different
		ListNode<Integer> previous = null;
		for (int i = 0; i < 5; i++) {
			assertFalse(pool.isEmpty());
			ListNode<Integer> node = pool.allocate();
			assertNotSame(node, previous);
			assertNotNull(node);
			previous = node;
		}
		// The pool should now be empty
		assertTrue(pool.isEmpty());
		assertNull(pool.allocate());
	}

	@SuppressWarnings("boxing")
	@Test
	public void testFree() {
		// Make a node and add it to the pool
		ListNode<Integer> node = new ListNode<>();
		node.setKey(-8);
		pool.free(node);
		// Allocate should return this node
		assertSame(node, pool.allocate());
		assertEquals(-8, node.getKey().intValue());
	}

}
