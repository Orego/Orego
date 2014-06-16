package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Color;

/** Provides various methods specified by interfaces, but does nothing. */
public class DoNothing implements RunIncorporator {

	@Override
	public void incorporateRun(Color winner, McRunnable mcRunnable) {
		// Does nothing
	}

}
