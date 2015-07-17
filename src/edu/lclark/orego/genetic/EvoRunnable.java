package edu.lclark.orego.genetic;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;

/**
 * Players use this class to perform multiple Monte Carlo coevolution in different
 * threads.
 */
public class EvoRunnable implements Runnable {

	public Board getBoard() {
		return null;
	}

	/**
	 * Chooses two random members of the population and has them play a game against each other.
	 *
	 * @param mercy True if we should abandon the playout when one color has many more stones than the other.
	 * @return The winning color, although this is only used in tests.
	 */	
	public Color performPlayout(boolean mercy) {
		return null;
	}

	/** Returns the number of playouts completed by this runnable. */
	public int getPlayoutsCompleted() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
