package orego.response;

import ec.util.MersenneTwisterFast;
import orego.core.Board;

public abstract class AbstractResponseList {
	/** initial win rate for each move .50 */
	public final static int NORMAL_WINS_PRIOR = 1;
	
	public final static int NORMAL_RUNS_PRIOR = 2;
	
	/** we want a constant .10 win rate for pass
	* pass win rate should be less than the initial win
	* rate since we don't want to pass immediately.
	*/

	public final static int PASS_WINS_PRIOR = 1;
	
	public final static int PASS_RUNS_PRIOR = 10;
	
	/**
	 * Add a win and run to this move.
	 */
	public abstract void addWin(int p);
	
	/**
	 * Add a run to this move.
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
	 * TODO: perhaps change to something more generic?
	 */
	public abstract double getWinRate(int p);

}
