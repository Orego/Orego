package orego.mcts;

import static orego.core.Coordinates.BOARD_AREA;
import static orego.core.SuperKoTable.IGNORE_SIGN_BIT;
import static orego.experiment.Debug.debug;
import orego.util.ListNode;
import orego.util.Pool;

// TODO Why are we using chaining instead of open addressing here?
/** A hash table of nodes representing board configurations. */
public class TranspositionTable {

	/** Number of nodes to allocate in general. */
	public static final int DEFAULT_NODE_POOL_SIZE = 1024 * 1024 * 20 / BOARD_AREA;

	/** ListNodes used to build child lists for SearchNodes. */
	protected Pool<ListNode<SearchNode>> listNodes;

	/** Search nodes. */
	protected Pool<SearchNode> searchNodes;

	/** The hash table itself. */
	protected ListNode<SearchNode>[] table;

	@SuppressWarnings("unchecked")
	public TranspositionTable(int size, SearchNode prototype) {
		debug("Allocating " + size + " search nodes");
		// The first line below would produce a warning if not suppressed,
		// because generic types do not play well with arrays
		table = new ListNode[size];
		searchNodes = new Pool<SearchNode>();
		for (int i = 0; i < size; i++) {
			try {
				searchNodes.free(prototype.getClass().newInstance());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		listNodes = new Pool<ListNode<SearchNode>>();
		for (int i = 0; i < 3 * size; i++) {
			listNodes.free(new ListNode<SearchNode>());
		}
		debug("Allocated " + size + " search nodes");
	}

	public TranspositionTable(SearchNode prototype) {
		this(DEFAULT_NODE_POOL_SIZE, prototype);
	}

	/** Adds child as a child of parent. */
	public void addChild(SearchNode parent, SearchNode child) {
		ListNode<SearchNode> node;
		synchronized (this) {
			node = listNodes.allocate();
			node.setKey(child);
			node.setNext(parent.getChildren());
			parent.setChildren(node);
		}
	}

	/** Slow -- for testing only. Returns the number of nodes reachable from the root. */
	public int dagSize(SearchNode root) {
		java.util.Set<SearchNode> visited = new java.util.HashSet<SearchNode>();
		return dagSize(root, visited);
	}

	/** Called by the one-argument version of dagSize(). */
	protected int dagSize(SearchNode root, java.util.Set<SearchNode> visited) {
		if (visited.contains(root)) {
			return 0;
		} else {
			visited.add(root);
			int sum = 1;
			ListNode<SearchNode> child = root.getChildren();
			while (child != null) {
				sum += dagSize(child.getKey(), visited);
				child = child.getNext();
			}
			return sum;
		}
	}

	/** Returns the node associated with hash, or null if there is no such node. */
	public synchronized SearchNode findIfPresent(long hash) {
		int slot = (((int) hash) & IGNORE_SIGN_BIT) % table.length;
		// abs(Long.MIN_VALUE) returns a negative number! The next line deals with this.
		if (slot < 0) {
			slot = table.length - 1;
		}
		ListNode<SearchNode> listNode = table[slot];
		while (listNode != null) {
			if (listNode.getKey().getHash() == hash) {
				return listNode.getKey();
			}
			listNode = listNode.getNext();
		}
		return null;
	}

	/**
	 * Returns the node associated with hash in the table, if any. If not,
	 * allocates and returns a new node from the pool. If no nodes are available
	 * in the pool, returns null.
	 */
	public synchronized SearchNode findOrAllocate(long hash) {
		int slot = (((int) hash) & IGNORE_SIGN_BIT) % table.length;
		// abs(Long.MIN_VALUE) returns a negative number! The next line deals with this.
		if (slot < 0) {
			slot = table.length - 1;
		}
		ListNode<SearchNode> listNode = table[slot];
		while (listNode != null) {
			if (listNode.getKey().getHash() == hash) {
				return listNode.getKey();
			}
			listNode = listNode.getNext();
		}
		// Didn't find it; allocate a node if possible
		if (searchNodes.isEmpty() | listNodes.isEmpty()) {
			return null;
		}
		ListNode<SearchNode> newListNode = listNodes.allocate();
		SearchNode newNode = searchNodes.allocate();
		newNode.reset(hash);
		newListNode.setKey(newNode);
		newListNode.setNext(table[slot]);
		table[slot] = newListNode;
		return newNode;
	}

	/** Returns the pool of ListNodes. For testing only. */
	protected Pool<ListNode<SearchNode>> getListNodes() {
		return listNodes;
	}

	/** Returns the pool of SearchNodes. For testing only. */
	protected Pool<SearchNode> getSearchNodes() {
		return searchNodes;
	}

	/** Marks all nodes reachable from root, so they will survive sweep(). */
	protected void markNodesReachableFrom(SearchNode root) {
		if (!root.isMarked()) {
			root.setMarked(true);
			ListNode<SearchNode> child = root.getChildren();
			while (child != null) {
				markNodesReachableFrom(child.getKey());
				child = child.getNext();
			}
		}
	}

	/**
	 * After markNodesUnreachableFrom(), returns all unmarked SearchNodes (and
	 * associated ListNodes) to their pools.
	 */
	public void sweep() {
		for (int i = 0; i < table.length; i++) {
			if (table[i] != null) {
				ListNode<SearchNode> prev = null;
				ListNode<SearchNode> node = table[i];
				while (node != null) {
					SearchNode searchNode = node.getKey();
					if (searchNode.isMarked()) {
						// Clear the mark for the next pass
						searchNode.setMarked(false);
						prev = node;
						node = node.getNext();
					} else {
						// Reclaim the ListNodes in the SearchNode's child list
						ListNode<SearchNode> n = searchNode.getChildren();
						while (n != null) {
							n = listNodes.free(n);
						}
						// Reclaim the SearchNode itself
						searchNodes.free(searchNode);
						// Reclaim this ListNode
						node = listNodes.free(node);
						if (prev == null) {
							table[i] = node;
						} else {
							prev.setNext(node);
						}
					}
				}
			}
		}
	}

}
