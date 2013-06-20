package orego.core;

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
 * for (int p : getAllPointsOnBoard()) {
 * 	// Do something with p
 * }
 * </pre>
 * 
 * The standard idiom for accessing all neighbors of point p is:
 * 
 * <pre>
 * for (int i = 0; i &lt; 4; i++) {
 * 	int n = getNeighbors()[p][i];
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
 * <p>
 * This class makes extensive use of mutable static fields, which are accessed
 * using static methods. This is usually bad style, but since these fields are
 * accessed so often from so many places, we presume that the singleton pattern
 * would result in more complicated code and possibly a slowdown.
 */
public final class Coordinates {

	/**
	 * @see #getAllPointsOnBoard()
	 */
	private static int[] allPointsOnBoard;

	/**
	 * @see #getBoardArea()
	 */
	private static int boardArea;

	/**
	 * @see #getBoardWidth()
	 */
	private static int boardWidth;

	/** Add this amount to move east one column, or subtract to move west. */
	public static final int EAST = 1;

	/**
	 * @see #getExtendedBoardArea()
	 */
	private static int extendedBoardArea;

	/**
	 * @see #getEyelikeThreshold(int)
	 */
	private static int[] eyelikeThreshold;

	/**
	 * @see #getFirstPointBeyondBoard()
	 */
	private static int firstPointBeyondBoard;

	/**
	 * @see #getFirstPointOnBoard()
	 */
	private static int firstPointOnBoard;

	/**
	 * @see #getKnightNeighborhood(int)
	 */
	private static int[][] knightNeighborhood;

	/**
	 * @see #getLargeKnightNeighborhood(int)
	 */
	private static int[][] largeKnightNeighborhood;

	/**
	 * @see #getNeighbors(int)
	 */
	private static int[][] neighbors;

	/** Special coordinate for no point. */
	public static final int NO_POINT = 1;

	/**
	 * @see #isOnBoard(int)
	 */
	private static boolean[] onBoard;

	/** Special coordinate for passing. */
	public static final int PASS = 0;

	/** Special coordinate for a resignation move. */
	public static final int RESIGN = 2;

	/** Add this amount to move south one row, or subtract to move north. */
	private static int south;

	/**
	 * @see #isOnThirdOrFourthLine(int)
	 */
	private static boolean[] thirdOrFourthLine;

	// Initialize the fields for the default board size
	static {
		setBoardWidth(19);
	}

	/** Returns the int representation of the point at row r, column c. */
	public static int at(int r, int c) {
		assert isValidOneDimensionalCoordinate(r) : "Invalid row: " + r;
		assert isValidOneDimensionalCoordinate(c) : "Invalid column: " + c;
		return (r + 1) * south + (c + 1) * EAST;
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
		r = boardWidth - r;
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
		return p % south - 1;
	}

	/**
	 * Returns the column c as a lower case letter. This is used for sgf.
	 */
	public static char columnToSgfChar(int c) {
		return (char) (c + 'a');
	}

	/** Returns a String representation of column c. */
	public static String columnToString(int c) {
		// Note that, as per convention, I is missing
		return "" + "ABCDEFGHJKLMNOPQRST".charAt(c);
	}

	/** Returns the Euclidean distance from p1 to p2. */
	public static double distance(int p1, int p2) {
		int rowd = Math.abs(row(p1) - row(p2));
		int cold = Math.abs(column(p1) - column(p2));
		return Math.sqrt(rowd * rowd + cold * cold);
	}

	/**
	 * Returns an array of points near p, with "near" defined by the array of
	 * {row, column} offsets. Used by reset().
	 */
	protected static int[] findNeighborhood(int p, int[][] offsets) {
		int r = row(p), c = column(p);
		int large[] = new int[offsets.length];
		int count = 0;
		for (int i = 0; i < offsets.length; i++) {
			if (isValidOneDimensionalCoordinate(r + offsets[i][0])
					&& (isValidOneDimensionalCoordinate(c + offsets[i][1]))) {
				large[i] = at(r + offsets[i][0], c + offsets[i][1]);
				count++;
			}
		}
		// Create a small array and copy the elements into it
		int result[] = new int[count];
		int v = 0;
		for (int i = 0; i < offsets.length; i++)
			if (large[i] > 0) {
				result[v] = large[i];
				v++;
			}
		return result;
	}

	/** Returns an array of all the points on the board, for iterating through. */
	public static int[] getAllPointsOnBoard() {
		return allPointsOnBoard;
	}

	/** Returns the number of points on the board. */
	public static int getBoardArea() {
		return boardArea;
	}

	/**
	 * Returns the width of the board.
	 */
	public static int getBoardWidth() {
		return boardWidth;
	}

	/**
	 * Returns the number of points on the board including the buffer of
	 * sentinels around the outside.
	 */
	public static int getExtendedBoardArea() {
		return extendedBoardArea;
	}

	/**
	 * Returns the eyelike threshold for p (1 or 2). If there are this many
	 * enemy stones on diagonal neighbors of p, it is not eyelike.
	 */
	public static int getEyelikeThreshold(int p) {
		return eyelikeThreshold[p];
	}

	/** Returns one more than the highest index of any point on the board. */
	public static int getFirstPointBeyondBoard() {
		return firstPointBeyondBoard;
	}

	/** Returns the lowest index of any point on the board. */
	public static int getFirstPointOnBoard() {
		return firstPointOnBoard;
	}

	/**
	 * Returns an array of points within a knight's move of p.
	 */
	public static int[] getKnightNeighborhood(int p) {
		return knightNeighborhood[p];
	}

	/**
	 * Returns an array of points within a large knight's move of p.
	 */
	public static int[] getLargeKnightNeighborhood(int p) {
		return largeKnightNeighborhood[p];
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
	public static int[] getNeighbors(int p) {
		return neighbors[p];
	}

	/** Returns the number to be added to a point to move south one row. */
	public static int getSouth() {
		return south;
	}

	/** Returns true if p is on the board. */
	public static boolean isOnBoard(int p) {
		return onBoard[p];
	}

	/** Returns true if p is on the third or fourth line. */
	public static boolean isOnThirdOrFourthLine(int p) {
		return thirdOrFourthLine[p];
	}

	/** Verifies that a row or column index is valid. */
	protected static boolean isValidOneDimensionalCoordinate(int c) {
		return (c >= 0) & (c < boardWidth);
	}

	/**
	 * Returns p's line (1-based) from the edge of the board
	 */
	public static int line(int p) {
		// Find the closest distance to wall
		int r = Math.min(row(p), boardWidth - row(p) - 1);
		int c = Math.min(column(p), boardWidth - column(p) - 1);
		return 1 + Math.min(r, c);
	}

	/** Returns the Manhattan distance from p1 to p2. */
	public static int manhattanDistance(int p1, int p2) {
		int rowd = Math.abs(row(p1) - row(p2));
		int cold = Math.abs(column(p1) - column(p2));
		return rowd + cold;
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
			return columnToString(column(p)) + rowToString(row(p));
		}
	}

	/** Returns the row of point p. */
	public static int row(int p) {
		return p / south - 1;
	}

	/**
	 * Returns the row r as a lower case letter. This is used for sgf format.
	 */
	public static char rowToSgfChar(int r) {
		return (char) ((boardWidth - r) + 'a' - 1);
	}

	/** Returns a String representation of row r. */
	public static String rowToString(int r) {
		return "" + (boardWidth - r);
	}

	/**
	 * Sets the board width. This must be called before other classes (e.g.,
	 * Players) are loaded, because they may build data structures that depend
	 * on the state of this class (which is initialized by this method). This is
	 * done by orego.ui.Orego.handleCommandLineArguments().
	 */
	public static void setBoardWidth(int width) {
		boardWidth = width;
		boardArea = boardWidth * boardWidth;
		allPointsOnBoard = new int[boardArea];
		extendedBoardArea = (boardWidth + 1) * (boardWidth + 2) + 1;
		south = boardWidth + 1;
		eyelikeThreshold = new int[extendedBoardArea];
		firstPointBeyondBoard = boardWidth * (south + EAST) + 1;
		firstPointOnBoard = south + EAST;
		knightNeighborhood = new int[firstPointBeyondBoard][];
		largeKnightNeighborhood = new int[firstPointBeyondBoard][];
		neighbors = new int[extendedBoardArea][8];
		onBoard = new boolean[extendedBoardArea];
		thirdOrFourthLine = new boolean[extendedBoardArea];
		for (int p = 0; p < extendedBoardArea; p++) {
			int r = row(p);
			int c = column(p);
			// Avoid branch with non-short-circuited &
			onBoard[p] = isValidOneDimensionalCoordinate(r)
					& isValidOneDimensionalCoordinate(c);
		}
		int i = 0;
		for (int p = firstPointOnBoard; p < firstPointBeyondBoard; p++) {
			if (onBoard[p]) {
				allPointsOnBoard[i] = p;
				i++;
			}
		}
		for (int p : allPointsOnBoard) {
			eyelikeThreshold[p] = 2;
			neighbors[p][0] = p - south;
			neighbors[p][1] = p - EAST;
			neighbors[p][2] = p + EAST;
			neighbors[p][3] = p + south;
			neighbors[p][4] = p - south - EAST;
			neighbors[p][5] = p - south + EAST;
			neighbors[p][6] = p + south - EAST;
			neighbors[p][7] = p + south + EAST;
			for (i = 4; i < 8; i++) {
				int n = neighbors[p][i];
				if (!onBoard[n]) {
					eyelikeThreshold[p] = 1;
				}
			}
			int line = line(p);
			if ((line >= 3) && (line <= 4)) {
				thirdOrFourthLine[p] = true;
			}
			knightNeighborhood[p] = findNeighborhood(p, new int[][] {
					{ 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 }, { -1, -1 },
					{ -1, 1 }, { 1, -1 }, { 1, 1 }, { -2, 0 }, { 2, 0 },
					{ 0, -2 }, { 0, 2 }, { -2, -1 }, { -2, 1 }, { -1, -2 },
					{ -1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { 1, 2 } });
			largeKnightNeighborhood[p] = findNeighborhood(p, new int[][] {
					{ 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 }, { -1, -1 },
					{ -1, 1 }, { 1, -1 }, { 1, 1 }, { -2, 0 }, { 2, 0 },
					{ 0, -2 }, { 0, 2 }, { -2, -1 }, { -2, 1 }, { -1, -2 },
					{ -1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { 1, 2 },
					{ 2, 2 }, { 2, -2 }, { -2, 2 }, { -2, -2 }, { 3, 0 },
					{ -3, 0 }, { 0, -3 }, { 0, 3 }, { 3, 1 }, { 3, -1 },
					{ -1, -3 }, { 1, -3 }, { -3, -1 }, { -3, 1 }, { -1, 3 },
					{ 1, 3 } });
		}

	}

}
