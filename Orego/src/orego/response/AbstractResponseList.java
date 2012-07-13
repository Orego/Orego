package orego.response;

import ec.util.MersenneTwisterFast;
import orego.core.Board;

public abstract class AbstractResponseList {

	/** Number of initial virtual wins given to each non-pass move. */
	public final static int NORMAL_WINS_PRIOR = 1;
	
	/** Number of initial virtual runs given to each non-pass move. */
	public final static int NORMAL_RUNS_PRIOR = 2;
	
	/** Number of initial virtual wins given to each pass move. */
	public final static int PASS_WINS_PRIOR = 1;
	
	/** Number of initial virtual runs given to each pass move. */
	public final static int PASS_RUNS_PRIOR = 10;
	
	/**
	 * Add a win and run for move p.
	 */
	public abstract void addWin(int p);
	
	/**
	 * Add a run (but not a win) for move p.
	 */
	public abstract void addLoss(int p);

	// TODO This should move to ResponsePlayer
	/**
	 * Get the best response from the list
	 */
	public abstract int bestMove(Board board, MersenneTwisterFast random);
	
	/**
	 * Gets the total number of runs for *all* moves in this response list.
	 * @return the total number of runs across all runs
	 */
	public abstract long getTotalRuns();
	
	/**
	 * Gets the win rate for a given move in this response list
	 * TODO: perhaps change name to something more generic?
	 */
	public abstract double getWinRate(int p);

}
