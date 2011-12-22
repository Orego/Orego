package orego.util;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class PoolTest {

	private Pool<ListNode<Integer>> pool;

	@Before
	public void setUp() throws Exception {
		pool = new Pool<ListNode<Integer>>();
		for (int i = 0; i < 5; i++) {
			pool.free(new ListNode<Integer>());
		}
	}

	@Test
	public void testAllocate() {
		ListNode<Integer> previous = null;
		assertEquals(5, pool.size());
		for (int i = 0; i < 5; i++) {
			assertFalse(pool.isEmpty());
			ListNode<Integer> node = pool.allocate();
			assertNotSame(node, previous);
			assertNotNull(node);
			previous = node;
		}
		assertTrue(pool.isEmpty());
		assertNull(pool.allocate());
	}

	@Test
	public void testFree() {
		ListNode<Integer> node = new ListNode<Integer>();
		node.setKey(-8);
		pool.free(node);
		assertEquals(-8, pool.allocate().getKey().intValue());
	}

}
