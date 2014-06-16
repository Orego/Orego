package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.SuperKoTable.*;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.*;

/** A hash table of nodes representing board configurations. */
final class TranspositionTable {

	/** ListNodes used to build child lists for SearchNodes. */
	private final Pool<ListNode<SearchNode>> listNodes;

	/** The hash table itself. */
	private final SearchNode[] table;

	private final CoordinateSystem coords;

	TranspositionTable(int size, SearchNodeBuilder builder,
			CoordinateSystem coords) {
		table = new SearchNode[size];
		for (int i = 0; i < size; i++) {
			table[i] = builder.build();
		}
		listNodes = new Pool<>();
		for (int i = 0; i < 3 * size; i++) {
			listNodes.free(new ListNode<SearchNode>());
		}
		this.coords = coords;
	}

	TranspositionTable(SearchNodeBuilder builder, CoordinateSystem coords) {
		/**
		 * The calculation here is for the number of nodes to allocate in
		 * general
		 */
		this(1024 * 1024 * 20 / coords.getArea(), builder, coords);
	}

	/** Adds child as a child of parent. */
	void addChild(SearchNode parent, SearchNode child) {
		ListNode<SearchNode> node = listNodes.allocate();
		node.setKey(child);
		node.setNext(parent.getChildren());
		parent.setChildren(node);
	}

	/**
	 * Slow -- for testing only. Returns the number of nodes reachable from the
	 * root.
	 */
	int dagSize(SearchNode root) {
		int result = markNodesReachableFrom(root);
		for (int i = 0; i < table.length; i++) {
			table[i].setMarked(false);
		}
		return result;
	}

	/** Returns the node associated with hash, or null if there is no such node. */
	synchronized SearchNode findIfPresent(long fancyHash) {
		int start = (((int) fancyHash) & IGNORE_SIGN_BIT) % table.length;
		int slot = start;
		do {
			SearchNode n = table[slot];
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
		int start = (((int) fancyHash) & IGNORE_SIGN_BIT) % table.length;
		int slot = start;
		do {
			SearchNode n = table[slot];
			if (n.isInUse()) {
				if (n.getFancyHash() == fancyHash) {
					return n;
				}
			} else {
				n.reset(fancyHash, coords);
				return n;
			}
			slot = (slot + 1) % table.length;
		} while (slot != start);
		return null;
	}

	/** Returns the pool of ListNodes. For testing only. */
	Pool<ListNode<SearchNode>> getListNodes() {
		return listNodes;
	}

	/** Marks all nodes reachable from root, so they will survive sweep(). Returns the number of nodes marked. */
	int markNodesReachableFrom(SearchNode root) {
		if (root.isMarked()) {
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

	/**
	 * After markNodesUnreachableFrom(), frees all unused SearchNodes (tagging
	 * them as not in use) and associated ListNodes (returning them to the
	 * pool).
	 */
	void sweep() {
		for (int i = 0; i < table.length; i++) {
			SearchNode node = table[i];
			if (node.isInUse()) {
				if (node.isMarked()) {
					node.setMarked(false);
				} else {
					ListNode<SearchNode> n = node.getChildren();
					while (n != null) {
						n = listNodes.free(n);
					}
					node.free();
				}
			}
		}
	}

}
