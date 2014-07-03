package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static java.lang.String.format;

import java.util.Arrays;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ListNode;
import edu.lclark.orego.util.ShortSet;

public final class RaveNode implements SearchNode {

	/** Underlying SearchNode to delegate methods to. */
	private final SearchNode node;

	/** Number of RAVE runs through each child of this node. */
	private final int[] raveRuns;

	/** RAVE winrate through the children of this node. */
	private final float[] raveWinRates;

	public RaveNode(CoordinateSystem coords) {
		node = new SimpleSearchNode(coords);
		raveRuns = new int[coords.getFirstPointBeyondBoard()];
		raveWinRates = new float[coords.getFirstPointBeyondBoard()];
	}

	public void addRaveLoss(short p) {
		addRaveRun(p, 0);
	}

	public void addRaveRun(int p, float w) {
		raveWinRates[p] = (w + raveWinRates[p] * raveRuns[p])
				/ (1 + raveRuns[p]);
		raveRuns[p]++;
	}

	public void addRaveWin(short p) {
		addRaveRun(p, 1);
	}

	@Override
	public String bestWinCountReport(CoordinateSystem coords) {
		return node.bestWinCountReport(coords);
	}

	@Override
	public void clear(long fancyHash, CoordinateSystem coords) {
		node.clear(fancyHash, coords);
		Arrays.fill(raveRuns, 2);
		Arrays.fill(raveWinRates, 0.5f);
	}

	@Override
	public String deepToString(Board board, TranspositionTable table,
			int maxDepth) {
		return deepToString(board, table, maxDepth, 0);
	}

	private String deepToString(Board board, TranspositionTable table,
			int maxDepth, int depth) {
		CoordinateSystem coords = board.getCoordinateSystem();
		if (maxDepth < depth) {
			return "";
		}
		String indent = "";
		for (int i = 0; i < depth; i++) {
			indent += "  ";
		}
		String result = indent + "Total runs: " + getTotalRuns() + "\n";
		Board childBoard = new Board(coords.getWidth());
		for (short p : coords.getAllPointsOnBoard()) {
			if (hasChild(p)) {
				result += indent + toString(p, coords);
				childBoard.copyDataFrom(board);
				childBoard.play(p);
				// TODO Ugly cast
				RaveNode child = (RaveNode) table.findIfPresent(childBoard
						.getFancyHash());
				if (child != null) {
					result += child.deepToString(childBoard, table, maxDepth,
							depth + 1);
				}
			}
		}
		short p = PASS;
		if (hasChild(p)) {
			result += indent + toString(p, coords);
			childBoard.copyDataFrom(board);
			childBoard.play(p);
			SearchNode child = table.findIfPresent(childBoard.getFancyHash());
			if (child != null) {
				result += deepToString(childBoard, table, maxDepth, depth + 1);
			}
		}
		return result;
	}

	@Override
	public void exclude(short p) {
		node.exclude(p);
	}

	@Override
	public void free() {
		node.free();
	}

	@Override
	public ListNode<SearchNode> getChildren() {
		return node.getChildren();
	}

	@Override
	public long getFancyHash() {
		return node.getFancyHash();
	}

	@Override
	public short getMoveWithMostWins(CoordinateSystem coords) {
		return node.getMoveWithMostWins(coords);
	}

	/** Returns the number of RAVE runs through move p. */
	public int getRaveRuns(short p) {
		return raveRuns[p];
	}

	/** Returns the RAVE win rate for move p. */
	public float getRaveWinRate(short p) {
		return raveWinRates[p];
	}

	/** Returns the number of RAVE wins through move p. */
	public float getRaveWins(int p) {
		return raveWinRates[p] * raveRuns[p];
	}

	@Override
	public int getRuns(short p) {
		return node.getRuns(p);
	}

	@Override
	public int getTotalRuns() {
		return node.getTotalRuns();
	}

	@Override
	public short getWinningMove() {
		return node.getWinningMove();
	}

	@Override
	public float getWinRate(short p) {
		return node.getWinRate(p);
	}

	@Override
	public float getWins(short p) {
		return node.getWins(p);
	}

	@Override
	public boolean hasChild(short p) {
		return node.hasChild(p);
	}

	@Override
	public boolean isFresh(CoordinateSystem coords) {
		return node.isFresh(coords);
	}

	@Override
	public boolean isInUse() {
		return node.isInUse();
	}

	@Override
	public boolean isMarked() {
		return node.isMarked();
	}

	@Override
	public float overallWinRate(CoordinateSystem coords) {
		return node.overallWinRate(coords);
		// TODO Test this in RAVE node?
	}

	@Override
	public boolean priorsUpdated() {
		return node.priorsUpdated();
	}

	@Override
	public void recordPlayout(float winProportion, McRunnable runnable, int t) {
		ShortSet playedPoints = runnable.getPlayedPoints();
		playedPoints.clear();
		node.recordPlayout(winProportion, runnable, t);
		// The remaining moves in the sequence are recorded for RAVE
		while (t < runnable.getTurn()) {
			short move = runnable.getHistoryObserver().get(t);
			if ((move != PASS) && !playedPoints.contains(move)) {
				assert runnable.getBoard().getCoordinateSystem()
						.isOnBoard(move);
				playedPoints.addKnownAbsent(move);
				addRaveRun(move, winProportion);
			}
			t++;
			if (t >= runnable.getTurn()) {
				return;
			}
			move = runnable.getHistoryObserver().get(t);
			playedPoints.add(move);
			t++;
		}
	}

	/**
	 * Similar to the public version, but takes simpler pieces as arguments, to
	 * simplify testing.
	 */
	void recordPlayout(float winProportion, short[] moves, int t, int turn,
			ShortSet playedPoints) {
		assert t < turn;
		short move = moves[t];
		update(move, 1, winProportion);
		while (t < turn) {
			move = moves[t];
			if ((move != PASS) && !playedPoints.contains(move)) {
				playedPoints.addKnownAbsent(move);
				addRaveRun(move, winProportion);
			}
			t++;
			if (t >= turn) {
				return;
			}
			move = moves[t];
			playedPoints.add(move);
			t++;
		}
	}

	@Override
	public void setChildren(ListNode<SearchNode> children) {
		node.setChildren(children);
	}

	@Override
	public void setHasChild(short p) {
		node.setHasChild(p);
	}

	@Override
	public void setMarked(boolean marked) {
		node.setMarked(marked);
	}

	@Override
	public void setPriorsUpdated(boolean value) {
		node.setPriorsUpdated(value);
	}

	@Override
	public String toString(CoordinateSystem coords) {
		String result = "Total runs: " + node.getTotalRuns() + "\n";
		for (short p : coords.getAllPointsOnBoard()) {
			if (node.getRuns(p) > 2) {
				result += toString(p, coords);
			}
		}
		if (node.getRuns(PASS) > 10) {
			result += toString(PASS, coords);
		}
		return result;
	}

	@SuppressWarnings("boxing")
	String toString(short p, CoordinateSystem coords) {
		return format("%s: %7d/%7d (%1.4f) RAVE %d (%1.4f)\n",
				coords.toString(p), (int) getWins(p), node.getRuns(p),
				node.getWinRate(p), raveRuns[p], raveWinRates[p]);
	}

	@Override
	public void update(short p, int n, float wins) {
		node.update(p, n, wins);
	}

	@Override
	public void updatePriors(McRunnable runnable) {
		node.updatePriors(runnable);
	}

}
