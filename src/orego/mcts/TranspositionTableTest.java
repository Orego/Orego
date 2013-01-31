package orego.mcts;

import static org.junit.Assert.*;

import orego.util.ListNode;

import org.junit.Before;
import org.junit.Test;

public class TranspositionTableTest {

	public static final int TABLE_SIZE = 17;

	private TranspositionTable table;

	@Before
	public void setUp() throws Exception {
		table = new TranspositionTable(TABLE_SIZE, new SearchNode());
	}

	@Test
	public void testFindOrAllocate() {
		SearchNode m = table.findOrAllocate(-1L);
		assertNotNull(m);
		SearchNode n = table.findOrAllocate(-1L);
		assertTrue(m == n);
		SearchNode p = table.findOrAllocate(1L);
		assertFalse(m == p);
	}

	@Test
	public void testFindOrAllocate2() {
		SearchNode a = table.findOrAllocate(1);
		SearchNode b = table.findOrAllocate(1 + TABLE_SIZE);
		SearchNode c = table.findOrAllocate(1);
		SearchNode d = table.findOrAllocate(1 + TABLE_SIZE);
		assertEquals(a, c);
		assertEquals(b, d);
		assertFalse(a == b);
	}

	@Test
	public void testFindIfPresent() {
		assertNull(table.findIfPresent(1L));
		table.findOrAllocate(0L);
		SearchNode n = table.findOrAllocate(1L);
		table.findOrAllocate(2L);
		assertEquals(n, table.findIfPresent(1L));
	}

	@Test
	public void testAllocationFailsWhenNoNodesLeft() {
		for (int i = 0; i < TABLE_SIZE; i++) {
			table.findOrAllocate(i);
		}
		assertNull(table.findOrAllocate(TABLE_SIZE));
	}

	@Test
	public void testAddChild() {
		SearchNode parent = table.findOrAllocate(0L);
		SearchNode child1 = table.findOrAllocate(1L);
		SearchNode child2 = table.findOrAllocate(2L);
		table.addChild(parent, child1);
		table.addChild(parent, child2);
		ListNode<SearchNode> n = parent.getChildren();
		assertEquals(child2, n.getKey());
		assertEquals(child1, n.getNext().getKey());
	}

	@Test
	public void testMarkNodesReachableFrom() {
		SearchNode a = table.findOrAllocate(0L);
		SearchNode b = table.findOrAllocate(1L);
		SearchNode c = table.findOrAllocate(2L);
		SearchNode d = table.findOrAllocate(3L);
		SearchNode e = table.findOrAllocate(4L);
		table.addChild(a, b);
		table.addChild(b, c);
		table.addChild(b, d);
		table.addChild(c, e);
		table.addChild(d, e);
		table.markNodesReachableFrom(b);
		assertFalse(a.isMarked());
		assertTrue(b.isMarked());
		assertTrue(c.isMarked());
		assertTrue(d.isMarked());
		assertTrue(e.isMarked());
	}

	@Test
	public void testSweep() {
		SearchNode a = table.findOrAllocate(0L);
		SearchNode b = table.findOrAllocate(1L);
		SearchNode c = table.findOrAllocate(2L);
		SearchNode d = table.findOrAllocate(3L);
		SearchNode e = table.findOrAllocate(4L);
		table.addChild(a, b);
		table.addChild(b, c);
		table.addChild(b, d);
		table.addChild(c, e);
		table.addChild(d, e);
		table.markNodesReachableFrom(b);
		table.sweep();
		assertEquals(table.getSearchNodes().allocate(), a);
		assertNull(table.findIfPresent(0L));
		assertEquals(e, table.findIfPresent(4L));
		SearchNode x = table.getListNodes().allocate().getKey();
		SearchNode y = table.getListNodes().allocate().getKey();
		assertTrue(((x == a) && (y == b)) || ((x == b) && (y == a)));
	}

	@Test
	public void testDagSize() {
		SearchNode a = table.findOrAllocate(0L);
		SearchNode b = table.findOrAllocate(1L);
		SearchNode c = table.findOrAllocate(2L);
		SearchNode d = table.findOrAllocate(3L);
		SearchNode e = table.findOrAllocate(4L);
		table.addChild(a, b);
		table.addChild(b, c);
		table.addChild(b, d);
		table.addChild(c, e);
		table.addChild(d, e);
		assertEquals(5, table.dagSize(a));
		assertEquals(4, table.dagSize(b));
		assertEquals(2, table.dagSize(c));
		assertEquals(2, table.dagSize(d));
		assertEquals(1, table.dagSize(e));
	}

	@Test
	public void testMinValue() {
		// Math.abs(Integer.MIN_VALUE) is negative, so there is special code to
		// handle it
		long hash = Integer.MIN_VALUE;
		assertNull(table.findIfPresent(hash));
		table.findOrAllocate(hash);
		assertNotNull(table.findIfPresent(hash));
	}

}
