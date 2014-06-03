package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.NonStoneColor;

/** True if p is "near" another stone, i.e., within Manhattan distance 4. */
public final class NearAnotherStone implements Feature {

	private static final short[][][] neighbors = new short[20][][];

	private final CoordinateSystem coords;

	private final Board board;

	public NearAnotherStone(Board board) {
		this.board = board;
		coords = board.getCoordinateSystem();
		if (neighbors[coords.getWidth()] == null) {
			short[] pointsOnBoard = coords.getAllPointsOnBoard();
			neighbors[coords.getWidth()] = new short[coords.getFirstPointBeyondBoard()][];
			for (short p : pointsOnBoard) {
				neighbors[coords.getWidth()][p] = findNeighborhood(
							p, new short[][] { { 0, -1 }, { 0, 1 },
								{ -1, 0 }, { 1, 0 }, { -1, -1 }, { -1, 1 },
								{ 1, -1 }, { 1, 1 }, { -2, 0 }, { 2, 0 },
								{ 0, -2 }, { 0, 2 }, { -2, -1 }, { -2, 1 },
								{ -1, -2 }, { -1, 2 }, { 2, 1 }, { 2, -1 },
								{ 1, -2 }, { 1, 2 }, { 2, 2 }, { 2, -2 },
								{ -2, 2 }, { -2, -2 }, { 3, 0 }, { -3, 0 },
								{ 0, -3 }, { 0, 3 }, { 3, 1 }, { 3, -1 },
								{ -1, -3 }, { 1, -3 }, { -3, -1 }, { -3, 1 },
								{ -1, 3 }, { 1, 3 } });
			}
		}
	}

	@Override
	public boolean at(short p) {
		for (int i = 0; i < neighbors[coords.getWidth()][p].length; i++) {
			if (board.getColorAt(neighbors[coords.getWidth()][p][i]) != NonStoneColor.VACANT) {
				return true;
			}
		}
		return false;
	}

	private short[] findNeighborhood(short p, short[][] offsets) {
		int r = coords.row(p), c = coords.column(p);
		short large[] = new short[offsets.length];
		int count = 0;
		for (int i = 0; i < offsets.length; i++) {
			if (coords.isValidOneDimensionalCoordinate(r + offsets[i][0])
					&& (coords.isValidOneDimensionalCoordinate(c
							+ offsets[i][1]))) {
				large[i] = coords.at(r + offsets[i][0], c + offsets[i][1]);
				count++;
			}
		}
		// Create a small array and copy the elements into it
		short result[] = new short[count];
		int v = 0;
		for (int i = 0; i < offsets.length; i++)
			if (large[i] > 0) {
				result[v] = large[i];
				v++;
			}
		return result;
	}

}
