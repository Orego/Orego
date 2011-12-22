package orego.patterns;

import static orego.core.Colors.*;

/**
 * A pattern for matching 3x3 neighborhoods on the board. Note that a
 * neighborhood has a specific color at each point, while a pattern may have
 * wild cards such as "not black".
 * 
 * Both neighborhoods and patterns are represented in the same order as the
 * NEIGHBORS array in orego.core.Coordinates. The center point of the
 * neighborhood is ignored in all cases, because it is assumed to be vacant.
 * 
 * Neighborhoods can be very densely represented as 16-bit chars, with two bits
 * for each of the eight points in the neighborhood.
 * 
 * @see orego.core.Coordinates#NEIGHBORS
 */
public abstract class Pattern {

	/** A "color" that matches black, white, and vacant. */
	public static final int IGNORE_COLOR = 4;

	/** A "color" that matches white and vacant. */
	public static final int NOT_BLACK = 5;

	/** A "color" that matches black and vacant. */
	public static final int NOT_WHITE = 6;

	/**
	 * Converts a multiline string into a neighborhood.
	 * 
	 * @see #neighborhoodToDiagram(char)
	 */
	public final static char diagramToNeighborhood(String diagram) {
		String[] rows = diagram.split("\n");
		char[][] c = { rows[0].toCharArray(), rows[1].toCharArray(),
				rows[2].toCharArray() };
		char[] n = { c[0][1], c[1][0], c[1][2], c[2][1], c[0][0], c[0][2],
				c[2][0], c[2][2] };
		char result = 0;
		for (int i = 0; i < 8; i++) {
			result = (char) ((result >> 2) | (charToColor(n[i]) << 14));
		}
		return result;
	}

	/**
	 * Returns the given neighborhood represented as a human-readable, multiline
	 * string.
	 * 
	 * @see #diagramToNeighborhood(String)
	 */
	public final static String neighborhoodToDiagram(char neighborhood) {
		int[] n = new int[8];
		int mask = 0x3;
		for (int i = 0; i < n.length; i++) {
			n[i] = (neighborhood >>> 2 * i) & mask;
		}
		return String.format("%c%c%c\n%c %c\n%c%c%c", colorToChar(n[4]),
				colorToChar(n[0]), colorToChar(n[5]), colorToChar(n[1]),
				colorToChar(n[2]), colorToChar(n[6]), colorToChar(n[3]),
				colorToChar(n[7]));
	}

	/** Colors (including wild cards) of the eight points of this template. */
	private int[] colors;

	public Pattern() {
		colors = new int[8];
	}

	/**
	 * Returns true if color is one of the choices specified by desired. For
	 * example, both VACANT and BLACK count as NOT_WHITE.
	 */
	protected boolean countsAs(int desired, int color) {
		switch (desired) {
		case IGNORE_COLOR:
			return color != OFF_BOARD_COLOR;
		case NOT_BLACK:
			return (color == VACANT) || (color == WHITE);
		case NOT_WHITE:
			return (color == VACANT) || (color == BLACK);
		default:
			return color == desired;
		}
	}

	/**
	 * Returns true if the elements of desired (which might include wild cards
	 * like NOT_BLACK) correspond to neighborhood.
	 */
	protected boolean countsAs(int[] desired, char neighborhood) {
		for (int i = 0; i < desired.length; i++) {
			if (!countsAs(desired[i], neighborhoodColorAt(neighborhood, i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if this pattern matches neighborhood with any number of
	 * rotations or reflections. Note that this messes up the colors stored in
	 * this pattern; it is important to call setColors() before invoking this.
	 */
	protected boolean countsAsInAnyOrientation(char neighborhood) {
		if (countsAs(colors, neighborhood)) {
			return true;
		}
		for (int i = 0; i < 3; i++) {
			colors = rotate90(colors);
			if (countsAs(colors, neighborhood)) {
				return true;
			}
		}
		colors = reflect(rotate90(colors));
		if (countsAs(colors, neighborhood)) {
			return true;
		}
		for (int i = 0; i < 3; i++) {
			colors = rotate90(colors);
			if (countsAs(colors, neighborhood)) {
				return true;
			}
		}
		return false;
	}

	/** Returns the color at each position in this pattern. */
	protected int[] getColors() {
		return colors;
	}

	/**
	 * Returns true if neighborhood is consistent with this pattern.
	 */
	public abstract boolean matches(char neighborhood);

	/**
	 * Returns the color in the ith position of neighborhood.
	 * 
	 * @see orego.core.Coordinates#NEIGHBORS
	 */
	protected int neighborhoodColorAt(char neighborhood, int i) {
		return (neighborhood & (3 << (2 * i))) >>> (2 * i);
	}

	/** Returns NOT_BLACK for BLACK and NOT_WHITE for WHITE. */
	protected int not(int color) {
		assert isAPlayerColor(color);
		if (color == WHITE) {
			return NOT_WHITE;
		} else {
			return NOT_BLACK;
		}
	}

	/**
	 * Returns colors reflected left to right.
	 * 
	 * @param colors
	 *            Colors of the eight points surrounding a point.
	 * @see orego.core.Coordinates#NEIGHBORS
	 */
	protected int[] reflect(int[] colors) {
		int[] transformed = new int[8];
		transformed[0] = colors[0];
		transformed[1] = colors[2];
		transformed[2] = colors[1];
		transformed[3] = colors[3];
		transformed[4] = colors[5];
		transformed[5] = colors[4];
		transformed[6] = colors[7];
		transformed[7] = colors[6];
		return transformed;
	}

	/**
	 * Returns colors rotated 90 degrees clockwise.
	 * 
	 * @param colors
	 *            Colors of the eight points surrounding a point.
	 * @see orego.core.Coordinates#NEIGHBORS
	 */
	protected int[] rotate90(int[] colors) {
		int[] transformed = new int[8];
		transformed[0] = colors[1];
		transformed[1] = colors[3];
		transformed[2] = colors[0];
		transformed[3] = colors[2];
		transformed[4] = colors[6];
		transformed[5] = colors[4];
		transformed[6] = colors[7];
		transformed[7] = colors[5];
		return transformed;
	}

	/**
	 * Sets the colors in this pattern according to s and colorToPlay.
	 * 
	 * @param s
	 *            Colors, in neighbor order. Options are # (color to play), O
	 *            (opposite color), . (vacant), * (off board), o (not color to
	 *            play), + (not opposite color), and ? (ignore).
	 */
	protected void setColors(String s, int colorToPlay) {
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '#') {
				colors[i] = colorToPlay;
			} else if (s.charAt(i) == 'O') {
				colors[i] = opposite(colorToPlay);
			} else if (s.charAt(i) == '.') {
				colors[i] = VACANT;
			} else if (s.charAt(i) == '*') {
				colors[i] = OFF_BOARD_COLOR;
			} else if (s.charAt(i) == 'o') { // o stands for O or .
				colors[i] = not(colorToPlay);
			} else if (s.charAt(i) == '+') { // + stands for # or .
				colors[i] = not(opposite(colorToPlay));
			} else if (s.charAt(i) == '?') {
				colors[i] = IGNORE_COLOR;
			} else {
				assert false : "Unknown color in template: " + s.charAt(i);
			}
		}
	}

}
