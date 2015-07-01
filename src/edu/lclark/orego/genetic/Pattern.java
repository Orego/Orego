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

	/*
	 * friendly = 100 4 enemy = 010 2 vacant = 001 1 friendly, enemy = 110 6 all
	 * three 111 7 enemy or vacant = 011 3 friendly or v = 101 5 off-board 000 0
	 */

	private Board board;

	private ShortSet candidates;

	private HistoryObserver historyObserver;

	private ShortSet vacantPoints;

	public Pattern(Board board, HistoryObserver historyObserver) {
		this.board = board;
		this.historyObserver = historyObserver;
		vacantPoints = board.getVacantPoints();
		candidates = board.getVacantPoints();
	}

	// TODO Needs revision for new wild card plan
	// TODO Specify yes/no, time/space, edges (for space), 1 or 2 moves (for
	// time)
	/**
	 * Converts a human-readable pattern into three bit vectors. In the diagram,
	 * the symbols are:
	 * 
	 * <pre>
	 * # Friendly
	 * O Enemy
	 * . Vacant
	 * 
	 * + Friendly or vacant
	 * o Enemy or vacant
	 * * Any stone
	 * 
	 * ? Anything
	 * ! Nothing
	 * </pre>
	 */
	public static int[] makeRule(String... diagram) {
		//TODO update for 2 entries
		int[] result = new int[3];
		int i = 0;
		for (String row : diagram) {
			for (int c = 0; c < row.length(); c++) {
				char glyph = row.charAt(c);
				if ("#+*?".indexOf(glyph) >= 0) {
					result[0] |= 1 << i;
				}
				if ("Oo*?".indexOf(glyph) >= 0) {
					result[1] |= 1 << i;
				}
				if (".+o?".indexOf(glyph) >= 0) {
					result[2] |= 1 << i;
				}
				i++;
			}
		}
		return result;
	}

	/**
	 * Given a row or column i and the width of the board, returns "actual",
	 * which will be actualFriendly or actualEnemy in the space patternMatcher
	 */
	private int edgePattern(int i, int width) {
		int actual = 0;
		if (i == 0) {
			actual |= (1 << 24);
		} else if (i == 1) {
			actual |= (2 << 24);
		} else if (i == 2) {
			actual |= (3 << 24);
		} else if (i == width - 3) {
			actual |= (4 << 24);
		} else if (i == width - 2) {
			actual |= (5 << 24);
		} else if (i == width - 1) {
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
		for (int i = 0; i <= pattern.length - 3; i += 3) {
			short tempResult = patternMatcher(pattern[i], pattern[i + 1],
					pattern[i + 2]);
			if (tempResult != NO_POINT) {
				return tempResult;
			}
		}
		for (int i = 0; i < vacantPoints.size(); i++) {
			if (board.play(vacantPoints.get(i)) == OK) {
				return vacantPoints.get(i);
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
		if ((pattern[0] & YES) == 0) {
			candidates.remove(p);
			return false;
		}
		CoordinateSystem coords = board.getCoordinateSystem();
		int row = coords.row(p);
		int col = coords.column(p);
		int actualFriendly = edgePattern(row, coords.getWidth());
		int actualEnemy = edgePattern(col, coords.getWidth());
		//TODO edge logic here
		actualFriendly = 0;
		actualEnemy = 0;
		pattern[0] = 0xffffff & pattern[0];
		pattern[1] = 0xffffff & pattern[0];
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
				}
				i++;
			}
		}
		// System.out.println(Integer.toBinaryString(actualFriendly) + " "
		// + Integer.toBinaryString(actualEnemy));
		return (((actualFriendly & (pattern[0] & ~pattern[1])) == (pattern[0] & ~pattern[1]))
				&& (((actualFriendly & (~pattern[0] | pattern[1])) | (pattern[0] & pattern[1])) == (pattern[0] & pattern[1]))
				&& ((actualEnemy & (pattern[1] & ~pattern[0])) == (pattern[1] & ~pattern[0])) 
				&& (((actualEnemy & (~pattern[1] | pattern[0])) | (pattern[1] & pattern[0])) == (pattern[1] & pattern[0])));
	}

	private short spaceMatcherIterator(int... pattern) {
		while (candidates.size() > 0) {
			short p = candidates.get((int) (Math.random() * candidates.size()));
			// System.out.println(p);
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
		//TODO Make this only in two ints (probably only in 1)
		int actualMoves = 0;
		actualMoves |= (historyObserver.get(board.getTurn() - 1));
		if ((pattern[0] & MOVE_LENGTH) != actualMoves){
			candidates.remove((short) ((pattern[0] >> 18) & MOVE_LENGTH));
			return NO_POINT;
		} 
		if ((pattern[0] & PENULTIMATE) == 1){
			actualMoves |= ((historyObserver.get(board.getTurn() - 2)) << 9);
			if (((pattern[0]) & MOVE_LENGTH) != actualMoves){
				candidates.remove((short) ((pattern[0] >> 18) & MOVE_LENGTH));
				return NO_POINT;
			}
		}
//		candidates.contains((short) ((pattern[0] >> 18) & MOVE_LENGTH)) && 
		System.out.println(Integer.toBinaryString(actualMoves) + " " + Integer.toBinaryString(((pattern[0] >> 18) & MOVE_LENGTH)));
		if ((board.play((short) ((pattern[0] >> 18) & MOVE_LENGTH)) == OK)) {
			if ((pattern[0] & YES) != 0) {
				return (short) ((pattern[0] >> 18) & MOVE_LENGTH);
			}
		}
		candidates.remove((short) ((pattern[0] >> 18) & MOVE_LENGTH));
		return NO_POINT;
	}


}
