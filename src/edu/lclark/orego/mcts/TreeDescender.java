package edu.lclark.orego.mcts;


/**
 * Generates moves within a MC search tree (or analogous structure).
 */
public interface TreeDescender {

	/**
	 * Returns the best move to make from here when actually playing (as opposed
	 * to during a playout). We choose the move with the most wins.
	 */
	public short bestPlayMove();

	/** Returns this object to its original state. */
	public void clear();

	/**
	 * Generates moves to the frontier of the tree. These moves are played on
	 * runnable's board.
	 */
	public void descend(McRunnable runnable);

	/**
	 * A descend method for testing that takes a runnable partway through a
	 * playout.
	 */
	public void fakeDescend(McRunnable runnable, short... moves);

	/** Returns the number of runs a node must have before biases are applied. */
	public int getBiasDelay();
	
	
	/**
	 * Returns the search value of this move, e.g., best win rate, UCT, or RAVE.
	 */
	public float searchValue(SearchNode node, short move);

}
