package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static java.util.Arrays.*;
import static java.lang.String.*;
import edu.lclark.orego.util.*;

/** A node in the search tree / transposition table. */
public final class SimpleSearchNode implements SearchNode {

	/**
	 * When reset is called, a pass is given this many runs, only one of which
	 * is a win, to discourage passing unless all other moves are awful.
	 */
	private static final int INITIAL_PASS_RUNS = 10;

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
	private long fancyHash;

	/** Number of runs through each child of this node. */
	private final int[] runs;

	/**
	 * Total number of runs through this node. For not-in-use nodes this is set
	 * to -1.
	 */
	private int totalRuns;

	/** @see #getWinningMove() */
	private short winningMove;

	/** Number of wins through each child of this node. */
	private final float[] winRates;

	public SimpleSearchNode(CoordinateSystem coords) {
		runs = new int[coords.getFirstPointBeyondBoard()];
		winRates = new float[coords.getFirstPointBeyondBoard()];
		hasChild = new BitVector(coords.getFirstPointBeyondBoard());
		totalRuns = -1; // Indicates this node is not in use
	}

	@Override
	public String bestWinCountReport(CoordinateSystem coords) {
		short best = getMoveWithMostWins(coords);
		return coords.toString(best) + " wins " + winRates[best] * runs[best]
				+ "/" + runs[best] + " = " + getWinRate(best);
	}

	@Override
	public float bestWinRate(CoordinateSystem coords) {
		short best = getMoveWithMostWins(coords);
		return getWinRate(best);
	}

	@Override
	public void exclude(short p) {
		// This will ensure that winRates[p]*runs[p] == Integer.MIN_VALUE
		winRates[p] = Integer.MIN_VALUE;
		runs[p] = 1;
	}

	@Override
	public void free() {
		totalRuns = -1;
	}

	@Override
	public ListNode<SearchNode> getChildren() {
		return children;
	}

	@Override
	public long getFancyHash() {
		return fancyHash;
	}

	@Override
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
	public int getRuns(short p) {
		return runs[p];
	}

	@Override
	public int getTotalRuns() {
		return totalRuns;
	}

	@Override
	public short getWinningMove() {
		return winningMove;
	}

	@Override
	public float getWinRate(short p) {
		return winRates[p];
	}

	@Override
	public float getWins(short p) {
		return winRates[p] * runs[p];
	}

	@Override
	public boolean hasChild(short p) {
		return hasChild.get(p);
	}

	@Override
	public boolean isFresh(CoordinateSystem coords) {
		return totalRuns == 2 * coords.getArea() + INITIAL_PASS_RUNS;
	}

	@Override
	public boolean isInUse() {
		return totalRuns >= 0;
	}

	@Override
	public boolean isMarked() {
		return hasChild.get(NO_POINT);
	}

	@Override
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

	@Override
	public void recordPlayout(float winProportion, McRunnable runnable, int t) {
		int turn = runnable.getTurn();
		HistoryObserver history = runnable.getHistoryObserver();
		assert t < turn;
		short move = history.get(t);
		update(move, 1, winProportion);
		if (winProportion == 1) {
			winningMove = move;
		} else {
			winningMove = NO_POINT;
		}		
	}
	
	/**
	 * (Similar to the public version, but takes simpler pieces as arguments, to simplify testing.
	 * 
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
	 * @param turn
	 *            Index right after the last move played.
	 * @param playedPoints
	 *            For keeping track of points played to avoid counting
	 *            already-played points.
	 */
	void recordPlayout(float winProportion, short[] moves, int t, int turn, ShortSet playedPoints) {
		// TODO Is this unnecessarily redundant?
		assert t < turn;
		short move = moves[t];
		update(move, 1, winProportion);
		if (winProportion == 1) {
			winningMove = move;
		} else {
			winningMove = NO_POINT;
		}		
	}

	@Override
	public void clear(@SuppressWarnings("hiding") long fancyHash, CoordinateSystem coords) {
		this.fancyHash = fancyHash;
		totalRuns = 2 * coords.getArea() + INITIAL_PASS_RUNS;
		fill(runs, 2);
		fill(winRates, 0.5f);
		hasChild.clear();
		// Make passing look very bad, so it will only be tried if all other
		// moves lose
		runs[PASS] = 10;
		winRates[PASS] = 1.0f / INITIAL_PASS_RUNS;
		children = null;
		winningMove = NO_POINT;
	}

	@Override
	public void setChildren(ListNode<SearchNode> children) {
		this.children = children;
	}

	@Override
	public void setHasChild(short p) {
		hasChild.set(p, true);
	}

	@Override
	public void setMarked(boolean marked) {
		hasChild.set(NO_POINT, marked);
	}

	@Override
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

	@SuppressWarnings("boxing")
	String toString(short p, CoordinateSystem coords) {
		return format("%s: %7d/%7d (%1.4f)\n", coords.toString(p),
				(int) getWins(p), runs[p], winRates[p]);
	}

	@Override
	public String deepToString(Board board, TranspositionTable table, int maxDepth) {
		return deepToString(board, table, maxDepth, 0);
	}
	
	String deepToString(Board board, TranspositionTable table, int maxDepth, int depth) {
		CoordinateSystem coords = board.getCoordinateSystem();
		if (maxDepth < depth) {
			return "";
		}
		String indent = "";
		for (int i = 0; i < depth; i++) {
			indent += "  ";
		}
		String result = indent + "Total runs: "
				+ getTotalRuns() + "\n";
		Board childBoard = new Board(coords.getWidth());
		for (short p : coords.getAllPointsOnBoard()) {
			if (hasChild(p)) {
				result += indent + toString(p, coords);
				childBoard.copyDataFrom(board);
				childBoard.play(p);
				// TODO Ugly cast
				SimpleSearchNode child = (SimpleSearchNode)table.findIfPresent(childBoard.getFancyHash());
				if (child != null) {
					result += child.deepToString(childBoard, table, maxDepth, depth + 1);
				}
			}
		}
		short p = PASS;
		if (hasChild(p)) {
			result += indent + toString(p, coords);
			childBoard.copyDataFrom(board);
			childBoard.play(p);
			SearchNode child = table.findIfPresent(childBoard.getHash());
			if (child != null) {
				result += deepToString(childBoard, table, maxDepth, depth + 1);
			}
		}
		return result;		
	}
	
	@Override
	public synchronized void update(short p, int n, float wins) {
		totalRuns += n;
		winRates[p] = (wins + winRates[p] * runs[p]) / (n + runs[p]);
		runs[p] += n;
	}

}
