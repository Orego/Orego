package edu.lclark.orego.feature;

import static java.lang.Math.max;
import edu.lclark.orego.core.Board;

public class SimpleTimeManager implements TimeManager {
	
	private int timeRemaining;
	
	private final Board board;
	
	/** The constant C to use in the time management formula. */
	private double timeC = 0.20;
	
	private boolean alreadyThought;
	
	public SimpleTimeManager(Board board){
		this.board = board;
	}

	@Override
	public int getTime() {
		if(alreadyThought){
			return 0;
		}
		int msec = getMsecPerMove();
		alreadyThought = true;
		return msec;
	}
	
	private int getMsecPerMove() {
		int movesLeft = max(10, (int) (board.getVacantPoints().size() * timeC));
		int msPerMove = max(1, (timeRemaining * 1000) / movesLeft);
		msPerMove = max(1, msPerMove);
		return msPerMove;
	}

	@Override
	public void setRemainingTime(int seconds) {
		alreadyThought = false;
		timeRemaining = seconds;
	}

}
