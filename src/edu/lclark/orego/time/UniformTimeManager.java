package edu.lclark.orego.time;

import static java.lang.Math.max;
import edu.lclark.orego.core.Board;

/** Manages time based on the estimation of how many moves are left in the game. */
public final class UniformTimeManager implements TimeManager {

	/** Constant used in getMsec. */
	private static final double TIME_CONSTANT = 0.2;

	/** True if we've already thought this turn. */
	private boolean alreadyThought;

	private final Board board;

	/** Time remaining for the entire game. */
	private int msecRemaining;

	public UniformTimeManager(Board board) {
		this.board = board;
	}

	@Override
	public int getMsec() {
		if (!alreadyThought) {
			final int movesLeft = max(10,
					(int) (board.getVacantPoints().size() * TIME_CONSTANT));
			alreadyThought = true;
			return max(1, msecRemaining / movesLeft);
		}
		return 0;
	}

	@Override
	public void setRemainingSeconds(int seconds) {
		// The subtraction ensures that we don't run out of time due to lag
		msecRemaining = max(1, (seconds - 10) * 1000);
	}

	@Override
	public void startNewTurn() {
		alreadyThought = false;
	}

}
