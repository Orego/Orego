package edu.lclark.orego.feature;

import static edu.lclark.orego.core.CoordinateSystem.MAX_POSSIBLE_BOARD_WIDTH;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

/** True if p is "near" another stone, i.e., within a large knight's move. */
@SuppressWarnings("serial")
public final class NearAnotherStone implements Predicate {

	/**
	 * Values of neighborhoods for each board width.
	 */
	private static final short[][][] NEIGHBORHOODS = new short[MAX_POSSIBLE_BOARD_WIDTH + 1][][];

	/** Row and column offsets of nearby points, expanding outward. */
	public static final short[][] OFFSETS = { { 0, -1 }, { 0, 1 }, { -1, 0 },
			{ 1, 0 }, { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }, { -2, 0 },
			{ 2, 0 }, { 0, -2 }, { 0, 2 }, { -2, -1 }, { -2, 1 }, { -1, -2 },
			{ -1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { 1, 2 }, { 2, 2 },
			{ 2, -2 }, { -2, 2 }, { -2, -2 }, { 3, 0 }, { -3, 0 }, { 0, -3 },
			{ 0, 3 }, { 3, 1 }, { 3, -1 }, { -1, -3 }, { 1, -3 }, { -3, -1 },
			{ -3, 1 }, { -1, 3 }, { 1, 3 } };

	/**
	 * Returns an array containing the coordinates of all on-board points within
	 * a large knight's move of p.
	 */
	private static short[] findNeighborhood(short p, CoordinateSystem coords) {
		final int r = coords.row(p), c = coords.column(p);
		final short[] result = new short[OFFSETS.length];
		int count = 0;
		for (int i = 0; i < OFFSETS.length; i++) {
			final int rr = r + OFFSETS[i][0];
			final int cc = c + OFFSETS[i][1];
			if (coords.isValidOneDimensionalCoordinate(rr)
					&& coords.isValidOneDimensionalCoordinate(cc)) {
				result[count] = coords.at(rr, cc);
				count++;
			}
		}
		// Create a small array and copy the elements into it
		return java.util.Arrays.copyOf(result, count);
	}

	private final Board board;

	/**
	 * Large-knight neighborhoods around points. First index is point around
	 * which neighborhood is defined.
	 */
	private final short[][] neighborhoods;

	public NearAnotherStone(Board board) {
		this.board = board;
		final CoordinateSystem coords = board.getCoordinateSystem();
		final int width = coords.getWidth();
		if (NEIGHBORHOODS[width] == null) {
			final short[] pointsOnBoard = coords.getAllPointsOnBoard();
			NEIGHBORHOODS[width] = new short[coords.getFirstPointBeyondBoard()][];
			for (final short p : pointsOnBoard) {
				NEIGHBORHOODS[width][p] = findNeighborhood(p, coords);
			}
		}
		neighborhoods = NEIGHBORHOODS[width];
	}

	@Override
	public boolean at(short p) {
		for (final short q : neighborhoods[p]) {
			if (board.getColorAt(q) != VACANT) {
				return true;
			}
		}
		return false;
	}

}
