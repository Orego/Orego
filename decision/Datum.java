package orego.decision;

/** A single data point, corresponding to the result of playing a move. Stores previous moves and whether the playout resulted in a win. */
public class Datum {

	/** The moves played before this one. */
	private int[] previous;

	/** True if this move resulted in a win. */
	private boolean win;

	public Datum(int[] previous, boolean win) {
		this.previous = previous;
		this.win = win;
	}

	/** Returns the moves played before this one (with older moves later in the array). */
	public int[] getPrevious() {
		return previous;
	}

	/** Returns true if this move resulted in a win. */
	public boolean isWin() {
		return win;
	}

}
