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

	/**
	 * These are 361 primes greater than 361 used to skip randomly through a set
	 * of possible moves.
	 */
	public static final int[] PRIMES = { 367, 373, 379, 383, 389, 397, 401,
			409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479,
			487, 491, 499, 503, 509, 521, 523, 541, 547, 557, 563, 569, 571,
			577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647,
			653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739,
			743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827,
			829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919,
			929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997, 1009, 1013,
			1019, 1021, 1031, 1033, 1039, 1049, 1051, 1061, 1063, 1069, 1087,
			1091, 1093, 1097, 1103, 1109, 1117, 1123, 1129, 1151, 1153, 1163,
			1171, 1181, 1187, 1193, 1201, 1213, 1217, 1223, 1229, 1231, 1237,
			1249, 1259, 1277, 1279, 1283, 1289, 1291, 1297, 1301, 1303, 1307,
			1319, 1321, 1327, 1361, 1367, 1373, 1381, 1399, 1409, 1423, 1427,
			1429, 1433, 1439, 1447, 1451, 1453, 1459, 1471, 1481, 1483, 1487,
			1489, 1493, 1499, 1511, 1523, 1531, 1543, 1549, 1553, 1559, 1567,
			1571, 1579, 1583, 1597, 1601, 1607, 1609, 1613, 1619, 1621, 1627,
			1637, 1657, 1663, 1667, 1669, 1693, 1697, 1699, 1709, 1721, 1723,
			1733, 1741, 1747, 1753, 1759, 1777, 1783, 1787, 1789, 1801, 1811,
			1823, 1831, 1847, 1861, 1867, 1871, 1873, 1877, 1879, 1889, 1901,
			1907, 1913, 1931, 1933, 1949, 1951, 1973, 1979, 1987, 1993, 1997,
			1999, 2003, 2011, 2017, 2027, 2029, 2039, 2053, 2063, 2069, 2081,
			2083, 2087, 2089, 2099, 2111, 2113, 2129, 2131, 2137, 2141, 2143,
			2153, 2161, 2179, 2203, 2207, 2213, 2221, 2237, 2239, 2243, 2251,
			2267, 2269, 2273, 2281, 2287, 2293, 2297, 2309, 2311, 2333, 2339,
			2341, 2347, 2351, 2357, 2371, 2377, 2381, 2383, 2389, 2393, 2399,
			2411, 2417, 2423, 2437, 2441, 2447, 2459, 2467, 2473, 2477, 2503,
			2521, 2531, 2539, 2543, 2549, 2551, 2557, 2579, 2591, 2593, 2609,
			2617, 2621, 2633, 2647, 2657, 2659, 2663, 2671, 2677, 2683, 2687,
			2689, 2693, 2699, 2707, 2711, 2713, 2719, 2729, 2731, 2741, 2749,
			2753, 2767, 2777, 2789, 2791, 2797, 2801, 2803, 2819, 2833, 2837,
			2843, 2851, 2857, 2861, 2879, 2887, 2897, 2903, 2909, 2917, 2927,
			2939, 2953, 2957, 2963, 2969, 2971, 2999, 3001, 3011, 3019 };

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
