package edu.lclark.orego.core;

/** Color of a stone. */
public enum StoneColor implements Color {

	BLACK('#', 0),

	WHITE('O', 1);

	// Set the opposite fields. This cannot be done in the constructor because,
	// when BLACK is being constructed, WHITE doesn't exist yet.
	static {
		BLACK.opposite = WHITE;
		WHITE.opposite = BLACK;
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

	private final char glyph;

	private final int index;

	private StoneColor opposite;

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
		return opposite;
	}

	@Override
	public char toChar() {
		return glyph;
	}

}
