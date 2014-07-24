package edu.lclark.orego.patterns;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.StoneColor.*;

public final class PatternFinder {

	public static final long[][] POINT_HASHES = new long[5][120];

	public static final int[] PATTERN_SIZES = { 0, 8, 24, 48, 80, 120 };

	public static final int[][] OFFSETS = { { -1, 0 }, { 0, 1 }, { 1, 0 },
			{ 0, -1 }, { -1, -1 }, { -1, 1 }, { 1, 1 }, { 1, -1 }, { -2, 0 },
			{ 0, 2 }, { 2, 0 }, { 0, -2 }, { -2, -1 }, { -2, 1 }, { -1, 2 },
			{ 1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { -1, -2 }, { -2, -2 },
			{ -2, 2 }, { 2, 2 }, { 2, -2 }, { -3, -3 }, { -3, -2 }, { -3, -1 },
			{ -3, 0 }, { -3, 1 }, { -3, 2 }, { -3, 3 }, { -2, 3 }, { -1, 3 },
			{ 0, 3 }, { 1, 3 }, { 2, 3 }, { 3, 3 }, { 3, 2 }, { 3, 1 },
			{ 3, 0 }, { 3, -1 }, { 3, -2 }, { 3, -3 }, { 2, -3 }, { 1, -3 },
			{ 0, -3 }, { -1, -3 }, { -2, -3 }, { -4, -4 }, { -4, -3 },
			{ -4, -2 }, { -4, -1 }, { -4, 0 }, { -4, 1 }, { -4, 2 }, { -4, 3 },
			{ -4, 4 }, { -3, 4 }, { -2, 4 }, { -1, 4 }, { 0, 4 }, { 1, 4 },
			{ 2, 4 }, { 3, 4 }, { 4, 4 }, { 4, 3 }, { 4, 2 }, { 4, 1 },
			{ 4, 0 }, { 4, -1 }, { 4, -2 }, { 4, -3 }, { 4, -4 }, { 3, -4 },
			{ 2, -4 }, { 1, -4 }, { 0, -4 }, { -1, -4 }, { -2, -4 },
			{ -3, -4 }, { -5, -5 }, { -5, -4 }, { -5, -3 }, { -5, -2 },
			{ -5, -1 }, { -5, 0 }, { -5, 1 }, { -5, 2 }, { -5, 3 }, { -5, 4 },
			{ -5, 5 }, { -4, 5 }, { -3, 5 }, { -2, 5 }, { -1, 5 }, { 0, 5 },
			{ 1, 5 }, { 2, 5 }, { 3, 5 }, { 4, 5 }, { 5, 5 }, { 5, 4 },
			{ 5, 3 }, { 5, 2 }, { 5, 1 }, { 5, 0 }, { 5, -1 }, { 5, -2 },
			{ 5, -3 }, { 5, -4 }, { 5, -5 }, { 4, -5 }, { 3, -5 }, { 2, -5 },
			{ 1, -5 }, { 0, -5 }, { -1, -5 }, { -2, -5 }, { -3, -5 },
			{ -4, -5 } };

	static {
		MersenneTwisterFast random = new MersenneTwisterFast(0L);
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 120; j++) {
				POINT_HASHES[i][j] = random.nextLong();
			}
		}
	}

	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		HashMap<String, Float> map = new HashMap<>();
		HashMap<String, Long> hashMap = new HashMap<>();
		Board board = new Board(19);
		ShapeTable table = new ShapeTable("patterns" + File.separator
				+ "patterns9x9-SHAPE-sf99.data");
		int centerColumn = 11;
		int centerRow = 16;
		int patternRadius = 4;
		int minStoneCount = 4;
		int maxStoneCount = 4;
		ArrayList<Short> stones = new ArrayList<>();
		generatePatternMap(board, map, hashMap, table, stones, minStoneCount,
				maxStoneCount, centerRow, centerColumn, patternRadius);
		System.out.println(map.size());
		ArrayList<Entry<String, Float>> entries = new ArrayList<>(
				map.entrySet());
		Collections.sort(entries, new Comparator<Entry<String, Float>>() {
			@Override
			public int compare(Entry<String, Float> entry1,
					Entry<String, Float> entry2) {
				if (entry1.getValue() > entry2.getValue()) {
					return 1;
				} else if (entry1.getValue() < entry2.getValue()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		System.out.println("Bottom Twenty\n");
		for (int i = 0; i < 20; i++) {
			System.out.println(entries.get(i).getValue());
			System.out.println(hashMap.get(entries.get(i).getKey()));
			table.printIndividualWinRates(hashMap.get(entries.get(i).getKey()));
			System.out.println(entries.get(i).getKey());
		}
		System.out.println("Top Twenty\n");
		for (int i = entries.size() - 20; i < entries.size(); i++) {
			System.out.println(entries.get(i).getValue());
			System.out.println(hashMap.get(entries.get(i).getKey()));
			table.printIndividualWinRates(hashMap.get(entries.get(i).getKey()));
			System.out.println(entries.get(i).getKey());
		}
	}

	@SuppressWarnings("boxing")
	static void generatePatternMap(Board board, HashMap<String, Float> map,
			HashMap<String, Long> hashMap, ShapeTable table,
			ArrayList<Short> stones, int minStoneCount, int maxStoneCount,
			int centerRow, int centerColumn, int patternRadius) {
		int topRow = centerRow - patternRadius;
		int bottomRow = centerRow + patternRadius;
		int leftColumn = centerColumn - patternRadius;
		int rightColumn = centerColumn + patternRadius;
		if (stones.size() >= minStoneCount) {
			board.clear();
			for (short p : stones) {
				board.play(p);
			}
			long hash = getHash(board,
					board.getCoordinateSystem().at(centerRow, centerColumn),
					(1 + (patternRadius * 2)) * (1 + (patternRadius * 2)) - 1);
			String pattern = getPatternString(board, topRow, bottomRow,
					leftColumn, rightColumn);
			map.put(pattern, table.getWinRate(hash));
			hashMap.put(pattern, hash);
		}
		if (stones.size() < maxStoneCount) {
			for (int row = topRow; row <= bottomRow; row++) {
				for (int column = leftColumn; column <= rightColumn; column++) {
					if (column == centerColumn && row == centerRow) {
						continue;
					}
					if (!board.getCoordinateSystem()
							.isValidOneDimensionalCoordinate(row)
							|| !board.getCoordinateSystem()
									.isValidOneDimensionalCoordinate(column)) {
						continue;
					}
					short p = board.getCoordinateSystem().at(row, column);
					if (!stones.contains(p)) {
						stones.add(p);
						generatePatternMap(board, map, hashMap, table, stones,
								minStoneCount, maxStoneCount, centerRow,
								centerColumn, patternRadius);
						stones.remove(new Short(p));
					}
				}
			}
		}
	}

	private static String getPatternString(Board board, int topRow,
			int bottomRow, int leftColumn, int rightColumn) {
		String pattern = "";
		for (int row = topRow; row <= bottomRow; row++) {
			for (int column = leftColumn; column <= rightColumn; column++) {
				if (!board.getCoordinateSystem()
						.isValidOneDimensionalCoordinate(row)
						|| !board.getCoordinateSystem()
								.isValidOneDimensionalCoordinate(column)) {
					pattern += OFF_BOARD.toChar();
				} else {
					Color pointColor = board.getColorAt(board
							.getCoordinateSystem().at(row, column));
					pattern += pointColor.toChar();
				}
			}
			pattern += "\n";
		}
		return pattern;
	}

	/**
	 * Gets the Zobrist hash for a pattern around a point. patternSize is the
	 * area of the pattern - 1 (exclude the center point).
	 */
	public static long getHash(Board board, short p, int minStones) {
		CoordinateSystem coords = board.getCoordinateSystem();
		long result = 0L;
		int row = coords.row(p);
		int column = coords.column(p);
		int stoneCounter = 0;
		for (int i = 0; i < 4; i++) {
			for (int j = PATTERN_SIZES[i]; j < PATTERN_SIZES[i + 1]; j++) {
				int newRow = row + OFFSETS[j][0];
				int newColumn = column + OFFSETS[j][1];
				if (coords.isValidOneDimensionalCoordinate(newRow)
						&& coords.isValidOneDimensionalCoordinate(newColumn)) {
					short point = coords.at(newRow, newColumn);
					Color color = board.getColorAt(point);
					if (color == BLACK) {
						if (board.getLiberties(point).size() == 1) {
							result ^= POINT_HASHES[0][j];
						} else {
							result ^= POINT_HASHES[1][j];
						}
						stoneCounter++;
					} else if (color == WHITE) {
						if (board.getLiberties(point).size() == 1) {
							result ^= POINT_HASHES[2][j];
						} else {
							result ^= POINT_HASHES[3][j];
						}
						stoneCounter++;
					}
				} else {
					result ^= POINT_HASHES[4][j];
					stoneCounter++;
				}
			}
			if (stoneCounter >= minStones) {
				return result;
			}
		}
		return result;
	}

}
