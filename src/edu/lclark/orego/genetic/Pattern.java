package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.Legality.OK;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.NonStoneColor.*;

public class Pattern implements Mover {

	private static CoordinateSystem coords;

	/** Mask for the bits indicating the location of the edge in a space rule. */
	public static final int EDGE = 0b111 << 24;

	/** Mask for enough bits to encode one point on the board. */
	public static final int MOVE_LENGTH = (1 << 9) - 1;

	/** Mask for enough bits to encode two points on the board. */
	public static final int MOVE_LENGTH_2 = (1 << 18) - 1;

	/**
	 * Mask for the "penultimate bit", which determines if this time rule takes
	 * into account the move before last.
	 */
	public static final int PENULTIMATE = 1 << 29;

	/** Mask for the bits representing the spatial part of a rule. */
	public static final int SPACE = (1 << 24) - 1;

	/**
	 * Mask for the "yes bit", which determines if this rule advocates or
	 * forbids a move.
	 */
	public static final int YES = 1 << 30;

	/**
	 * Given a row or column i and the width of the board, returns bit encoding
	 * an edge at that location in a space rule.
	 */
	private static int edgePattern(int i, int width) {
		int actual = 0;
		if (i == 1) {
			actual |= (1 << 24);
		} else if (i == 2) {
			actual |= (2 << 24);
		} else if (i == 3) {
			actual |= (3 << 24);
		} else if (i == width - 2) {
			actual |= (4 << 24);
		} else if (i == width - 1) {
			actual |= (5 << 24);
		} else if (i == width) {
			actual |= (6 << 24);
		}
		return actual;
	}

	// TODO Specify yes/no, time/space, edges (for space), 1 or 2 moves (for
	// time)
	/**
	 * Converts a human-readable pattern into two bit vectors. In the diagram,
	 * the symbols are:
	 * 
	 * <pre>
	 * # Friendly
	 * O Enemy
	 * . Vacant
	 * ? Anything
	 * </pre>
	 * 
	 * @param horizontal
	 *            relative location of horizontal edge
	 * @param vertical
	 *            relative location of vertical edge
	 * @param flagBits
	 *            bits such as YES
	 */
	public static int[] makeSpaceRule(int horizontal, int vertical,
			int flagBits, String... diagram) {
		int[] result = new int[2];
		// TODO This is a bit cryptic
		result[0] |= edgePattern(horizontal, coords.getWidth()) | flagBits;
		result[1] |= edgePattern(vertical, coords.getWidth());
		int i = 0;
		for (int r = 0; r < diagram.length; r++) {
			String row = diagram[r];
			for (int c = 0; c < row.length(); c++) {
				if ((r != diagram.length / 2) || (c != diagram.length / 2)) {
					char glyph = row.charAt(c);
					if ("#?".indexOf(glyph) >= 0) {
						result[0] |= 1 << i;
					}
					if ("O?".indexOf(glyph) >= 0) {
						result[1] |= 1 << i;
					}
					i++;
				}
			}
		}
		return result;
	}

	private Board board;

	/** Set of moves still under consideration. */
	private ShortSet candidates;

	private HistoryObserver historyObserver;

	public Pattern(Board board, HistoryObserver historyObserver) {
		this.board = board;
		this.historyObserver = historyObserver;
		coords = board.getCoordinateSystem();
		candidates = new ShortSet(coords.getFirstPointBeyondBoard());
	}

	/**
	 * Yes rules have a 1 at the 30th place of pattern[0]. Space rules have a 0
	 * at the 31st place; time rules have a 1 at the 31st place.
	 */
	public short patternMatcher(int... pattern) {
		candidates.copyDataFrom(board.getVacantPoints());
		if (pattern[0] >= 0) { // i.e., sign bit is zero
			return spaceMatcherIterator(pattern);
		} else {
			return timeMatcher(pattern);
		}
	}

	private int[] pattern;
	
	void setPattern(int... pattern) {
		this.pattern = pattern;
	}

	public short selectAndPlayOneMove(MersenneTwisterFast random, boolean fast) {
		for (int i = 0; i <= pattern.length - 2; i += 2) {
			short tempResult = patternMatcher(pattern[i], pattern[i + 1]);
			if (tempResult != NO_POINT) {
				return tempResult;
			}
		}
		for (int i = 0; i < candidates.size(); i++) {
			if (board.play(candidates.get(i)) == OK) {
				// TODO make this random
				return candidates.get(i);
			}
		}
		return PASS;
	}

	/**
	 * Given a position on the board (p) that is assumed to be empty and two
	 * ints containing the pattern, this returns a boolean--true if the pattern
	 * matches the condition of the position.
	 */
	private boolean spaceMatcher(short p, int... pattern) {
		int row = coords.row(p);
		int col = coords.column(p);
		int actualFriendly = edgePattern(row + 1, coords.getWidth());
		int actualEnemy = edgePattern(col + 1, coords.getWidth());
		if (((pattern[0] & EDGE) >> 24) != ((actualFriendly & EDGE) >> 24)
				|| (((pattern[1] & EDGE) >> 24) != ((actualEnemy & EDGE) >> 24))) {
			candidates.remove(p);
			return false;
		}
		actualFriendly = 0;
		actualEnemy = 0;
		boolean yes = (pattern[0] & YES) != 0;
		pattern[0] = SPACE & pattern[0];
		pattern[1] = SPACE & pattern[1];
		int i = 0;
		for (int r = row - 2; r <= row + 2; r++) {
			for (int c = col - 2; c <= col + 2; c++) {
				if ((c != col) || (r != row)) {
					if (coords.isValidOneDimensionalCoordinate(r)
							&& coords.isValidOneDimensionalCoordinate(c)) {
						short temp = coords.at(r, c);
						final Color color = board.getColorAt(temp);
						if (color == board.getColorToPlay()) {
							actualFriendly |= (1 << i);
						} else if (color == board.getColorToPlay().opposite()) {
							actualEnemy |= (1 << i);
						}
					}
					i++;
				}
			}
		}
		boolean truth = (((actualFriendly & (pattern[0] & ~pattern[1])) == (pattern[0] & ~pattern[1]))
				&& (((actualFriendly & (~pattern[0] | pattern[1])) | (pattern[0] & pattern[1])) == (pattern[0] & pattern[1]))
				&& ((actualEnemy & (pattern[1] & ~pattern[0])) == (pattern[1] & ~pattern[0])) && (((actualEnemy & (~pattern[1] | pattern[0])) | (pattern[1] & pattern[0])) == (pattern[1] & pattern[0])));
		if (!truth && yes) {
			candidates.remove(p);
			return false;
		}
		return truth;
	}

	private short spaceMatcherIterator(int... pattern) {
		while (candidates.size() > 0) {
			short p = candidates.get((int) (Math.random() * candidates.size()));
			assert board.getColorAt(p) == VACANT;
			if (spaceMatcher(p, pattern)) {
				Legality legality = board.play(p);
				if (legality == OK) {
					return p;
				}
			}
			candidates.remove(p);
		}
		return NO_POINT;
	}

	/**
	 * Returns the short iff successful play was made and NO_POINT if not. It
	 * removes unsuccessful moves from the list.
	 */
	private short timeMatcher(int... pattern) {
		// TODO Make this only in two ints (probably only in 1)
		int actualMoves = 0;
		short response = (short) ((pattern[0] >> 18) & MOVE_LENGTH);
		actualMoves |= (historyObserver.get(board.getTurn() - 1));
		if ((pattern[0] & MOVE_LENGTH) != actualMoves) {
			candidates.remove(response);
			return NO_POINT;
		}
		if ((pattern[0] & PENULTIMATE) != 0) {
			actualMoves |= ((historyObserver.get(board.getTurn() - 2)) << 9);
			if (((pattern[0]) & MOVE_LENGTH_2) != actualMoves) {
				candidates.remove(response);
				return NO_POINT;
			}
		}
		response = (short) (response % coords.getFirstPointBeyondBoard());
		if (candidates.contains(response)) {
			if ((pattern[0] & YES) != 0) {
				if (board.play(response) == OK) {
					return response;
				}
			}
		}
		candidates.remove(response);
		return NO_POINT;
	}

}
