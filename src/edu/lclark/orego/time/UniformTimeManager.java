package edu.lclark.orego.time;

import static java.lang.Math.max;
import edu.lclark.orego.core.Board;

/** Manages time based on the estimation of how many moves are left in the game. */
public final class UniformTimeManager implements TimeManager {

	private Board board;

	private int secondsRemaining;

	private boolean alreadyThought;

	private final double timeC = 0.2;

	public UniformTimeManager(Board board) {
		this.board = board;
	}

	@Override
	public int getTime() {
		if (!alreadyThought) {
			int movesLeft = max(10, (int) (board.getVacantPoints().size() * timeC));
			return max(1, (secondsRemaining * 1000) / movesLeft);
		}
		return 0;
	}

	@Override
	public void startNewTurn() {
		alreadyThought = false;

	}

	@Override
	public void setRemainingTime(int seconds) {
		secondsRemaining = seconds;

	}

}
