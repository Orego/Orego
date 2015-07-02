package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.Legality.OK;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.core.NonStoneColor;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;

public class Pattern {

	public static final int YES = 1 << 30;
	public static final int PENULTIMATE = 1 << 29;
	public static final int MOVE_LENGTH = 0b111111111;
	public static final int MOVE_LENGTH_2 = 0b111111111111111111;
	public static final int EDGE = 0b111 << 24;

	/*
	 * friendly = 100 4 enemy = 010 2 vacant = 001 1 friendly, enemy = 110 6 all
	 * three 111 7 enemy or vacant = 011 3 friendly or v = 101 5 off-board 000 0
	 */

	private Board board;

	private ShortSet candidates;

	private HistoryObserver historyObserver;

	private ShortSet vacantPoints;
	
	private static CoordinateSystem coords;

	public Pattern(Board board, HistoryObserver historyObserver) {
		this.board = board;
		this.historyObserver = historyObserver;
		coords = board.getCoordinateSystem();
		vacantPoints = board.getVacantPoints();
		candidates = new ShortSet(coords.getFirstPointBeyondBoard());
		candidates.copyDataFrom(vacantPoints);
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
	 */
	public static int[] makeRule(int vertical, int horizontal, String... diagram) {
		int[] result = new int[2];
		result[0] |= edgePattern(vertical, coords.getWidth());
		result[1] |= edgePattern(horizontal, coords.getWidth());
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

	/**
	 * Given a row or column i and the width of the board, returns "actual",
	 * which will be actualFriendly or actualEnemy in the space patternMatcher
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

	/**
	 * Yes rules have a 1 at the 30th place of pattern[0]. Space rules have a 0
	 * at the 31st place; time rules have a 1 at the 31st place.
	 */
	public short patternMatcher(int... pattern) {
		if (pattern[0] > 0) {
			return spaceMatcherIterator(pattern);
		} else {
			return timeMatcher(pattern);
		}
	}

	public short selectAndPlayMove(int... pattern) {
		for (int i = 0; i <= pattern.length - 2; i += 2) {
			short tempResult = patternMatcher(pattern[i], pattern[i + 1]);
			if (tempResult != NO_POINT) {
				return tempResult;
			}
		}
		for (int i = 0; i < candidates.size(); i++) {
			if (board.play(candidates.get(i)) == OK) {
				//TODO make this random
				return candidates.get(i);
			}
		}
		return PASS;
	}

	/**
	 * Given a position on the board (short) that is assumed to be empty, a
	 * board, and three ints containing the pattern, this returns a
	 * boolean--true if the pattern matches the condition of the position. Bit
	 * encoding for edge is as follows: for top and bottom edges, 3 bits (24-27
	 * in actualFriendly are equal to the row number. For left and right edges,
	 * 3 bits (24-27) in actualEnemy are equal to the column number.
	 */
	private boolean spaceMatcher(short p, int... pattern) {
//		System.out.println("Trying to match at " + coords.toString(p));
		int row = coords.row(p);
		int col = coords.column(p);
		int actualFriendly = edgePattern(row + 1, coords.getWidth());
		int actualEnemy = edgePattern(col + 1, coords.getWidth());
//		System.out.println(" edge: " + Integer.toBinaryString((actualFriendly & EDGE)>> 24) + "\n edge: "
//				 + Integer.toBinaryString(((pattern[0] & EDGE)>> 24)));
//		System.out.println(" edge: " + Integer.toBinaryString((actualEnemy & EDGE)>> 24) + "\n edge: "
//				 + Integer.toBinaryString(((pattern[1] & EDGE)>> 24)));
		if(((pattern[0] & EDGE) >> 24) != ((actualFriendly & EDGE)>> 24) || (((pattern[1] & EDGE)>> 24) != ((actualEnemy & EDGE)>> 24))){
//			System.out.println("edges not equal?");
			candidates.remove(p);
			return false;
		}
		actualFriendly = 0;
		actualEnemy = 0;
		boolean yes = (pattern[0] & YES) == 0;
//		System.out.println("pattern[0] = " + Integer.toBinaryString(pattern[0]));
//		System.out.println("pattern[1] = " + Integer.toBinaryString(pattern[1]));
		pattern[0] = 0xffffff & pattern[0];
		pattern[1] = 0xffffff & pattern[1];
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
//		System.out.println("pattern[0] = " + Integer.toBinaryString(pattern[0]));
//		System.out.println("act friend = " + Integer.toBinaryString(actualFriendly));
//		System.out.println("pattern[1] = " + Integer.toBinaryString(pattern[1]));
//		System.out.println("act enemy  = " + Integer.toBinaryString(actualEnemy));
//		 System.out.println(pattern[0] == actualFriendly);
//		 System.out.println(pattern[1] == actualEnemy);
		boolean truth = (((actualFriendly & (pattern[0] & ~pattern[1])) == (pattern[0] & ~pattern[1]))
				&& (((actualFriendly & (~pattern[0] | pattern[1])) | (pattern[0] & pattern[1])) == (pattern[0] & pattern[1]))
				&& ((actualEnemy & (pattern[1] & ~pattern[0])) == (pattern[1] & ~pattern[0])) && (((actualEnemy & (~pattern[1] | pattern[0])) | (pattern[1] & pattern[0])) == (pattern[1] & pattern[0])));
		if (!truth && !yes) {
			candidates.remove(p);
			return false;
		}
		return truth;
	}

	private short spaceMatcherIterator(int... pattern) {
		while (candidates.size() > 0) {
			short p = candidates.get((int) (Math.random() * candidates.size()));
//			 System.out.println(p);
			if (spaceMatcher(p, pattern)) {
//				System.out.println("it matched: 0" + p);
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
//		System.out.println(Integer.toBinaryString(actualMoves) + " "
//				+ Integer.toBinaryString(response));
		response = (short) (response % coords.getFirstPointBeyondBoard());
		if (candidates.contains(response)) {
			if (board.play(response) == OK) {
				if ((pattern[0] & YES) != 0) {
					return response;
				}
			}
		}
		candidates.remove(response);
		return NO_POINT;
	}
}
