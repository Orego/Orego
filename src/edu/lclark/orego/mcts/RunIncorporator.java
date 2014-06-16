package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Color;

/** Incorporates a playout into the tree. */
public interface RunIncorporator {

	/** Incorporate the result of a run in the tree. */
	public void incorporateRun(Color winner, McRunnable mcRunnable);

	/** Returns this RunIncorporator to its original state. */
	public void clear();
	
}
