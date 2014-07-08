package edu.lclark.orego.time;

import static java.lang.Math.max;
import static edu.lclark.orego.util.Gaussian.Phi;
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

	/**
	 * Returns the confidence (from 0.0 to 1.0) that case A is better than case
	 * B.
	 */
	private static double confidence(float winrateA, double runsA,
			float winrateB, double runsB) {
		double z = (winrateA - winrateB)
				/ Math.sqrt(winrateA * (1 - winrateA) / runsA + winrateB
						* (1 - winrateB) / runsB);
		return Phi(z);
	}

	/** Used to determine the number of remaining vacant points. */
	private final Board board;

	/** Used to find the root of the tree. */
	private final Player player;

	/** Time (in msec) rolled over from previous turns. */
	private int rollover;

	/** Number of time slices left in this turn. */
	private int slicesRemaining;

	/** The constant C to use in the time management formula. */
	private double timeC = 0.20;

	private int timePerSlice;

	/** Time left for the rest of our moves, in seconds. */
	private int timeRemaining;

	public ExitingTimeManager(Player player) {
		this.player = player;
		this.board = player.getBoard();
	}

	/**
	 * Returns how confident we are (from 0.0 to 1.0) that the best move has a
	 * higher winrate than the rest of the legal moves.
	 */
	private double confidenceBestVsRest() {
		SearchNode root = player.getRoot();
		CoordinateSystem coords = player.getBoard().getCoordinateSystem();
		// win rate and runs of the best move
		short bestMove = root.getMoveWithMostWins(coords);
		float bestWinRate = root.getWinRate(bestMove);
		int bestRuns = root.getRuns(bestMove);
		// runs and wins of the rest of the moves
		int restRuns = 0;
		int restWins = 0;
		ShortSet vacant = board.getVacantPoints();
		for (int i = 0; i < vacant.size(); i++) {
			short p = vacant.get(i);
			if (p != bestMove && root.getWinRate(p) > 0.0) {
				float w = root.getWins(p);
				restWins += w;
				restRuns += root.getRuns(p);
			}
		}
		float restWinRate = restWins / (float) (restRuns);
		double c = confidence(bestWinRate, bestRuns, restWinRate, restRuns);
		return c;
	}

	private void createSlices() {
//		System.err.println("Creating slices");
		slicesRemaining = SLICE_COUNT;
		timePerSlice = (getMsecPerMove() + rollover) / SLICE_COUNT;
//		System.err.println("Allocated " + timePerSlice + " msec per slice");
	}

	private int getMsecPerMove() {
		int movesLeft = max(10, (int) (board.getVacantPoints().size() * timeC));
		return max(1, (timeRemaining * 1000) / movesLeft);
	}

	protected int getRollover() {
		return rollover;
	}

	// TODO Use seconds or msec instead of time in naming
	@Override
	public int getTime() {
		if (slicesRemaining == 0) {
//			System.err.println("Out of slices; stopping");
			rollover = 0;
			return 0;
		}
		if (slicesRemaining < SLICE_COUNT && confidenceBestVsRest() > 0.95) {
//			System.err.println("Confident in best move; stopping");
			rollover = slicesRemaining * timePerSlice;
			return 0;
		}
//		System.err.println("Starting slice " + slicesRemaining);
		slicesRemaining--;
		return timePerSlice;
	}

	@Override
	public void setRemainingTime(int seconds) {
		timeRemaining = seconds - (rollover / 1000);
		createSlices();
	}

	@Override
	public void startNewTurn() {
		// Does nothing; things are reset in setRemainingTime
	}

}
