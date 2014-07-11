package edu.lclark.orego.core;

/** Color of something other than a stone. */
public enum NonStoneColor implements Color {

	/** Color of, e.g., a sentinel point off the edge of the board. */
	OFF_BOARD('?', 3),

	/** Color of an unoccupied point. */
	VACANT('.', 2);

	public static NonStoneColor forChar(char c) {
		if (c == OFF_BOARD.toChar()) {
			return OFF_BOARD;
		} else if (c == VACANT.toChar()) {
			return VACANT;
		}
		return null;
	}

	private char glyph;

	private int index;

	private NonStoneColor(char c, int index) {
		glyph = c;
		this.index = index;
	}

	@Override
	public int index() {
		return index;
	}

	@Override
	public char toChar() {
		return glyph;
	}

}
