package edu.lclark.orego.time;

/** Manages a player's time. */
public interface TimeManager {

	/**
	 * Gets the amount of time (in msec) to think before asking again. Returns 0
	 * if no more thinking should be done this turn.
	 */
	public int getMsec();

	/** Sets the amount of time left in the game for this player. */
	public void setRemainingSeconds(int seconds);

	/** Resets state to start a new turn. */
	public void startNewTurn();

}
