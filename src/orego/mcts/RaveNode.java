package orego.mcts;

import static java.lang.String.format;
import static java.util.Arrays.fill;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.pointToString;
import orego.util.IntSet;

public class RaveNode extends SearchNode {

	/** Number of RAVE runs through each child of this node. */
	private int[] raveRuns;

	/** Number of RAVE wins through each child of this node. */
	private double[] raveWinRates;

	public RaveNode() {
		raveRuns = new int[FIRST_POINT_BEYOND_BOARD];
		raveWinRates = new double[FIRST_POINT_BEYOND_BOARD];
	}

	public void addRaveLoss(int p) {
		addRaveRun(p, 0);
	}

	public void addRaveWin(int p) {
		addRaveRun(p, 1);
	}
	
	public void addRaveRun(int p, double w) {
		raveWinRates[p] = (w + raveWinRates[p] * raveRuns[p]) / (1 + raveRuns[p]);
		raveRuns[p]++;
	}

	/** Returns the number of RAVE runs through move p. */
	public int getRaveRuns(int p) {
		return raveRuns[p];
	}

	/** Returns the RAVE win rate for move p. */
	public double getRaveWinRate(int p) {
		return raveWinRates[p];
	}

	/** Returns the number of RAVE wins through move p. */
	public double getRaveWins(int p) {
		return raveWinRates[p] * raveRuns[p];
	}

	// TODO Should this be synchronized, as it is in the superclass?
	@Override
	public void recordPlayout(double winProportion, int[] moves, int t, int turn,
			IntSet playedPoints) {
		super.recordPlayout(winProportion, moves, t, turn, playedPoints);
		// The remaining moves in the sequence are recorded for RAVE
		int move = moves[t];
		playedPoints.clear();
		while (t < turn) {
			move = moves[t];
			if ((move != PASS) && !playedPoints.contains(move)) {
				playedPoints.add(move);
				addRaveRun(move, winProportion);
			}
			t++;
			if (t >= turn) {
				return;
			}
			playedPoints.add(moves[t]);
			t++;
		}
	}

	@Override
	public void reset(long hash) {
		super.reset(hash);
		fill(raveRuns, 2);
		fill(raveWinRates, 0.5);
	}

	@Override
	public String toString(int p) {
		return format("%s: %7d/%7d (%1.4f), RAVE %7d/%7d (%1.4f)\n",
				pointToString(p), getWins(p), getRuns(p), getWinRate(p), 
				(int) getRaveWins(p), (int) raveRuns[p],
				raveWinRates[p]);
	}

}
