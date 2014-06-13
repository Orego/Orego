package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;

public interface Player {

	public Board getBoard();

	public void generateMovesToFrontier(McRunnable mcRunnable);

	public void incorporateRun(Color winner, McRunnable mcRunnable);

	public boolean shouldKeepRunning();

	public void reset();

	public McRunnable getMcRunnable(int i);
	
	public short bestMove();

	public void setMillisecondsPerMove(int milliseconds);
	
}
