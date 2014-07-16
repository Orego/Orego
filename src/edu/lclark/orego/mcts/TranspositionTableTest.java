package edu.lclark.orego.mcts;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ListNode;

public class TranspositionTableTest {

	private TranspositionTable table;

	@Before
	public void setUp() throws Exception {
		CoordinateSystem coords = CoordinateSystem.forWidth(5);
		table = new TranspositionTable(1, new SimpleSearchNodeBuilder(coords), coords);
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
		SearchNode b = table.findOrAllocate(1 + table.getCapacity());
		SearchNode c = table.findOrAllocate(1);
		SearchNode d = table.findOrAllocate(1 + table.getCapacity());
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
		for (int i = 0; i <= table.getCapacity(); i++) {
			table.findOrAllocate(i);
		}
		assertNull(table.findOrAllocate(table.getCapacity() + 1));
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
		assertNull(table.findIfPresent(0L));
		assertEquals(b, table.findIfPresent(1L));
		assertEquals(c, table.findIfPresent(2L));
		assertEquals(d, table.findIfPresent(3L));
		assertEquals(e, table.findIfPresent(4L));
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
