package edu.lclark.orego.core;

import static java.lang.Math.abs;

import java.io.Serializable;

import edu.lclark.orego.mcts.CopiableStructure;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

/**
 * Coordinate system to convert between a short and other representations of a
 * location. There is no public constructor for this class; instead, use the
 * static method widthOf to get the appropriate instance.
 * <p>
 * A point is represented as a single short. This is an index into a
 * one-dimensional array representing the board, with a buffer of sentinel
 * points around the edges.
 * <p>
 * The standard idiom for accessing all points on the board is:
 *
 * <pre>
 * for (short p : getAllPointsOnBoard()) {
 * 	// Do something with p
 * }
 * </pre>
 *
 * The standard idiom for traversing all orthogonal neighbors of point p is:
 *
 * <pre>
 * short[] neighbors = getNeighbors(p);
 * for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i &lt;= LAST_ORTHOGONAL_NEIGHBOR; i++) {
 * 	short n = neighbors[i];
 * 	// Do something with n, which might be an off-board point
 * }
 * </pre>
 *
 * To traverse diagonal neighbors, do the same, but with DIAGONAL substituted
 * for ORTHOGONAL. To traverse both, use a for-each loop on neighbors.
 * <p>
 * On those rare occasions where rows and columns are used, rows are always
 * zero-based from the top, columns from the left.
 */
@SuppressWarnings("serial")
public final class CoordinateSystem implements Serializable {

	/** Added to a point to find the one to the east. */
	private static final short EAST = 1;

	/** Index into an array returned by getNeighbors. */
	public static final int EAST_NEIGHBOR = 2;

	/**
	 * Orego doesn't support larger board sizes. This is a constant to avoid
	 * magic numbers.
	 */
	public static final int MAX_POSSIBLE_BOARD_WIDTH = 19;

	/** Special value for no point. */
	public static final short NO_POINT = 0;

	/** Index into an array returned by getNeighbors. */
	public static final int NORTH_NEIGHBOR = 0;

	/** Index into an array returned by getNeighbors. */
	public static final int NORTHEAST_NEIGHBOR = 5;

	/** Index into an array returned by getNeighbors. */
	public static final int NORTHWEST_NEIGHBOR = 4;

	/** Special value for passing. */
	public static final short PASS = 1;

	/** Special value for resigning. */
	public static final short RESIGN = 2;

	/** Index into an array returned by getNeighbors. */
	public static final int SOUTH_NEIGHBOR = 3;

	/** Index into an array returned by getNeighbors. */
	public static final int SOUTHEAST_NEIGHBOR = 7;

	/** Index into an array returned by getNeighbors. */
	public static final int SOUTHWEST_NEIGHBOR = 6;

	/** Index into an array returned by getNeighbors. */
	public static final int WEST_NEIGHBOR = 1;

	// The remaining constants are out of order because they are defined in
	// terms of others

	/** Index into an array returned by getNeighbors. */
	public static final int FIRST_DIAGONAL_NEIGHBOR = NORTHWEST_NEIGHBOR;

	/** Index into an array returned by getNeighbors. */
	public static final int FIRST_ORTHOGONAL_NEIGHBOR = NORTH_NEIGHBOR;

	/** Instances for various board widths. */
	private static final CoordinateSystem[] INSTANCES = new CoordinateSystem[MAX_POSSIBLE_BOARD_WIDTH + 1];

	/** Index into an array returned by getNeighbors. */
	public static final int LAST_DIAGONAL_NEIGHBOR = SOUTHEAST_NEIGHBOR;

	/** Index into an array returned by getNeighbors. */
	public static final int LAST_ORTHOGONAL_NEIGHBOR = SOUTH_NEIGHBOR;

	/** Returns a String representation of column c. */
	public static String columnToString(int column) {
		return "" + "ABCDEFGHJKLMNOPQRST".charAt(column);
	}

	/** Returns the unique CoordinateSystem for the specified width. */
	public static CoordinateSystem forWidth(int width) {
		if (INSTANCES[width] == null) {
			INSTANCES[width] = new CoordinateSystem(width);
		}
		return INSTANCES[width];
	}

	/**
	 * @see #getAllPointsOnBoard()
	 */
	private final short[] allPointsOnBoard;

	/**
	 * @see #getMaxMovesPerGame()
	 */
	private final short maxMovesPerGame;

	/**
	 * @see #getNeighbors(short)
	 */
	private final short[][] neighbors;

	/** Added to a point to find the one to the south. */
	private final short south;

	/** Width of the board. */
	private final int width;

	/**
	 * Random numbers for Zobrist hashes, indexed by color and point. The last
	 * row is for the simple ko point.
	 */
	private final long[][] zobristHashes;

	/** Other classes should use forWidth to get an instance. */
	private CoordinateSystem(int width) {
		this.width = width;
		south = (short) (width + 1);
		final short boardArea = (short) (width * width);
		allPointsOnBoard = new short[boardArea];
		for (int r = 0, i = 0; r < width; r++) {
			for (int c = 0; c < width; c++, i++) {
				allPointsOnBoard[i] = at(r, c);
			}
		}
		maxMovesPerGame = (short) (boardArea * 3);
		final int n = getFirstPointBeyondBoard();
		neighbors = new short[n][];
		zobristHashes = new long[2][n];
		final MersenneTwisterFast random = new MersenneTwisterFast(0L);
		for (final short p : allPointsOnBoard) {
			neighbors[p] = new short[] { (short) (p - south),
					(short) (p - EAST), (short) (p + EAST),
					(short) (p + south), (short) (p - south - EAST),
					(short) (p - south + EAST), (short) (p + south - EAST),
					(short) (p + south + EAST) };
			for (int i = 0; i < zobristHashes.length; i++) {
				zobristHashes[i][p] = random.nextLong();
			}
		}
	}

	/** Returns the short representation of the point at row r, column c. */
	public short at(int r, int c) {
		assert isValidOneDimensionalCoordinate(r) : "Invalid row: " + r;
		assert isValidOneDimensionalCoordinate(c) : "Invalid column: " + c;
		return (short) ((r + 1) * south + (c + 1) * EAST);
	}

	/**
	 * Returns the short representation of the point described by label, which
	 * might be something like "A5", "b3", or "PASS".
	 */
	public short at(String label) {
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
		final char letter = label.charAt(0);
		if (letter <= 'H') {
			c = letter - 'A';
		} else {
			c = letter - 'B';
		}
		return at(r, c);
	}

	/** Returns the column of point p. */
	public int column(short p) {
		return p % south - 1;
	}

	/** Returns an array of all the points on the board, for iterating through. */
	public short[] getAllPointsOnBoard() {
		return allPointsOnBoard;
	}

	/** Returns the number of points on the board. */
	public int getArea() {
		return width * width;
	}

	/**
	 * Returns the index of the first point beyond the board. This is useful as
	 * the size of any array that must have an entry for any point on the board.
	 */
	public short getFirstPointBeyondBoard() {
		return (short) (width * (south + EAST) + 1);
	}

	/**
	 * Returns the index of the first point beyond the extended board (which
	 * includes the sentinels around the outside.) This is useful as the size of
	 * any array that must have an entry for any point or sentinel.
	 */
	public short getFirstPointBeyondExtendedBoard() {
		return (short) ((width + 1) * (width + 2) + 1);
	}

	/** Returns the random number for playing a stone of color at p. */
	long getHash(Color color, short p) {
		return zobristHashes[color.index()][p];
	}

	/**
	 * Returns the maximum number of moves per game. It should be extremely rare
	 * to actually play this many moves, but a playout (which doesn't check
	 * superko) might run this long. It's faster to just cut off such unusual
	 * runs (by forcing passes) than to check for superko in playouts.
	 */
	public short getMaxMovesPerGame() {
		return maxMovesPerGame;
	}

	/**
	 * Returns an array of p's four orthogonal neighbors and four diagonal
	 * neighbors. If a point is at the edge (corner) of the board, one (two) of
	 * its neighbors are off-board points. The neighbors of an off-board point
	 * are not defined.
	 * <p>
	 *
	 * @see edu.lclark.orego.core.CoordinateSystem
	 */
	public short[] getNeighbors(short p) {
		return neighbors[p];
	}

	/** Returns the width of the board (e.g., 19). */
	public int getWidth() {
		return width;
	}

	/** Returns true if p is on the board. */
	public boolean isOnBoard(short p) {
		return isValidOneDimensionalCoordinate(row(p))
				&& isValidOneDimensionalCoordinate(column(p));
	}

	/** Returns true if c is a valid row or column index. */
	public boolean isValidOneDimensionalCoordinate(int c) {
		return c >= 0 & c < width;
	}

	/** Returns the Manhattan distance from p to q. */
	public int manhattanDistance(short p, short q) {
		final int rowd = abs(row(p) - row(q));
		final int cold = abs(column(p) - column(q));
		return rowd + cold;
	}

	/**
	 * Used so that serialization, as used in CopiableStructure, does not create
	 * redundant CoordinateSystems.
	 *
	 * @see CopiableStructure
	 */
	private Object readResolve() {
		return forWidth(width);
	}

	/** Returns the row of point p. */
	public int row(short p) {
		return p / south - 1;
	}

	/** Returns a String representation of row r. */
	public String rowToString(int row) {
		return "" + (width - row);
	}

	/** Returns a String representation of p. */
	public String toString(short p) {
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

}
