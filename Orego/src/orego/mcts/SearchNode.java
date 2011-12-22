package orego.mcts;

import static orego.core.Coordinates.*;
import static java.util.Arrays.*;
import static java.lang.String.*;
import orego.util.*;

/** A node in the search tree / transposition table. */
public class SearchNode implements Poolable<SearchNode> {

	/** Children of this node. */
	private ListNode<SearchNode> children;

	/**
	 * True for those positions corresponding to moves through which another
	 * node has already been created. Used to determine if a new child is
	 * necessary.
	 */
	private BitVector hasChild;

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
	private int[] runs;

	/** Total number of runs through this node. */
	private int totalRuns;

	/**
	 * The last move played from this node if it resulted in a win, otherwise
	 * NO_POINT.
	 */
	private int winningMove;

	/** Number of wins through each child of this node. */
	private int[] wins;

	public SearchNode() {
		runs = new int[LAST_POINT_ON_BOARD + 1];
		wins = new int[LAST_POINT_ON_BOARD + 1];
		hasChild = new BitVector(LAST_POINT_ON_BOARD + 1);
	}

	/** Adds n losses for p. */
	public void addLosses(int p, int n) {
		totalRuns += n;
		runs[p] += n;
	}

	/** Adds n wins for p, e.g., as a prior due to a heuristic. */
	public synchronized void addWins(int p, int n) {
		totalRuns += n;
		runs[p] += n;
		wins[p] += n;
	}

	/**
	 * Returns a human-readable String giving statistics on the move with the
	 * most wins.
	 */
	public String bestWinCountReport() {
		int best = getMoveWithMostWins();
		return pointToString(best) + " wins " + wins[best] + "/" + runs[best]
				+ " = " + getWinRate(best);
	}

	/** Returns the win rate of the best move. */
	public double bestWinRate() {
		int best = getMoveWithMostWins();
		return getWinRate(best);
	}

	/**
	 * Marks move p (e.g., an illegal move) as being horrible, so it will never
	 * be tried again.
	 */
	public void exclude(int p) {
		wins[p] = Integer.MIN_VALUE;
	}

	/** Returns the (beginning of the linked list of) children of this node. */
	public ListNode<SearchNode> getChildren() {
		return children;
	}

	/** Returns the BitVector indicating which children this node has. */
	protected BitVector getHasChild() {
		return hasChild;
	}

	/** Returns the Zobrist hash of the board situation stored in this node. */
	public long getHash() {
		return hash;
	}

	/** Returns the move with the most wins from this node. */
	public int getMoveWithMostWins() {
		int best = PASS;
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getWins(p) >= getWins(best)) {
				best = p;
			}
		}
		return best;
	}

	public SearchNode getNext() {
		return next;
	}

	/** Returns the number of runs through move p. */
	public int getRuns(int p) {
		return runs[p];
	}

	/** Returns an array of the runs where the index is the move */
	public int[] getRunsArray() {
		return runs;
	}

	/** Returns the total number of runs through this node. */
	public int getTotalRuns() {
		return totalRuns;
	}

	public int getWinningMove() {
		return winningMove;
	}

	/** Returns the win rate through this node for move p. */
	public double getWinRate(int p) {
		double w = (double) wins[p];
		double r = runs[p];
		double result = w / r;
		assert result <= 1.0 : "Invalid win rate for " + pointToString(p)
				+ ": " + w + "/" + r + "=" + result;
		return result;
	}

	/** Returns the number of wins through move p. */
	public int getWins(int p) {
		return wins[p];
	}

	/** Returns an array of the wins where the index is the move */
	protected int[] getWinsArray() {
		return wins;
	}

	/**
	 * Returns true for those moves through which another node has already been
	 * created.
	 */
	public boolean hasChild(int p) {
		return hasChild.get(p);
	}

	/** For testing only. */
	public void incrementTotalRuns() {
		totalRuns++;
	}

	/**
	 * Returns true if this node has not yet experienced any playouts (other
	 * than initial bias playouts).
	 */
	public boolean isFresh() {
		return totalRuns == 2 * BOARD_AREA + 10;
	}

	/**
	 * True if this node is marked. Used in garbage collection.
	 * 
	 * @see orego.mcts.TranspositionTable
	 */
	public boolean isMarked() {
		return hasChild.get(NO_POINT);
	}

	/**
	 * Returns the total ratio of wins to runs for moves from this node. This is
	 * slow.
	 */
	public double overallWinRate() {
		int runs = 0;
		int wins = 0;
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getWins(p) > 0) {
				wins += getWins(p);
				runs += getRuns(p);
			}
		}
		wins += getWins(PASS);
		runs += getRuns(PASS);
		return 1.0 * wins / runs;
	}

	/**
	 * Increments the counts for a move sequence resulting from a playout.
	 * 
	 * @param win
	 *            True if this is a winning playout for the player to play at
	 *            this node.
	 * @param moves
	 *            Sequence of moves made in this playout, including two final
	 *            passes.
	 * @param t
	 *            Index of the first move (the one made from this node).
	 * @param turn
	 *            Index right after the last move played.
	 * @param playedPoints
	 *            For keeping track of points played to avoid counting
	 *            already-played points. (Used by, e.g., RavePlayer.)
	 */
	public synchronized void recordPlayout(boolean win, int[] moves, int t,
			int turn, IntSet playedPoints) {
		assert t < turn;
		int move = moves[t];
		totalRuns++;
		runs[move]++;
		if (win) {
			wins[move]++;
			winningMove = move;
		} else {
			winningMove = NO_POINT;
		}
	}

	/**
	 * Resets this node as a "new" node for the board situation represented by
	 * hash.
	 */
	public void reset(long hash) {
		this.hash = hash;
		totalRuns = 2 * BOARD_AREA + 10;
		fill(runs, (char) 2);
		fill(wins, (char) 1);
		hasChild.clear();
		// Make passing look very bad, so it will only be tried if all other
		// moves lose
		runs[PASS] = 10;
		wins[PASS] = 1;
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

	@Override
	public String toString() {
		String result = "Total runs: " + totalRuns + "\n";
		for (int p : ALL_POINTS_ON_BOARD) {
			if (runs[p] > 2) {
				result += toString(p);
			}
		}
		if (runs[PASS] > 10) {
			result += toString(PASS);
		}
		return result;
	}

	/**
	 * Returns a human-readable representation of the information stored for
	 * move p.
	 */
	public String toString(int p) {
		return format("%s: %7d/%7d (%1.4f)\n", pointToString(p), (int) wins[p],
				(int) runs[p], ((double) wins[p]) / runs[p]);
	}

}
