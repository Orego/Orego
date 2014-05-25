package edu.lclark.orego.core;

/** Color of a stone. */
public enum StoneColor implements Color {

	BLACK('#', 0),

	WHITE('O', 1);

	private char glyph;

	private int index;
	
	private StoneColor(char c, int index) {
		glyph = c;
		this.index = index;
	}

	@Override
	public int index() {
		return index;
	}

	/** Returns the opposite color. */
	public StoneColor opposite() {
		// Can anyone do this without the if? I can't find a way to store the
		// information as the instances are created.
		if (this == BLACK) {
			return WHITE;
		}
		return BLACK;
	}

	/**
	 * Returns the StoneColor represented in diagrams by the specified
	 * character. Returns null if c does not correspond to a StoneColor.
	 */
	public static StoneColor forChar(char c) {
		if (c == BLACK.toChar()) {
			return BLACK;
		} else if (c == WHITE.toChar()) {
			return WHITE;
		}
		return null;
	}

	@Override
	public char toChar() {
		return glyph;
	}

}
