package edu.lclark.orego.core;

/** Color of a stone. */
public enum StoneColor implements Color {
	
	BLACK,
	
	WHITE;

	/** Returns the opposite color. */
	public StoneColor opposite() {
		// Can anyone do this without the if? I can't find a way to store the information
		// as the instances are created.
		if (this == BLACK) {
			return WHITE;
		}
		return BLACK;
	}

}
