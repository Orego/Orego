package edu.lclark.orego.patterns;

/** Data structure to hold a pattern and its wins / runs statistics. */
final class Pattern implements Comparable<Pattern> {

	/**
	 * Returns the human-readable character corresponding to color i.
	 * 
	 * @param i
	 *            For a color c, this would be the output of c.index().
	 */
	public static char toGlyph(int i) {
		switch (i) {
		case 0:
			return '#';
		case 1:
			return 'O';
		case 2:
			return '.';
		case 3:
			return '?';
		default:
			return '!';
		}
	}

	/**
	 * The 3x3 pattern of colors around a vacant point, with two bits per point.
	 */
	private final char colors;

	/**
	 * Number of times this pattern has been either chosen in a recorded game or
	 * randomly selected as a "bad" move.
	 */
	private final int runs;

	/** Rate at which this pattern has been chosen in recorded games. */
	private final float winRate;

	Pattern(int colors, float winRate, int runs) {
		this.colors = (char)colors;
		this.winRate = winRate;
		this.runs = runs;
	}

	@Override
	public int compareTo(Pattern pattern) {
		if (winRate > pattern.winRate) {
			return 1;
		} else if (winRate < pattern.winRate) {
			return -1;
		} else if (winRate == pattern.winRate) {
			if (colors > pattern.colors) {
				return 1;
			} else if (colors < pattern.colors) {
				return -1;
			}
		}
		return 0;
	}

	public int getColors() {
		return colors;
	}

	public int getRuns() {
		return runs;
	}

	public float getWinRate() {
		return winRate;
	}

	@Override
	public String toString() {
		String result = "Win Rate: " + winRate + " Hash = " + (int)colors
				+ " Runs = " + runs + "\n";
		result += toGlyph(colors >>> 8 & 3);
		result += toGlyph(colors & 3);
		result += toGlyph(colors >>> 10 & 3);
		result += "\n";
		result += toGlyph(colors >>> 2 & 3);
		result += ".";
		result += toGlyph(colors >>> 4 & 3);
		result += "\n";
		result += toGlyph(colors >>> 12 & 3);
		result += toGlyph(colors >>> 6 & 3);
		result += toGlyph(colors >>> 14 & 3);
		result += "\n";
		return result;
	}

}
