package orego.core;

/** Colors of points. */
public class Colors {

	/** Black, equal to 0. */
	public static final int BLACK = 0;

	/** Number of colors, including VACANT and OFF_BOARD. Equal to 4. */
	public static final int NUMBER_OF_COLORS = 4;

	/** Number of player colors, equal to 2. */
	public static final int NUMBER_OF_PLAYER_COLORS = 2;

	/** Off board, equal to 3. */
	public static final int OFF_BOARD_COLOR = 3;

	/** Vacant, equal to 2. */
	public static final int VACANT = 2;

	/** White, equal to 1. */
	public static final int WHITE = 1;

	/** Returns the color represented by ch. */
	public static final int charToColor(char ch) {
		int result = "#O.*".indexOf(ch);
		assert result >= 0 : "Character does not correspond to any color: "
				+ ch;
		return result;
	}

	/** Returns a char representing color. Used for board diagrams. */
	public static final char colorToChar(int color) {
		if ((color < 0) || (color > 3)) {
			return '?';
		}
		return "#O.*".charAt(color);
	}

	/**
	 * Returns a String representation of color. The result is the same as that
	 * from colorToChar, but it's been converted to a String.
	 */
	public static final String colorToString(int color) {
		return "" + colorToChar(color);
	}

	/** Returns true if color is either BLACK or WHITE. */
	public static final boolean isAPlayerColor(int color) {
		return color <= WHITE;
	}

	/** Returns the opposite of color. Undefined if color is not BLACK or WHITE. */
	public static final int opposite(int color) {
		assert isAPlayerColor(color);
		return color ^ 1;
	}

}
