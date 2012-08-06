package orego.core;

import static orego.experiment.Debug.debug;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages coordinates on the board.
 * <p>
 * A point is represented as a single int. This is an index into a
 * one-dimensional array representing the board, with a buffer of sentinel
 * points around the edges.
 * <p>
 * The standard idiom for accessing all points on the board is:
 * 
 * <pre>
 * for (int p : ALL_POINTS_ON_BOARD) {
 * 	// Do something with p
 * }
 * </pre>
 * 
 * The standard idiom for accessing all neighbors of point p is:
 * 
 * <pre>
 * for (int i = 0; i &lt; 4; i++) {
 * 	int n = NEIGHBORS[p][i];
 * 	// Do something with n, which might be an off-board point with the color
 * 	// OFF_BOARD_COLOR
 * }
 * </pre>
 * 
 * To access diagonal neighbors, do the same, but with 0 and 4 replaced with 4
 * and 8.
 * <p>
 * On those rare occasions where rows and columns are used, rows are always
 * zero-based from the top, columns from the left.
 */
public final class Coordinates {

	// These constants are not in alphabetical order because some are defined in
	// terms of others.

	/** Width of the board, e.g., 9 or 19. */
	public static final int BOARD_WIDTH = 19;

	/** Number of points on the board. */
	public static final int BOARD_AREA = BOARD_WIDTH * BOARD_WIDTH;

	/** An array of all the points on the board, for iterating through. */
	public static final int[] ALL_POINTS_ON_BOARD = new int[BOARD_AREA];

	/**
	 * An array of all the points on the third or fourth line, for iterating
	 * through.
	 */
	public static final int[] THIRD_AND_FOURTH_LINE_POINTS = new int[(BOARD_WIDTH - 6) * 8];

	/** Add this amount to move east one column, or subtract to move west. */
	public static final int EAST = 1;

	/**
	 * Number of points on the board including the buffer of sentinels around
	 * the outside.
	 */
	public static final int EXTENDED_BOARD_AREA = (BOARD_WIDTH + 1)
			* (BOARD_WIDTH + 2) + 1;

	/** Add this amount to move south one row, or subtract to move north. */
	public static final int SOUTH = BOARD_WIDTH + 1;

	/** True for edge and corner points, false for others. */
	public static final boolean[] EDGE_OR_CORNER = new boolean[EXTENDED_BOARD_AREA];

	/**
	 * If there are this many enemy stones on diagonal neighbors, a point is not
	 * eyelike.
	 */
	public static final int[] EYELIKE_THRESHOLD = new int[EXTENDED_BOARD_AREA];

	/** Highest index of any point on the board. */
	public static final int FIRST_POINT_BEYOND_BOARD = BOARD_WIDTH
			* (SOUTH + EAST) + 1;

	/** Useful in loops over board points. */
	public static final int FIRST_POINT_ON_BOARD = SOUTH + EAST;

	// TODO Does the array need to be that large?

	/**
	 * KNIGHT_NEIGHBORHOOD[p] is an array of points within a knight's move of p.
	 */
	public static final int[][] KNIGHT_NEIGHBORHOOD = new int[EXTENDED_BOARD_AREA][];

	/**
	 * LARGE_KNIGHT_NEIGHBORHOOD[p] is an array of points within a large
	 * knight's move of p.
	 */
	public static final int[][] LARGE_KNIGHT_NEIGHBORHOOD = new int[EXTENDED_BOARD_AREA][];

	/**
	 * For each point, the four orthogonal neighbors (indices 0-3) and the four
	 * diagonal neighbors (4-7) and four extentions of the neighborhood
	 * (indeices 8-11). If a point is at the edge (corner) of the board, one
	 * (two) of its neighbors are off-board points. The neighbors of an
	 * off-board point are not defined.
	 * 
	 * The neighbors are ordered like this:
	 * 
	 * <pre>
	 *    8
	 *   405
	 * 9 1 2 10
	 *   637
	 *   11
	 * </pre>
	 */
	public static final int[][][] MANHATTAN_NEIGHBORHOOD = new int[6][EXTENDED_BOARD_AREA][];

	/**
	 * SQUARE_NEIGHTBORHOOD[radius][p][] is an array of points in the square (of
	 * specified radius) around p.
	 */
	public static final int[][][] SQUARE_NEIGHBORHOOD = new int[6][EXTENDED_BOARD_AREA][];

	/**
	 * For each point, the four orthogonal neighbors (indices 0-3) and the four
	 * diagonal neighbors (4-7). If a point is at the edge (corner) of the
	 * board, one (two) of its neighbors are off-board points. The neighbors of
	 * an off-board point are not defined.
	 * 
	 * The neighbors are ordered like this:
	 * 
	 * <pre>
	 * 405
	 * 1 2
	 * 637
	 * </pre>
	 */
	public static final int[][] NEIGHBORS = new int[EXTENDED_BOARD_AREA][8];

	/** Special coordinate for no point. */
	public static final int NO_POINT = 1;

	/**
	 * Special sentinel value for encoding responses in @see
	 * orego.response.ResponsePlayer
	 */
	public static final int ZERO_LEVEL_SENTINEL = 3;

	/** True for points on the board. */
	public static final boolean[] ON_BOARD = new boolean[EXTENDED_BOARD_AREA];

	/** Special coordinate for passing. */
	public static final int PASS = 0;

	/** Special coordinate for a resignation move. */
	public static final int RESIGN = 2;

	/** True for 3rd or 4th line points, false for others. */
	public static final boolean[] THIRD_OR_FOURTH_LINE = new boolean[EXTENDED_BOARD_AREA];

	// Initialize various arrays
	static {
		for (int p = 0; p < EXTENDED_BOARD_AREA; p++) {
			int r = row(p);
			int c = column(p);
			// Avoid branch with non-short-circuited &
			ON_BOARD[p] = isValidOneDimensionalCoordinate(r)
					& isValidOneDimensionalCoordinate(c);
		}
		int i = 0;
		for (int p = FIRST_POINT_ON_BOARD; p < FIRST_POINT_BEYOND_BOARD; p++) {
			if (ON_BOARD[p]) {
				ALL_POINTS_ON_BOARD[i] = p;
				i++;
			}
		}
		int thirdFourthLineCount = 0;
		for (int p : ALL_POINTS_ON_BOARD) {
			EYELIKE_THRESHOLD[p] = 2;
			NEIGHBORS[p][0] = north(p);
			NEIGHBORS[p][1] = west(p);
			NEIGHBORS[p][2] = east(p);
			NEIGHBORS[p][3] = south(p);
			NEIGHBORS[p][4] = northwest(p);
			NEIGHBORS[p][5] = northeast(p);
			NEIGHBORS[p][6] = southwest(p);
			NEIGHBORS[p][7] = southeast(p);
			for (i = 4; i < 8; i++) {
				int n = NEIGHBORS[p][i];
				if (!ON_BOARD[n]) {
					EDGE_OR_CORNER[p] = true;
					EYELIKE_THRESHOLD[p] = 1;
				}
			}
			int line = line(p);
			if ((line >= 3) && (line <= 4)) {
				THIRD_OR_FOURTH_LINE[p] = true;
				THIRD_AND_FOURTH_LINE_POINTS[thirdFourthLineCount] = p;
				thirdFourthLineCount++;
			}
			KNIGHT_NEIGHBORHOOD[p] = findKnightNeighborhood(p);
			LARGE_KNIGHT_NEIGHBORHOOD[p] = findLargeKnightNeighborhood(p);
			for (int k = 1; k < SQUARE_NEIGHBORHOOD.length; k++) {
				SQUARE_NEIGHBORHOOD[k][p] = findSquareNeighborhood(k, p);
			}
			for (int k = 1; k < MANHATTAN_NEIGHBORHOOD.length; k++) {
				MANHATTAN_NEIGHBORHOOD[k][p] = findManhattanNeighborhood(k, p);
			}
		}
		
	}

	public static int[] findManhattanNeighborhood(int radius, int p) {
		List<Integer> neighbors = new ArrayList<Integer>();
		for (int i : ALL_POINTS_ON_BOARD) {
			if (!isValidOneDimensionalCoordinate(row(i))
					|| !isValidOneDimensionalCoordinate(column(i))) {
				continue;
			}
			if ((manhattanDistance(p, i)) <= radius) {
				neighbors.add(i);
			}
		}
		int[] result = new int[neighbors.size()];
		for (int k = 0; k < neighbors.size(); k++) {
			result[k] = neighbors.get(k);
		}
		return result;
	}

	/** Returns the int representation of the point at row r, column c. */
	public static int at(int r, int c) {
		assert isValidOneDimensionalCoordinate(r) : "Invalid row: " + r;
		assert isValidOneDimensionalCoordinate(c) : "Invalid column: " + c;
		return (r + 1) * SOUTH + (c + 1) * EAST;
	}

	/**
	 * Returns the int representation of the point described by label, which
	 * might be something like "A5", "b3", or "PASS".
	 */
	public static int at(String label) {
		label = label.toUpperCase();
		if (label.equals("PASS")) {
			return PASS;
		}
		if (label.equals("RESIGN")) {
			return RESIGN;
		}
		int r = Integer.parseInt(label.substring(1));
		r = BOARD_WIDTH - r;
		int c;
		char letter = label.charAt(0);
		if (letter <= 'H') {
			c = letter - 'A';
		} else {
			c = letter - 'B';
		}
		return at(r, c);
	}

	/** Returns the column of point p. */
	public static int column(int p) {
		return p % SOUTH - 1;
	}

	/** Returns a String representation of column c. */
	public static String columnToString(int c) {
		// Note that, as per convention, I is missing
		return "" + "ABCDEFGHJKLMNOPQRST".charAt(c);
	}

	/**
	 * Returns the column c as a lower case letter. This is used for the sgf
	 * format.
	 */
	public static char columnToChar(int c) {
		return (char) (c + 'a');
	}

	/** Returns the Euclidean distance from p1 to p2. */
	public static double distance(int p1, int p2) {
		int rowd = Math.abs(row(p1) - row(p2));
		int cold = Math.abs(column(p1) - column(p2));
		return Math.sqrt(rowd * rowd + cold * cold);
	}

	/** Returns the point east of p (which may be off the board). */
	protected static int east(int p) {
		return p + EAST;
	}

	/**
	 * Used in the static block that initializes KNIGHT_NEIGHBORHOOD.
	 */
	protected static int[] findKnightNeighborhood(int p) {
		int r = row(p), c = column(p);
		int offset[][] = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 },
				{ -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }, { -2, 0 },
				{ 2, 0 }, { 0, -2 }, { 0, 2 }, { -2, -1 }, { -2, 1 },
				{ -1, -2 }, { -1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { 1, 2 } };

		int large[] = new int[offset.length];
		int count = 0;
		for (int i = 0; i < offset.length; i++) {
			if (isValidOneDimensionalCoordinate(r + offset[i][0])
					&& (isValidOneDimensionalCoordinate(c + offset[i][1]))) {
				large[i] = at(r + offset[i][0], c + offset[i][1]);
				count++;
			}
		}
		// Create a small array and copy the elements
		int valid[] = new int[count];
		int v = 0;
		for (int i = 0; i < offset.length; i++)
			if (large[i] > 0) {
				valid[v] = large[i];
				v++;
			}
		return valid;
	}

	/**
	 * Used in the static block that initializes LARGE_KNIGHT_NEIGHBORHOOD.
	 */
	protected static int[] findLargeKnightNeighborhood(int p) {
		int r = row(p), c = column(p);
		int offset[][] = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 },
				{ -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }, { -2, 0 },
				{ 2, 0 }, { 0, -2 }, { 0, 2 }, { -2, -1 }, { -2, 1 },
				{ -1, -2 }, { -1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 },
				{ 1, 2 }, { 2, 2 }, { 2, -2 }, { -2, 2 }, { -2, -2 }, { 3, 0 },
				{ -3, 0 }, { 0, -3 }, { 0, 3 }, { 3, 1 }, { 3, -1 },
				{ -1, -3 }, { 1, -3 }, { -3, -1 }, { -3, 1 }, { -1, 3 },
				{ 1, 3 } };
		int large[] = new int[offset.length];
		int count = 0;
		for (int i = 0; i < offset.length; i++) {
			if (isValidOneDimensionalCoordinate(r + offset[i][0])
					&& (isValidOneDimensionalCoordinate(c + offset[i][1]))) {
				large[i] = at(r + offset[i][0], c + offset[i][1]);
				count++;
			}
		}
		// Create a small array and copy the elements
		int valid[] = new int[count];
		int v = 0;
		for (int i = 0; i < offset.length; i++)
			if (large[i] > 0) {
				valid[v] = large[i];
				v++;
			}
		return valid;
	}

	/**
	 * Used in the static block that initializes SQUARE_NEIGHBORHOOD
	 * 
	 */
	private static int[] findSquareNeighborhood(int radius, int p) {
		int r = row(p);
		int c = column(p);
		List<Integer> neighbors = new ArrayList<Integer>();
		for (int i = -radius; i <= radius; i++) {
			int row = r + i;
			if (!isValidOneDimensionalCoordinate(row)) {
				continue;
			}
			for (int j = -radius; j <= radius; j++) {
				int col = c + j;
				if (!isValidOneDimensionalCoordinate(col)
						|| ((row == r) && (col == c))) {
					continue;
				}
				neighbors.add(at(row, col));
			}
		}
		int[] result = new int[neighbors.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = neighbors.get(i);
		}
		return result;
	}

	/** Verifies that a row or column index is valid. */
	protected static boolean isValidOneDimensionalCoordinate(int c) {
		return (c >= 0) & (c < BOARD_WIDTH);
	}

	/**
	 * Returns p's line (1-based) from the edge of the board
	 */
	public static int line(int p) {
		// Find the closest distance to wall
		int r = Math.min(row(p), BOARD_WIDTH - row(p) - 1);
		int c = Math.min(column(p), BOARD_WIDTH - column(p) - 1);
		return 1 + Math.min(r, c);
	}

	/** Returns the Manhattan distance from p1 to p2. */
	public static double manhattanDistance(int p1, int p2) {
		int rowd = Math.abs(row(p1) - row(p2));
		int cold = Math.abs(column(p1) - column(p2));
		return rowd + cold;
	}

	/** Returns the point north of p (which may be off the board). */
	protected static int north(int p) {
		return p - SOUTH;
	}

	/** Returns the point northeast of p (which may be off the board). */
	protected static int northeast(int p) {
		return north(east(p));
	}

	/** Returns the point northwest of p (which may be off the board). */
	protected static int northwest(int p) {
		return north(west(p));
	}

	/** Returns a String representation of point. */
	public static String pointToString(int p) {
		if (p == PASS) {
			return "PASS";
		} else if (p == NO_POINT) {
			return "NO_POINT";
		} else if (p == RESIGN) {
			return "RESIGN";
		} else {
			assert ON_BOARD[p];
			int r = row(p);
			int c = column(p);
			return columnToString(c) + rowToString(r);
		}
	}

	/** Returns the row of point p. */
	public static int row(int p) {
		return p / SOUTH - 1;
	}

	/** Returns a String representation of row r. */
	public static String rowToString(int r) {
		return "" + (BOARD_WIDTH - r);
	}

	/**
	 * Returns the row r as a lower case letter. This is used for the sgf
	 * format.
	 */
	public static char rowToChar(int r) {
		return (char) ((BOARD_WIDTH - r) + 'a' - 1);
	}

	/** Returns the point represented by an sgf String. */
	public static int sgfToPoint(String label) {
		int c = label.charAt(0) - 'a';
		int r = label.charAt(1) - 'a';
		assert isValidOneDimensionalCoordinate(r) : "Invalid row: " + r;
		assert isValidOneDimensionalCoordinate(c) : "Invalid column: " + c;
		return (r + 1) * SOUTH + (c + 1) * EAST;
	}

	/**
	 * Given an SGF-formatted move, returns the corresponding Orego int
	 * representation. If the move cannot be meaningfully interpreted, NO_POINT
	 * is returned.
	 */
	protected int sgfToOregoCoordinate(String currentToken) {
		if (currentToken.equals("B[??]") || currentToken.equals("W[??]")) {
			debug(currentToken + " makes no sense");
			return NO_POINT;
		}
		if (currentToken.equals("B[]") || currentToken.equals("W[]")) {
			return PASS;
		}
		if (currentToken.length() != 5) {
			debug(currentToken + " makes no sense");
			return NO_POINT;
		}
		char char2 = currentToken.charAt(2);
		if (char2 == 't') {
			return PASS;
		}
		int char3 = currentToken.charAt(3);
		int result = -1;
		try {
			result = at(char3 - 'a', char2 - 'a');
		} catch (AssertionError e) {
			debug("Couldn't make sense of " + currentToken);
			e.printStackTrace();
			System.exit(1);
		}
		return result;
	}

	/** Returns the point south of p (which may be off the board). */
	protected static int south(int p) {
		return p + SOUTH;
	}

	/** Returns the point southeast of p (which may be off the board). */
	protected static int southeast(int p) {
		return south(east(p));
	}

	/** Returns the point southwest of p (which may be off the board). */
	protected static int southwest(int p) {
		return south(west(p));
	}

	/** Returns the point west of p (which may be off the board). */
	protected static int west(int p) {
		return p - EAST;
	}

}
