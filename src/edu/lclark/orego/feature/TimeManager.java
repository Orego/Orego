package edu.lclark.orego.feature;

import static java.lang.Math.max;
import edu.lclark.orego.core.Board;

public class TimeManager {

	/** The constant C to use in the time management formula. */
	private double timeC = 0.20;

	private Board board;
	
	public TimeManager(Board board){
		this.board = board;
	}

	public int getMsecPerMove(int seconds) {
		int movesLeft = max(10, (int) (board.getVacantPoints().size() * timeC));
		int msPerMove = max(1, (seconds * 1000) / movesLeft);
		msPerMove = max(1, msPerMove);
		return msPerMove;
	}
}
