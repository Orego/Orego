package orego.response;

import orego.core.Board;

public abstract class AbstractResponseList {

	/**
	 * Add a win and run to this move.
	 */
	public abstract void addWin(int p);
	
	/**
	 * Add a run to this move.
	 */
	public abstract void addLoss(int p);
	
	/**
	 * Get the best response from the list
	 */
	public abstract int bestMove(Board board);
}
