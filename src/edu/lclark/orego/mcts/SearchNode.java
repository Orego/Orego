package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ListNode;

/** A node in the search "tree". */
public interface SearchNode {

	/**
	 * Returns a human-readable String giving statistics on the move with the
	 * most wins.
	 */
	public String bestWinCountReport(CoordinateSystem coords);

	/**
	 * Returns the win rate of the move with the highest win rate.
	 */
	public short bestWinRate(CoordinateSystem coords);

	/** Returns whether the bias has already been updated for this node. */
	public boolean biasUpdated();

	/**
	 * Resets this node as a "new" node for the board situation represented by
	 * boardHash.
	 */
	public void clear(long fancyHash, CoordinateSystem coords);

	/**
	 * Returns a human-readable representation of the subtree rooted at this
	 * node, up to max depth.
	 */
	public String deepToString(Board board, TranspositionTable table,
			int maxDepth);

	/**
	 * Marks move p (e.g., an illegal move) as being horrible, so it will never
	 * be tried again.
	 */
	public void exclude(short p);

	/** Mark this node as unused until the next time it is reset. */
	public void free();

	/** Returns the (beginning of the linked list of) children of this node. */
	public ListNode<SearchNode> getChildren();

	/**
	 * Returns the fancy Zobrist hash of the board situation stored in this
	 * node.
	 */
	public long getFancyHash();

	/** Returns the move with the most wins from this node. */
	public short getMoveWithMostWins(CoordinateSystem coords);

	/** Returns the number of runs through move p. */
	public int getRuns(short p);

	/** Returns the total number of runs through this node. */
	public int getTotalRuns();

	/**
	 * Returns the last move played from this node if it resulted in a win,
	 * otherwise NO_POINT.
	 */
	public short getWinningMove();

	/** Returns the win rate through this node for move p. */
	public float getWinRate(short p);

	/** Returns the number of wins through move p. */
	public float getWins(short p);

	/**
	 * Returns true for those moves through which another node has already been
	 * created.
	 */
	public boolean hasChild(short p);

	/**
	 * Returns true if this node has not yet experienced any playouts (other
	 * than initial bias playouts).
	 */
	public boolean isFresh(CoordinateSystem coords);

	/**
	 * Returns true if this node is in use (i.e., has been reset since the last
	 * time it was freed).
	 */
	public boolean isInUse();

	/**
	 * True if this node is marked. Used in garbage collection.
	 */
	public boolean isMarked();

	/**
	 * Returns the total ratio of wins to runs for moves from this node. This is
	 * slow.
	 */
	public float overallWinRate(CoordinateSystem coords);

	/**
	 * Increments the counts for a move sequence resulting from a playout.
	 *
	 * NOTE: Since this method is not synchronized, two simultaneous calls on
	 * the same node might result in a race condition affecting which one sets
	 * the winningMove field.
	 *
	 * @param winProportion
	 *            1.0 if this is a winning playout for the player to play at
	 *            this node, 0.0 otherwise.
	 * @param runnable
	 *            The McRunnable responsible for this run.
	 * @param t
	 *            Index of the first move (the one made from this node).
	 */
	public void recordPlayout(float winProportion, McRunnable runnable, int t);

	/** Sets whether the bias has already been updated for this node. */
	public void setBiasUpdated(boolean value);

	/** Sets the child list for this node. */
	public void setChildren(ListNode<SearchNode> children);

	/** Marks move p as visited. */
	public void setHasChild(short p);

	/** Sets the mark of this node for garbage collection. */
	public void setMarked(boolean marked);

	/** Sets the winning move for this node. */
	public void setWinningMove(short move);

	/** Returns a human-readable representation of this node. */
	public String toString(CoordinateSystem coords);

	/**
	 * Updates the win rate for p, by adding n runs and the specified number of
	 * wins. Also updates the counts of total runs and runs.
	 */
	public void update(short p, int n, float wins);

	/**
	 * Provides extra wins for moves suggested by runnable's suggesters.
	 */
	public void updateBias(McRunnable runnable);

}