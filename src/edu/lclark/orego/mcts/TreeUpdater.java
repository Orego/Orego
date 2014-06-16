package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Color;

/** Updates the tree based on the result of a run. */
public interface TreeUpdater {

	/** Returns this object to its original state. */
	public void clear();
	
	/** Updates the tree based on the result of a run. */
	public void updateTree(Color winner, McRunnable mcRunnable);

}
