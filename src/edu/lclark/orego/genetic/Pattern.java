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
	// TODO Specify yes/no, time/space, edges (for space), 1 or 2 moves (for time)
	/**
	 * Converts a human-readable pattern into three bit vectors. In the diagram, the symbols are:
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
			actual |= (4 << 24);
		} else if (i == 1) {
			actual |= (2 << 24);
		} else if (i == 2) {
			actual |= (6 << 24);
		} else if (i == width - 3) {
			actual |= (1 << 24);
		} else if (i == width - 2) {
			actual |= (3 << 24);
		} else if (i == width - 1) {
			actual |= (5 << 24);
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
		CoordinateSystem coords = board.getCoordinateSystem();
		int actualVacant = 0;
		int row = coords.row(p);
		int col = coords.column(p);
		int actualFriendly = edgePattern(row, coords.getWidth());
		int actualEnemy = edgePattern(col, coords.getWidth());
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
						} else if (color == NonStoneColor.VACANT) {
							actualVacant |= (1 << i);
						}
					}
				}
				i++;
			}
		}
		// System.out.println(Integer.toBinaryString(actualFriendly) + " "
		// + Integer.toBinaryString(actualEnemy) + " "
		// + Integer.toBinaryString(actualVacant));
		if ((pattern[0] & YES) == 0) {
			candidates.remove(p);
			return false;
		}
		return ((((actualFriendly) & (pattern[0])) == (actualFriendly))
				&& (((actualEnemy) & (pattern[1])) == (actualEnemy)) && ((actualVacant & pattern[2]) == actualVacant));
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
	 * Returns the short iff successful play was made and NO_POINT if not. It removes
	 * unsuccessful moves from the list.
	 */
	private short timeMatcher(int... pattern) {
		int ultimate = 0;
		ultimate |= (historyObserver.get(board.getTurn() - 1));
		int penultimate = 0;
		if (pattern[1] > 0) {
			penultimate |= (historyObserver.get(board.getTurn() - 2));
		}
		// System.out.println(Integer.toBinaryString(ultimate) + " " +
		// Integer.toBinaryString(penultimate));
		if (((ultimate & pattern[0]) == ultimate)
				&& ((penultimate & pattern[1]) == penultimate)) {
			if (candidates.contains((short) pattern[2])
					&& (board.play((short) pattern[2]) == OK)) {
				if ((pattern[0] & YES) != 0) {
					return (short) pattern[2];
				}
			}
			candidates.remove((short) pattern[2]);
		}
		return NO_POINT;
	}

}


