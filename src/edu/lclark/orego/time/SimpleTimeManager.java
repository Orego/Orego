package edu.lclark.orego.time;

/** Always allocates the same amount of time per move. */
public final class SimpleTimeManager implements TimeManager {

	/** True if we've already thought this turn. */
	private boolean alreadyThought;

	private final int msecPerMove;

	public SimpleTimeManager(int msecPerMove) {
		this.msecPerMove = msecPerMove;
	}

	@Override
	public int getMsec() {
		if (alreadyThought) {
			return 0;
		}
		alreadyThought = true;
		return msecPerMove;
	}

	@Override
	public void setRemainingSeconds(int seconds) {
		// Does nothing
	}

	@Override
	public void startNewTurn() {
		alreadyThought = false;
	}

}
