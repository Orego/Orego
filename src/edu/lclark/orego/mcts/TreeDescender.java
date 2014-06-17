package edu.lclark.orego.mcts;

import edu.lclark.orego.mcts.McRunnable;

/**
 * Generates moves within a MC search tree (or analogous structure).
 */
public interface TreeDescender {

	/** Returns this object to its original state. */
	public void clear();

	/**
	 * Generates moves to the frontier of the tree. These moves are played on
	 * runnable's board.
	 */
	public void descend(McRunnable runnable);

	/**
	 * Returns the best move to make from here when actually playing (as opposed
	 * to during a playout). We choose the move with the most wins.
	 */
	public short bestPlayMove();
	
}
