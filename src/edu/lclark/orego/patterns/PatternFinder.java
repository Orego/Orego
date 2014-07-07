package edu.lclark.orego.patterns;

import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.NonStoneColor.*;

public final class PatternFinder {

	public static final int[] PATTERN_SIZES = { 3, 7, 11, 19, 23 };

	public static final int[][] POINT_HASHES = new int[4][24];

	public static final int[][] OFFSETS = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, -1 },
			{ -1, 1 }, { 1, 1 }, { 1, -1 }, { -2, 0 }, { 0, 2 }, { 2, 0 }, { 0, -2 }, { -2, -1 },
			{ -2, 1 }, { -1, 2 }, { 1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { -1, -2 }, { -2, -2 },
			{ -2, 2 }, { 2, 2 }, { 2, -2 } };

	static {
		MersenneTwisterFast random = new MersenneTwisterFast(0L);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 24; j++) {
				POINT_HASHES[i][j] = random.nextInt();
			}
		}
	}

	public static long getHash(Board board, short p, int patternSize) {
		CoordinateSystem coords = board.getCoordinateSystem();
		long result = 0L;
		int row = coords.row(p);
		int column = coords.column(p);
		for (int i = 0; i < patternSize; i++) {
			int newRow = row + OFFSETS[i][0];
			int newColumn = column + OFFSETS[i][1];
			if ((newRow < 0 || newRow >= coords.getWidth())
					|| (newColumn < 0 || newColumn >= coords.getWidth())) {
				result ^= POINT_HASHES[3][i];
			}else{
				Color color = board.getColorAt(coords.at(newRow, newColumn));
				if(color == VACANT){
					result ^= POINT_HASHES[2][i];
				}else{
					result ^= board.getColorToPlay() == color ? POINT_HASHES[1][i] : POINT_HASHES[0][i];
				}
			}
		}
		return result;
	}

}
