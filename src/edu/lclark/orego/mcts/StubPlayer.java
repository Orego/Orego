package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;

/** Mainly for testing McRunnable. */
public class StubPlayer implements Player {

	@Override
	public Board getBoard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void generateMovesToFrontier(McRunnable mcRunnable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incorporateRun(Color winner, McRunnable mcRunnable) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean shouldKeepRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public McRunnable getMcRunnable(int i) {
		// TODO Auto-generated method stub
		return null;
	}

}
