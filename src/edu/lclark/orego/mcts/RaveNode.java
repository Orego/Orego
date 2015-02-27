package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static java.lang.String.format;

import java.util.Arrays;

import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;

/** Incorporates RAVE information. */
public final class RaveNode extends SimpleSearchNode {

	/** Number of RAVE runs through each child of this node. */
	private final int[] raveRuns;

	/** RAVE winrate through the children of this node. */
	private final float[] raveWinRates;

	public RaveNode(CoordinateSystem coords) {
		super(coords);
		raveRuns = new int[coords.getFirstPointBeyondBoard()];
		raveWinRates = new float[coords.getFirstPointBeyondBoard()];
	}

	/** Adds a RAVE loss for p. */
	public void addRaveLoss(short p) {
		addRaveRun(p, 0);
	}

	/**
	 * Adds one RAVE playout for p.
	 * 
	 * @param w The win rate for this playout, usually 0 for a loss or 1 for a win.
	 */
	public void addRaveRun(int p, float w) {
		raveWinRates[p] = (w + raveWinRates[p] * raveRuns[p])
				/ (1 + raveRuns[p]);
		raveRuns[p]++;
	}

	/** Adds a RAVE win for p. */
	public void addRaveWin(short p) {
		addRaveRun(p, 1);
	}

	@Override
	public void clear(long fancyHash, CoordinateSystem coords) {
		super.clear(fancyHash, coords);
		Arrays.fill(raveRuns, 2);
		Arrays.fill(raveWinRates, 0.5f);
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
	public void recordPlayout(float winProportion, McRunnable runnable, int t) {
		final ShortSet playedPoints = runnable.getPlayedPoints();
		super.recordPlayout(winProportion, runnable, t);
		playedPoints.clear();
		// The remaining moves in the sequence are recorded for RAVE
		while (t < runnable.getTurn()) {
			short move = runnable.getHistoryObserver().get(t);
			if (move != PASS && !playedPoints.contains(move)) {
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
			if (move != PASS && !playedPoints.contains(move)) {
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
	public String toString(CoordinateSystem coords) {
		String result = "Total runs: " + super.getTotalRuns() + "\n";
		for (final short p : coords.getAllPointsOnBoard()) {
			if (super.getRuns(p) > 2) {
				result += toString(p, coords);
			}
		}
		if (super.getRuns(PASS) > 10) {
			result += toString(PASS, coords);
		}
		return result;
	}

	@Override
	@SuppressWarnings("boxing")
	String toString(short p, CoordinateSystem coords) {
		return format("%s: %7d/%7d (%1.4f) RAVE %d (%1.4f)\n",
				coords.toString(p), (int) getWins(p), super.getRuns(p),
				super.getWinRate(p), raveRuns[p], raveWinRates[p]);
	}

}
