package edu.lclark.orego.time;

/** Always allocates the same amount of time per move. */
public final class SimpleTimeManager implements TimeManager {

	private boolean alreadyThought;
	
	private int msecPerMove;
	
	public SimpleTimeManager(int msecPerMove){
		this.msecPerMove = msecPerMove;
	}

	@Override
	public int getTime() {
		if(alreadyThought){
			return 0;
		}
		alreadyThought = true;
		return msecPerMove;
	}
	
	@Override
	public void setRemainingTime(int seconds) {
		// Does nothing
	}

	@Override
	public void startNewTurn() {
		alreadyThought = false;
	}

}
