package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Color;

/** Provides various methods specified by interfaces, but does nothing. */
public final class DoNothing implements TreeDescender, TreeUpdater {

	@Override
	public void clear() {
		// Does nothing
	}

	@Override
	public void descend(McRunnable runnable) {
		// Does nothing
	}
	
	@Override
	public void updateTree(Color winner, McRunnable mcRunnable) {
		// Does nothing
	}

	@Override
	public short bestPlayMove() {
		// TODO Auto-generated method stub
		return -1;
	}

}
