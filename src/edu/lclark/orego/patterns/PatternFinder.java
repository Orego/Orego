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

	public static final long[][] POINT_HASHES = new long[4][80];

	public static final int[][] OFFSETS = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, -1 },
			{ -1, 1 }, { 1, 1 }, { 1, -1 }, { -2, 0 }, { 0, 2 }, { 2, 0 }, { 0, -2 }, { -2, -1 },
			{ -2, 1 }, { -1, 2 }, { 1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { -1, -2 }, { -2, -2 },
			{ -2, 2 }, { 2, 2 }, { 2, -2 }, { -3, -3 }, { -3, -2 }, { -3, -1 }, { -3, 0 },
			{ -3, 1 }, { -3, 2 }, { -3, 3 }, { -2, 3 }, { -1, 3 }, { 0, 3 }, { 1, 3 }, { 2, 3 },
			{ 3, 3 }, { 3, 2 }, { 3, 1 }, { 3, 0 }, { 3, -1 }, { 3, -2 }, { 3, -3 }, { 2, -3 },
			{ 1, -3 }, { 0, -3 }, { -1, -3 }, { -2, -3 }, { -4, -4 }, { -4, -3 }, { -4, -2 },
			{ -4, -1 }, { -4, 0 }, { -4, 1 }, { -4, 2 }, { -4, 3 }, { -4, 4 }, { -3, 4 },
			{ -2, 4 }, { -1, 4 }, { 0, 4 }, { 1, 4 }, { 2, 4 }, { 3, 4 }, { 4, 4 }, { 4, 3 },
			{ 4, 2 }, { 4, 1 }, { 4, 0 }, { 4, -1 }, { 4, -2 }, { 4, -3 }, { 4, -4 }, { 3, -4 },
			{ 2, -4 }, { 1, -4 }, { 0, -4 }, { -1, -4 }, { -2, -4 }, { -3, -4 } };

	static {
		MersenneTwisterFast random = new MersenneTwisterFast(0L);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 80; j++) {
				POINT_HASHES[i][j] = random.nextLong();
			}
		}
	}

	public static void main(String[] args) {
		HashMap<String, Float> map = new HashMap<>();
		HashMap<String, Long> hashMap = new HashMap<>();
		Board board = new Board(19);
		ShapeTable table = new ShapeTable("patterns" + File.separator
				+ "patterns3x3-SHAPE-sf90.data");
		int centerColumn = 11;
		int centerRow = 11;
		int patternRadius = 1;
		int minStoneCount = 1;
		int maxStoneCount = 5;
		ArrayList<Short> stones = new ArrayList<>();
		generatePatternMap(board, map, hashMap, table, stones, minStoneCount, maxStoneCount, centerRow,
				centerColumn,
				patternRadius);
		System.out.println(map.size());
		ArrayList<Entry<String, Float>> entries = new ArrayList<>(map.entrySet());
		Collections.sort(entries, new Comparator<Entry<String, Float>>() {
			@SuppressWarnings("boxing")
			@Override
			public int compare(Entry<String, Float> entry1, Entry<String, Float> entry2) {
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
			System.out.println(entries.get(i).getKey());
		}
		System.out.println("Top Twenty\n");
		for (int i = entries.size() - 20; i < entries.size(); i++) {
			System.out.println(entries.get(i).getValue());
			System.out.println(hashMap.get(entries.get(i).getKey()));
			System.out.println(entries.get(i).getKey());
		}
	}

	@SuppressWarnings("boxing")
	private static void generatePatternMap(Board board, HashMap<String, Float> map,
			HashMap<String, Long> hashMap, ShapeTable table, ArrayList<Short> stones,
			int minStoneCount, int maxStoneCount,
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
			long hash = getHash(board, board.getCoordinateSystem().at(centerRow, centerColumn),
					1 + (patternRadius * 2));
			String pattern = getPatternString(board, topRow, bottomRow, leftColumn, rightColumn);
			map.put(pattern, table.getWinRate(hash));
			hashMap.put(pattern, hash);
		}
		if (stones.size() < maxStoneCount) {
			for (int row = topRow; row <= bottomRow; row++) {
				for (int column = leftColumn; column <= rightColumn; column++) {
					if (column == centerColumn && row == centerRow) {
						continue;
					}
					short p = board.getCoordinateSystem().at(row, column);
					if (!stones.contains(p)) {
						stones.add(p);
						generatePatternMap(board, map, hashMap, table, stones, minStoneCount, maxStoneCount,
								centerRow,
								centerColumn, patternRadius);
						stones.remove(new Short(p));
					}
				}
			}
		}
	}

	private static String getPatternString(Board board, int topRow, int bottomRow, int leftColumn,
			int rightColumn) {
		String pattern = "";
		for (int row = topRow; row <= bottomRow; row++) {
			for (int column = leftColumn; column <= rightColumn; column++) {
				Color pointColor = board.getColorAt(board.getCoordinateSystem().at(row, column));
				pattern += pointColor.toChar();
			}
			pattern += "\n";
		}
		return pattern;
	}

	// TODO Comments? What is patternSize?
	public static long getHash(Board board, short p, int patternSize) {
		CoordinateSystem coords = board.getCoordinateSystem();
		long result = 0L;
		int row = coords.row(p);
		int column = coords.column(p);
		for (int i = 0; i < patternSize; i++) {
			int newRow = row + OFFSETS[i][0];
			int newColumn = column + OFFSETS[i][1];
			if (coords.isValidOneDimensionalCoordinate(newRow)
					&& coords.isValidOneDimensionalCoordinate(newColumn)) {
				Color color = board.getColorAt(coords.at(newRow, newColumn));
				if (color != VACANT) {
					// We represent friendly as white, enemy as black
					result ^= board.getColorToPlay() == color ? POINT_HASHES[WHITE.index()][i]
							: POINT_HASHES[BLACK.index()][i];
				}
			} else {
				result ^= POINT_HASHES[OFF_BOARD.index()][i];
			}
		}
		return result;
	}

}
