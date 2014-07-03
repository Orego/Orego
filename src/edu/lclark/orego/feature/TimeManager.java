package edu.lclark.orego.feature;

public interface TimeManager {
	
	
	/** Gets the amount of time to think for */
	public int getTime();
	
	/** Sets the amount of time left in the game for this player */ 
	public void setRemainingTime(int seconds);

}
