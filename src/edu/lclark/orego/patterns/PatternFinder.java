package edu.lclark.orego.patterns;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import static edu.lclark.orego.core.NonStoneColor.*;

public final class PatternFinder {

	/** The first index is the condition of the point. The second is an index into offsets. */
	public static final long[][] POINT_HASHES = new long[10][39 * 39 - 1];

	private static int[] patternSizes;

	/** Returns the array of pattern sizes. For testing. */
	static int[] getPatternSizes() {
		return patternSizes;
	}

	private static short[][] offsets;

	/** Returns the array of offsets. For testing. */
	static short[][] getOffsets() {
		return offsets;
	}

	static {
		// Find all possible offsets
		ArrayList<short[]> unsortedOffsets = new ArrayList<>();
		for (short r = -19; r <= 19; r++) {
			for (short c = -19; c <= 19; c++) {
				if (r != 0 || c != 0) {
					unsortedOffsets.add(new short[] {r, c});
				}
			}
		}
		// Create concentric pattern sizes
		patternSizes = new int[180];
		offsets = new short[1520][];
		int sizeIndex = 1;
		int numberSorted = 0;
		int oldNumberSorted = numberSorted;
		for (double radius = 1.0; !unsortedOffsets.isEmpty(); radius += 0.01) {
			Iterator<short[]> iter = unsortedOffsets.iterator();
			while (iter.hasNext()) {
				short[] offset = iter.next();
				if (distanceTo(offset) <= radius) {
					offsets[numberSorted] = offset;
					iter.remove();
					numberSorted++;
				}
			}
			if (numberSorted > oldNumberSorted && (numberSorted - oldNumberSorted) % 4 == 0) {
				// The second condition above verifies that rounding error
				// didn't create an asymmetric pattern
				patternSizes[sizeIndex] = numberSorted;
				sizeIndex++;
				oldNumberSorted = numberSorted;
			}
		}
		// Create the Zobrist hashes themselves
		MersenneTwisterFast random = new MersenneTwisterFast(0L);
		for (int i = 0; i < POINT_HASHES.length; i++) {
			for (int j = 0; j < POINT_HASHES[i].length; j++) {
				POINT_HASHES[i][j] = random.nextLong();
			}
		}
	}

	/**
	 * Returns the Euclidean distance from the origin to offset.
	 */
	static double distanceTo(short[] offset) {
		double x = offset[0];
		double y = offset[1];
		return Math.sqrt(x * x + y * y);
	}

	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		HashMap<String, Float> map = new HashMap<>();
		HashMap<String, Long> hashMap = new HashMap<>();
		Board board = new Board(19);
		ShapeTable table = new ShapeTable("patterns" + File.separator
				+ "patterns9x9-SHAPE-sf99.data", 0.99f);
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
					(1 + (patternRadius * 2)) * (1 + (patternRadius * 2)) - 1,
					CoordinateSystem.NO_POINT);
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
	public static long getHash(Board board, short p, int minStones,
			short lastMove) {
		CoordinateSystem coords = board.getCoordinateSystem();
		long result = 0L;
		int row = coords.row(p);
		int column = coords.column(p);
		int stoneCounter = 0;
		for (int i = 0; i < 4; i++) {
			for (int j = patternSizes[i]; j < patternSizes[i + 1]; j++) {
				int newRow = row + offsets[j][0];
				int newColumn = column + offsets[j][1];
				if (coords.isValidOneDimensionalCoordinate(newRow)
						&& coords.isValidOneDimensionalCoordinate(newColumn)) {
					short point = coords.at(newRow, newColumn);
					Color color = board.getColorAt(point);
					if (color == board.getColorToPlay()) {
						if (board.getLiberties(point).size() == 1) {
							result ^= POINT_HASHES[0][j];
						} else if (board.getLiberties(point).size() == 2) {
							result ^= POINT_HASHES[1][j];
						} else {
							result ^= POINT_HASHES[2][j];
						}
						stoneCounter++;
					} else if (color == board.getColorToPlay().opposite()) {
						int lastMoveOffset = lastMove == point ? 3 : 0;
						if (board.getLiberties(point).size() == 1) {
							result ^= POINT_HASHES[3 + lastMoveOffset][j];
						} else if (board.getLiberties(point).size() == 2) {
							result ^= POINT_HASHES[4 + lastMoveOffset][j];
						} else {
							result ^= POINT_HASHES[5 + lastMoveOffset][j];
						}
						stoneCounter++;
					}
				} else {
					result ^= POINT_HASHES[9][j];
				}
			}
			if (stoneCounter >= minStones) {
				return result;
			}
		}
		return result;
	}

}
