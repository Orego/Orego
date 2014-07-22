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

public final class PatternFinder {

	public static final int[][] POINT_HASHES = new int[4][80];

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
				POINT_HASHES[i][j] = random.nextInt();
			}
		}
	}

	public static void main(String[] args) {
		HashMap<String, Float> map = new HashMap<>();
		Board board = new Board(19);
		ShapeTable table = new ShapeTable("patterns" + File.separator + "patterns3x3-SHAPE-sf90.data");
		int centerColumn = 4;
		int centerRow = 4;
		int patternRadius = 1;
		int stoneCount = 5;
		ArrayList<Short> stones = new ArrayList<>();
		generatePatternMap(board, map, table, stones, stoneCount, centerRow, centerColumn,
				patternRadius);
		System.out.println(map.size());
		ArrayList<Entry<String, Float>> entries = new ArrayList<>();
		entries.addAll(map.entrySet());
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
			System.out.println(entries.get(i).getKey());
		}
		System.out.println("Top Twenty\n");
		for (int i = entries.size() - 20; i < entries.size(); i++) {
			System.out.println(entries.get(i).getValue());
			System.out.println(entries.get(i).getKey());
		}
	}

	@SuppressWarnings("boxing")
	private static void generatePatternMap(Board board, HashMap<String, Float> map,
			ShapeTable table, ArrayList<Short> stones, int stoneCount,
			int centerRow, int centerColumn, int patternRadius) {
		int topRow = centerRow - patternRadius;
		int bottomRow = centerRow + patternRadius;
		int leftColumn = centerColumn - patternRadius;
		int rightColumn = centerColumn + patternRadius;
		if (stones.size() == stoneCount) {
			board.clear();
			for (short p : stones) {
				board.play(p);
			}
			long hash = getHash(board, board.getCoordinateSystem().at(centerRow, centerColumn),
					1 + (patternRadius * 2));
			String pattern = getPatternString(board, topRow, bottomRow, leftColumn, rightColumn);
			map.put(pattern, table.getWinRate(hash));
		} else {
			for (int row = topRow; row <= bottomRow; row++) {
				for (int column = leftColumn; column <= rightColumn; column++) {
					if (column == centerColumn && row == centerRow) {
						continue;
					}
					short p = board.getCoordinateSystem().at(row, column);
					if (!stones.contains(p)) {
						stones.add(p);
						generatePatternMap(board, map, table, stones, stoneCount, centerRow,
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
			} else {
				Color color = board.getColorAt(coords.at(newRow, newColumn));
				if (color == VACANT) {
					result ^= POINT_HASHES[2][i];
				} else {
					result ^= board.getColorToPlay() == color ? POINT_HASHES[1][i]
							: POINT_HASHES[0][i];
				}
			}
		}
		return result;
	}

}
