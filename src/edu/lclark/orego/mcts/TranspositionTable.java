package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.SuperKoTable.IGNORE_SIGN_BIT;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.experiment.Logging;
import edu.lclark.orego.util.ListNode;
import edu.lclark.orego.util.Pool;

/** A hash table of nodes representing board configurations. */
public final class TranspositionTable {

	private final CoordinateSystem coords;

	/** ListNodes used to build child lists for SearchNodes. */
	private final Pool<ListNode<SearchNode>> listNodes;

	/** The hash table itself. */
	private final SearchNode[] table;
	
	private int nodesInUse;

	public TranspositionTable(int megabytes, SearchNodeBuilder builder,
			CoordinateSystem coords) {
		final int size = megabytes * 1024 * 16 / Math.max(81, coords.getArea());
		table = new SearchNode[size];
		nodesInUse = 0;
		for (int i = 0; i < size; i++) {
			table[i] = builder.build();
		}
		listNodes = new Pool<>();
		for (int i = 0; i < 3 * size; i++) {
			listNodes.free(new ListNode<SearchNode>());
		}
		this.coords = coords;
	}

	/** Adds child as a child of parent. */
	void addChild(SearchNode parent, SearchNode child) {
		final ListNode<SearchNode> node = listNodes.allocate();
		node.setKey(child);
		node.setNext(parent.getChildren());
		parent.setChildren(node);
	}

	/**
	 * Slow -- for testing only. Returns the number of nodes reachable from the
	 * root.
	 */
	public int dagSize(SearchNode root) {
		final int result = markNodesReachableFrom(root);
		for (int i = 0; i < table.length; i++) {
			table[i].setMarked(false);
		}
		return result;
	}

	/** Returns the node associated with hash, or null if there is no such node. */
	public synchronized SearchNode findIfPresent(long fancyHash) {
		final int start = ((int) fancyHash & IGNORE_SIGN_BIT) % table.length;
		int slot = start;
		do {
			final SearchNode n = table[slot];
			if (n.isInUse()) {
				if (n.getFancyHash() == fancyHash) {
					return n;
				}
			} else {
				return null;
			}
			slot = (slot + 1) % table.length;
		} while (slot != start);
		return null;
	}

	/**
	 * Returns the node associated with hash in the table, if any. If not,
	 * allocates and returns a new node from the pool. If no nodes are available
	 * in the pool, returns null.
	 */
	synchronized SearchNode findOrAllocate(long fancyHash) {
		final int start = ((int) fancyHash & IGNORE_SIGN_BIT) % table.length;
		int slot = start;
		do {
			final SearchNode n = table[slot];
			if (n.isInUse()) {
				if (n.getFancyHash() == fancyHash) {
					return n;
				}
			} else {
				n.clear(fancyHash, coords);
				nodesInUse++;
				return n;
			}
			slot = (slot + 1) % table.length;
		} while (slot != start);
		return null;
	}

	/** Returns the number of nodes in the table. For testing. */
	int getCapacity() {
		return table.length;
	}

	/**
	 * Marks all nodes reachable from root, so they will survive sweep().
	 * Returns the number of nodes marked.
	 */
	int markNodesReachableFrom(SearchNode root) {
		if (root == null || root.isMarked()) {
			return 0;
		}
		root.setMarked(true);
		int sum = 1;
		ListNode<SearchNode> child = root.getChildren();
		while (child != null) {
			sum += markNodesReachableFrom(child.getKey());
			child = child.getNext();
		}
		return sum;
	}

	/** Returns the number of table nodes currently in use. */
	int getNodesInUse() {
		return nodesInUse;
	}
	
	/**
	 * After markNodesUnreachableFrom(), frees all unused SearchNodes (tagging
	 * them as not in use) and associated ListNodes (returning them to the
	 * pool).
	 */
	void sweep() {
		Logging.log("Nodes in use " + nodesInUse + "/" + table.length + " (" + (nodesInUse* 100)/table.length  + "%)");
		for (int i = 0; i < table.length; i++) {
			final SearchNode node = table[i];
			if (node.isInUse()) {
				if (node.isMarked()) {
					node.setMarked(false);
				} else {
					ListNode<SearchNode> n = node.getChildren();
					while (n != null) {
						n = listNodes.free(n);
					}
					node.free();
					nodesInUse--;
				}
			}
		}
	}

}
