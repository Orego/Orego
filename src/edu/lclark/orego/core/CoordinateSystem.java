package edu.lclark.orego.core;

/**
 * Coordinate system to convert between a char and other representations of a
 * location. There is no public constructor for this class; instead, use the
 * static method widthOf to get the appropriate instance.
 * <p>
 * A point is represented as a single char. This is an index into a
 * one-dimensional array representing the board, with a buffer of sentinel
 * points around the edges.
 * <p>
 * The standard idiom for accessing all points on the board is:
 * <pre>
 * for (char p : getAllPointsOnBoard()) {
 * 	// Do something with p
 * }
 * </pre>
 * The standard idiom for accessing all neighbors of point p is:
 * <pre>
 * for (int i = 0; i < 4; i++) {
 * 	char n = getNeighbors(p)[i];
 * 	// Do something with n, which might be an off-board point
 * }
 * </pre>
 * To access diagonal neighbors, do the same, but with 0 and 4 replaced with 4
 * and 8.
 * <p>
 * On those rare occasions where rows and columns are used, rows are always
 * zero-based from the top, columns from the left.
 */
public final class CoordinateSystem {

	/** Added to a point to find the one to the east. */
	private static final char EAST = 1;

	/** Instances for various board widths. */
	private static final CoordinateSystem[] instances = new CoordinateSystem[20];

	/** Special value for no point. */
	public static final char NO_POINT = 0;

	/** Special value for passing. */
	public static final char PASS = 1;

	/** Special value for resigning. */
	public static final char RESIGN = 2;

	/** Returns the CoordinateSystem for the specified width. */
	public static CoordinateSystem forWidth(int width) {
		if (instances[width] == null) {
			instances[width] = new CoordinateSystem(width);
		}
		return instances[width];
	}
	
	/**
	 * @see #getAllPointsOnBoard()
	 */
	private char[] allPointsOnBoard;

	/**
	 * @see #getNeighbors(int)
	 */
	private final char[][] neighbors;

	/** Added to a point to find the one to the south. */
	private final char south;
	
	/** Width of the board. */
	private final int width;

	/** Other classes should use forWidth to get an instance. */
	private CoordinateSystem(int width) {
		this.width = width;
		south = (char)(width + 1);
		int boardArea = width * width;
		int extendedBoardArea = (width + 1) * (width + 2) + 1;
		allPointsOnBoard = new char[boardArea];
		int i = 0;
		for (int r = 0; r < width; r++) {
			for (int c = 0; c < width; c++) {
				allPointsOnBoard[i] = at(r, c);
				i++;
			}
		}
		neighbors = new char[extendedBoardArea][];
		for (char p : allPointsOnBoard) {
			neighbors[p] = new char[] {(char)(p - south),
									(char)(p - EAST),
									(char)(p + EAST),
									(char)(p + south),
									(char)(p - south - EAST),
									(char)(p - south + EAST),
									(char)(p + south - EAST),
									(char)(p + south + EAST)};
		}
	}
	
	/** Returns the char representation of the point at row r, column c. */
	public char at(int r, int c) {
		assert isValidOneDimensionalCoordinate(r) : "Invalid row: " + r;
		assert isValidOneDimensionalCoordinate(c) : "Invalid column: " + c;
		return (char)((r + 1) * south + (c + 1) * EAST);
	}
	
	/**
	 * Returns the char representation of the point described by label, which
	 * might be something like "A5", "b3", or "PASS".
	 */
	public char at(String label) {
		label = label.toUpperCase();
		if (label.equals("PASS")) {
			return PASS;
		}
		if (label.equals("RESIGN")) {
			return RESIGN;
		}
		int r = Integer.parseInt(label.substring(1));
		r = width - r;
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
	public int column(char p) {
		return p % south - 1;
	}

	/** Returns a String representation of column c. */
	public String columnToString(int column) {
		return "" + "ABCDEFGHJKLMNOPQRST".charAt(column);
	}

	/** Returns an array of all the points on the board, for iterating through. */
	public char[] getAllPointsOnBoard() {
		return allPointsOnBoard;
	}

	/**
	 * Returns an array of p's four orthogonal neighbors (indices 0-3) and four
	 * diagonal neighbors (4-7). If a point is at the edge (corner) of the
	 * board, one (two) of its neighbors are off-board points. The neighbors of
	 * an off-board point are not defined.
	 * <p>
	 * The neighbors are ordered like this:
	 * 
	 * <pre>
	 * 405
	 * 1 2
	 * 637
	 * </pre>
	 */
	public char[] getNeighbors(char p) {
		return neighbors[p];
	}

	/** Returns true if p is on the board. */
	public boolean isOnBoard(char p) {
		return isValidOneDimensionalCoordinate(row(p)) && isValidOneDimensionalCoordinate(column(p));
	}

	/** Returns true if p is on the third or fourth line. */
	public boolean isOnThirdOrFourthLine(char p) {
		int line = line(p);
		return ((line >= 3) && (line <= 4));
	}

	/** Returns true if c is a valid row or column index. */
	private boolean isValidOneDimensionalCoordinate(int c) {
		return (c >= 0) & (c < width);
	}

	/**
	 * Returns p's line (1-based) from the edge of the board
	 */
	private int line(char p) {
		int r = Math.min(row(p), width - row(p) - 1);
		int c = Math.min(column(p), width - column(p) - 1);
		return 1 + Math.min(r, c);		
	}

	/** Returns the Manhattan distance from p to q. */
	public int manhattanDistance(char p, char q) {
		int rowd = Math.abs(row(p) - row(q));
		int cold = Math.abs(column(p) - column(q));
		return rowd + cold;
	}

	/** Returns a String representation of point. */
	public String pointToString(char p) {
		if (p == PASS) {
			return "PASS";
		} else if (p == NO_POINT) {
			return "NO_POINT";
		} else if (p == RESIGN) {
			return "RESIGN";
		} else {
			return columnToString(column(p)) + rowToString(row(p));
		}
	}

	/** Returns the row of point p. */
	public int row(char p) {
		return p / south - 1;
	}

	/** Returns a String representation of row r. */
	public String rowToString(int row) {
		return "" + (width - row);
	}

}
