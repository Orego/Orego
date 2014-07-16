package edu.lclark.orego.time;

import static edu.lclark.orego.thirdparty.Gaussian.Phi;
import static java.lang.Math.max;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.SearchNode;
import edu.lclark.orego.util.ShortSet;

/**
 * Occasionally stops early when it is confident that it has found a good move.
 * Excess time is rolled over into the next move until it is used.
 */
public final class ExitingTimeManager implements TimeManager {

	/** Number of slices into which to divide each turn. */
	private static final int SLICE_COUNT = 3;

	/** The constant C to use in the time management formula. */
	private static final double TIME_CONSTANT = 0.20;

	/**
	 * Returns the confidence (from 0.0 to 1.0) that case A is better than case
	 * B.
	 */
	private static double confidence(float winrateA, double runsA,
			float winrateB, double runsB) {
		if (runsB == 0) {
			// There are no other moves to consider, so this must be best
			return 1.0;
		}
		final double z = (winrateA - winrateB)
				/ Math.sqrt(winrateA * (1 - winrateA) / runsA + winrateB
						* (1 - winrateB) / runsB);
		return Phi(z);
	}

	/** Used to determine the number of remaining vacant points. */
	private final Board board;

	private int msecPerSlice;

	/** Time left for the rest of our moves, in msec. */
	private int msecRemaining;

	/** Used to find the root of the tree. */
	private final Player player;

	/** Time (in msec) rolled over from previous turns. */
	private int rollover;

	/** Number of time slices left in this turn. */
	private int slicesRemaining;

	public ExitingTimeManager(Player player) {
		this.player = player;
		this.board = player.getBoard();
	}

	/**
	 * Returns how confident we are (from 0.0 to 1.0) that the best move has a
	 * higher winrate than the rest of the legal moves.
	 */
	private double confidenceBestVsRest() {
		final SearchNode root = player.getRoot();
		final CoordinateSystem coords = player.getBoard().getCoordinateSystem();
		// win rate and runs of the best move
		final short bestMove = root.getMoveWithMostWins(coords);
		final float bestWinRate = root.getWinRate(bestMove);
		final int bestRuns = root.getRuns(bestMove);
		// runs and wins of the rest of the moves
		int restRuns = 0;
		int restWins = 0;
		final ShortSet vacant = board.getVacantPoints();
		for (int i = 0; i < vacant.size(); i++) {
			final short p = vacant.get(i);
			if (p != bestMove && root.getWinRate(p) > 0.0) {
				final float w = root.getWins(p);
				restWins += w;
				restRuns += root.getRuns(p);
			}
		}
		final float restWinRate = restWins / (float) restRuns;
		if (restWinRate <= 0) {
			return 0;
		}
		final double c = confidence(bestWinRate, bestRuns, restWinRate, restRuns);
		return c;
	}

	/** Sets the number and size of time slices to use. */
	private void createSlices() {
		slicesRemaining = SLICE_COUNT;
		msecPerSlice = (getMsecPerMove() + rollover) / SLICE_COUNT;
	}

	@Override
	public int getMsec() {
		assert player.shouldKeepRunning() == false;
		if (slicesRemaining == 0) {
			rollover = 0;
			return 0;
		}
		if (slicesRemaining < SLICE_COUNT && confidenceBestVsRest() > 0.99) {
			rollover = slicesRemaining * msecPerSlice;
			return 0;
		}
		slicesRemaining--;
		return msecPerSlice;
	}

	/** Computes the total time to allocate to the next move. */
	private int getMsecPerMove() {
		final int movesLeft = max(10, (int) (board.getVacantPoints().size() * TIME_CONSTANT));
		return max(1, msecRemaining / movesLeft);
	}

	/** Returns the number of msecs to be rolled over into the next turn. */
	int getRollover() {
		return rollover;
	}

	@Override
	public void setRemainingSeconds(int seconds) {
		msecRemaining = (seconds - 10) * 1000 - rollover / 1000;
		createSlices();
	}

	@Override
	public void startNewTurn() {
		// Does nothing; things are reset in setRemainingTime
	}

}
