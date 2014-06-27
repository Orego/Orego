package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Color;

/** Updates the tree based on the result of a run. */
public interface TreeUpdater {

	/** Returns this object to its original state. */
	public void clear();

	/** Returns the number of runs required before a child is created. */
	public int getGestation();
	
	/** Updates the tree based on the result of a run. */
	public void updateTree(Color winner, McRunnable mcRunnable);

}
