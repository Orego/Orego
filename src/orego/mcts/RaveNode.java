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
	private int[] raveWins;

	public RaveNode() {
		raveRuns = new int[FIRST_POINT_BEYOND_BOARD];
		raveWins = new int[FIRST_POINT_BEYOND_BOARD];
	}

	public void addRaveLoss(int p) {
		raveRuns[p]++;
	}

	public void addRaveWin(int p) {
		raveWins[p]++;
		raveRuns[p]++;
	}

	protected void setRaveWins(int p, int n) {
		raveWins[p] = n;
	}

	protected void setRaveRuns(int p, int n) {
		raveRuns[p] = n;
	}

	/** Returns the number of RAVE runs through move p. */
	public int getRaveRuns(int p) {
		return raveRuns[p];
	}

	/** Returns the RAVE win rate for move p. */
	public double getRaveWinRate(int p) {
		return ((double) raveWins[p]) / raveRuns[p];
	}

	/** Returns the number of RAVE wins through move p. */
	public int getRaveWins(int p) {
		return raveWins[p];
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
				if (winProportion == 1) {
					addRaveWin(move);
				} else if(winProportion == 0) {
					addRaveLoss(move);
				}
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
		fill(raveWins, 1);
	}

	@Override
	public String toString(int p) {
		return format("%s: %7d/%7d (%1.4f), RAVE %7d/%7d (%1.4f)\n",
				pointToString(p), getWins(p), getRuns(p), ((double) getWins(p))
						/ getRuns(p), (int) raveWins[p], (int) raveRuns[p],
				((double) raveWins[p]) / raveRuns[p]);
	}

}
