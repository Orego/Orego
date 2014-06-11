package edu.lclark.orego.mcts;

import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static java.util.Arrays.*;
import static java.lang.String.*;
import edu.lclark.orego.util.*;

/** A node in the search tree / transposition table. */
public final class SearchNode implements Poolable<SearchNode> {

	/** Children of this node. */
	private ListNode<SearchNode> children;

	/**
	 * True for those positions corresponding to moves through which another
	 * node has already been created. Used to determine if a new child is
	 * necessary.
	 */
	private final BitVector hasChild;

	/**
	 * The Zobrist hash of the board position represented by this node. This
	 * incorporates the simple ko position and color to play. Collisions are so
	 * rare that they can be ignored.
	 */
	private long hash;

	/**
	 * Next node in the free list of nodes.
	 * 
	 * @see orego.util.Pool
	 */
	private SearchNode next;

	/** Number of runs through each child of this node. */
	private final int[] runs;

	/** Total number of runs through this node. */
	private int totalRuns;

	/** @see #getWinningMove() */
	private short winningMove;

	/** Number of wins through each child of this node. */
	private final float[] winRates;

	public SearchNode(CoordinateSystem coords) {
		runs = new int[coords.getFirstPointBeyondBoard()];
		winRates = new float[coords.getFirstPointBeyondBoard()];
		hasChild = new BitVector(coords.getFirstPointBeyondBoard());
	}

	/**
	 * Returns a human-readable String giving statistics on the move with the
	 * most wins.
	 */
	public String bestWinCountReport(CoordinateSystem coords) {
		short best = getMoveWithMostWins(coords);
		return coords.toString(best) + " wins " + winRates[best] * runs[best]
				+ "/" + runs[best] + " = " + getWinRate(best);
	}

	/** Returns the win rate of the best move. */
	public float bestWinRate(CoordinateSystem coords) {
		short best = getMoveWithMostWins(coords);
		return getWinRate(best);
	}

	/**
	 * Marks move p (e.g., an illegal move) as being horrible, so it will never
	 * be tried again.
	 */
	public void exclude(short p) {
		// This will ensure that winRates[p]*runs[p] == Integer.MIN_VALUE
		winRates[p] = Integer.MIN_VALUE;
		runs[p] = 1;
	}

	/** Returns the (beginning of the linked list of) children of this node. */
	public ListNode<SearchNode> getChildren() {
		return children;
	}

	// TODO Maybe rename to distinguish this fancy hash (with simple ko point
	// and color to play) from the
	// regular one used by the superko table
	/** Returns the Zobrist hash of the board situation stored in this node. */
	public long getHash() {
		return hash;
	}

	/** Returns the move with the most wins from this node. */
	public short getMoveWithMostWins(CoordinateSystem coords) {
		short best = PASS;
		for (short p : coords.getAllPointsOnBoard()) {
			if (getWins(p) >= getWins(best)) {
				best = p;
			}
		}
		return best;
	}

	@Override
	public SearchNode getNext() {
		return next;
	}

	/** Returns the number of runs through move p. */
	public int getRuns(short p) {
		return runs[p];
	}

	/** Returns the total number of runs through this node. */
	public int getTotalRuns() {
		return totalRuns;
	}

	/**
	 * Returns the last move played from this node if it resulted in a win,
	 * otherwise NO_POINT.
	 */
	public short getWinningMove() {
		return winningMove;
	}

	/** Returns the win rate through this node for move p. */
	public float getWinRate(short p) {
		return winRates[p];
	}

	/** Returns the number of wins through move p. */
	public float getWins(short p) {
		return winRates[p] * runs[p];
	}

	/**
	 * Returns true for those moves through which another node has already been
	 * created.
	 */
	public boolean hasChild(short p) {
		return hasChild.get(p);
	}

	/**
	 * When reset is called, a pass is given this many runs, only one of which
	 * is a win, to discourage passing unless all other moves are awful.
	 */
	private static final int INITIAL_PASS_RUNS = 10;

	/**
	 * Returns true if this node has not yet experienced any playouts (other
	 * than initial bias playouts).
	 */
	public boolean isFresh(CoordinateSystem coords) {
		return totalRuns == 2 * coords.getArea() + INITIAL_PASS_RUNS;
	}

	/**
	 * True if this node is marked. Used in garbage collection.
	 */
	public boolean isMarked() {
		return hasChild.get(NO_POINT);
	}

	/**
	 * Returns the total ratio of wins to runs for moves from this node. This is
	 * slow.
	 */
	public float overallWinRate(CoordinateSystem coords) {
		int r = 0; // Runs
		int w = 0; // Wins
		for (short p : coords.getAllPointsOnBoard()) {
			if (getWins(p) > 0) {
				w += getWins(p);
				r += getRuns(p);
			}
		}
		w += getWins(PASS);
		r += getRuns(PASS);
		return 1.0f * w / r;
	}

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
	 * @param moves
	 *            Sequence of moves made in this playout, including two final
	 *            passes.
	 * @param t
	 *            Index of the first move (the one made from this node).
	 * @param playedPoints
	 *            For keeping track of points played to avoid counting
	 *            already-played points.
	 */
	public void recordPlayout(float winProportion, short[] moves, int t,
			ShortSet playedPoints) {
		short move = moves[t];
		update(move, 1, winProportion);
		if (winProportion == 1) {
			winningMove = move;
		} else {
			winningMove = NO_POINT;
		}
	}

	/**
	 * Resets this node as a "new" node for the board situation represented by
	 * hash.
	 */
	public void reset(long hash, CoordinateSystem coords) {
		this.hash = hash;
		totalRuns = 2 * coords.getArea() + INITIAL_PASS_RUNS;
		fill(runs, (char) 2);
		fill(winRates, 0.5f);
		hasChild.clear();
		// Make passing look very bad, so it will only be tried if all other
		// moves lose
		runs[PASS] = 10;
		winRates[PASS] = 1.0f / INITIAL_PASS_RUNS;
		children = null;
		winningMove = NO_POINT;
	}

	/** Sets the child list for this node. */
	public void setChildren(ListNode<SearchNode> children) {
		this.children = children;
	}

	/** Marks move p as visited. */
	public void setHasChild(int p) {
		hasChild.set(p, true);
	}

	/** Sets the hash code of this node. */
	protected void setHash(long hash) {
		this.hash = hash;
	}

	/** Sets the mark of this node for garbage collection. */
	public void setMarked(boolean marked) {
		hasChild.set(NO_POINT, marked);
	}

	public void setNext(SearchNode next) {
		this.next = next;
	}

	/** Sets the total number of runs in this node. */
	protected void setTotalRuns(int totalRuns) {
		this.totalRuns = totalRuns;
	}

	/** Returns a human-readable representation of this node. */
	public String toString(CoordinateSystem coords) {
		String result = "Total runs: " + totalRuns + "\n";
		for (short p : coords.getAllPointsOnBoard()) {
			if (runs[p] > 2) {
				result += toString(p, coords);
			}
		}
		if (runs[PASS] > 10) {
			result += toString(PASS, coords);
		}
		return result;
	}

	/**
	 * Returns a human-readable representation of the information stored for
	 * move p.
	 */
	public String toString(short p, CoordinateSystem coords) {
		return format("%s: %7d/%7d (%1.4f)\n", coords.toString(p),
				(int) getWins(p), (int) runs[p], winRates[p]);
	}

	/**
	 * Update the win rate for p, by adding the specified number of wins and n
	 * runs. Also updates the counts of total runs and runs.
	 */
	public synchronized void update(short p, int n, float wins) {
		totalRuns += n;
		winRates[p] = (wins + winRates[p] * runs[p]) / (n + runs[p]);
		runs[p] += n;
	}

}
