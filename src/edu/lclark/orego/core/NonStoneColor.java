package edu.lclark.orego.core;

/** Color of something other than a stone. */
public enum NonStoneColor implements Color {
	
	/** Color of an unoccupied point. */
	VACANT,
	
	/** Color of, e.g., a sentinel point off the edge of the board. */
	OFF_BOARD

}
