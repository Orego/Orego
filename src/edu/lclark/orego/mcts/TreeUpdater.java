package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Color;

/** Updates the tree based on the result of a run. */
public interface TreeUpdater {

	/** Returns this object to its original state. */
	public void clear();

	/** Returns the number of runs required before a child is created. */
	public int getGestation();

	/** Returns the root node (creating it if necessary). */
	public SearchNode getRoot();

	/**
	 * Updates the tree after accepting a move (e.g., throwing away unreachable
	 * nodes).
	 */
	public void updateForAcceptMove();

	/** Updates the tree based on the result of a run. */
	public void updateTree(Color winner, McRunnable mcRunnable);

}
