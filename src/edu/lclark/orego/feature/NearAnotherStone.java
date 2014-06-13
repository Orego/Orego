package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.CoordinateSystem.MAX_POSSIBLE_BOARD_WIDTH;
import static edu.lclark.orego.core.NonStoneColor.*;

/** True if p is "near" another stone, i.e., within a large knight's move. */
@SuppressWarnings("serial")
public final class NearAnotherStone implements Predicate {

	/**
	 * Values of neighborhoods for each board width.
	 */
	private static final short[][][] NEIGHBORHOODS = new short[MAX_POSSIBLE_BOARD_WIDTH + 1][][];

	private final Board board;

	/**
	 * Large-knight neighborhoods around points. First index is point around
	 * which neighborhood is defined.
	 */
	private final short[][] neighborhoods;

	/** Row and column offsets of nearby points. */
	public static final short[][] OFFSETS = { { 0, -1 }, { 0, 1 }, { -1, 0 },
			{ 1, 0 }, { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }, { -2, 0 },
			{ 2, 0 }, { 0, -2 }, { 0, 2 }, { -2, -1 }, { -2, 1 }, { -1, -2 },
			{ -1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { 1, 2 }, { 2, 2 },
			{ 2, -2 }, { -2, 2 }, { -2, -2 }, { 3, 0 }, { -3, 0 }, { 0, -3 },
			{ 0, 3 }, { 3, 1 }, { 3, -1 }, { -1, -3 }, { 1, -3 }, { -3, -1 },
			{ -3, 1 }, { -1, 3 }, { 1, 3 } };

	public NearAnotherStone(Board board) {
		this.board = board;
		CoordinateSystem coords = board.getCoordinateSystem();
		int width = coords.getWidth();
		if (NEIGHBORHOODS[width] == null) {
			short[] pointsOnBoard = coords.getAllPointsOnBoard();
			NEIGHBORHOODS[width] = new short[coords.getFirstPointBeyondBoard()][];
			for (short p : pointsOnBoard) {
				NEIGHBORHOODS[width][p] = findNeighborhood(p, coords);
			}
		}
		neighborhoods = NEIGHBORHOODS[width];
	}

	@Override
	public boolean at(short p) {
		for (int i = 0; i < neighborhoods[p].length; i++) {
			if (board.getColorAt(neighborhoods[p][i]) != VACANT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns an array containing the coordinates of all on-board points within
	 * a large knight's move of p.
	 */
	private static short[] findNeighborhood(short p, CoordinateSystem coords) {
		int r = coords.row(p), c = coords.column(p);
		short[] result = new short[OFFSETS.length];
		int count = 0;
		for (int i = 0; i < OFFSETS.length; i++) {
			int rr = r + OFFSETS[i][0];
			int cc = c + OFFSETS[i][1];
			if (coords.isValidOneDimensionalCoordinate(rr)
					&& (coords.isValidOneDimensionalCoordinate(cc))) {
				result[count] = coords.at(rr, cc);
				count++;
			}
		}
		// Create a small array and copy the elements into it
		return java.util.Arrays.copyOf(result, count);
	}

}
