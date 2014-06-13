package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;

/** Mainly for testing McRunnable. */
public class StubPlayer implements Player {

	private Board board;
	
	private McRunnable[] runnables;
	
	public StubPlayer(int width, int threads) {
		board = new Board(width);
		runnables = new McRunnable[threads];
		for (int i = 0; i < runnables.length; i++) {
			runnables[i] = new McRunnable(this);
		}
	}

	@Override
	public Board getBoard() {
		return board;
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
		return runnables[i];
	}

}
