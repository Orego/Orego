package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;

public class McPlayer implements Player {

	private Board board;
	
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short bestMove() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMillisecondsPerMove(int milliseconds) {
		// TODO Auto-generated method stub
		
	}

}
