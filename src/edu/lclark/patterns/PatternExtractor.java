package edu.lclark.patterns;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;

import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.*;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.move.Mover.*;

public final class PatternExtractor {

	private final static int PATTERN_COUNT = 65536;

	private final Board board;

	private final CoordinateSystem coords;

	private final int[] runs;

	private final int[] wins;

	private final MersenneTwisterFast random;

	private final ArrayList<Pattern> list;

	private final SgfParser parser;

	private static final String OUTPUT_FILE = "PatternData/Pro3x3PatternData.txt";

	/**
	 * Analyzes 3x3 patterns in SGF files and stores a win rate for each in an
	 * array. Does not support varying board sizes because SgfParser does not
	 * currently support board sizes other than 19.
	 */
	public PatternExtractor() {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		runs = new int[PATTERN_COUNT];
		wins = new int[PATTERN_COUNT];
		random = new MersenneTwisterFast();
		list = new ArrayList<>();
		parser = new SgfParser(coords);
	}

	/**
	 * Updates the tables with a win for the pattern around the move and a loss
	 * for some other random move on the board.
	 */
	private void analyzeMove(short move) {
		updateTables(true, move);
		updateTables(false, selectRandomMove(move));
	}

	/**
	 * Updates tables by generating hashes for patterns based on the color to
	 * play. Color reversed patterns are stored in the same slot. If the move is
	 * considered good, winner is true and the wins for the slot are updated. If
	 * not, only runs is updated.
	 */
	private void updateTables(boolean winner, short move) {
		short[] neighbors = coords.getNeighbors(move);
		int hash = 0;
		for (int i = 0; i < neighbors.length; i++) {
			Color color = board.getColorAt(neighbors[i]);
			if (color == board.getColorToPlay()) {
				// Friendly stone at this neighbor
				hash |= 1 << (i * 2);
			} else if (color != board.getColorToPlay().opposite()) {
				// neighbor is vacant or off board
				hash |= color.index() << (i * 2);
			} // else do nothing, no need to OR 0 with 0
		}
		if (winner) {
			wins[hash] += 1;
		}
		runs[hash] += 1;
	}

	/**
	 * Selects a random move. Used to help balance the table with losses for
	 * points not played in the game being analyzed.
	 */
	private short selectRandomMove(short move) {
		ShortSet vacantPoints = board.getVacantPoints();
		short start = (short) (random.nextInt(vacantPoints.size()));
		short i = start;
		short skip = PRIMES[random.nextInt(PRIMES.length)];
		do {
			short p = vacantPoints.get(i);
			if ((board.getColorAt(p) == VACANT)) {
				if (board.isLegal(p) && p != move) {
					return p;
				}
			}
			// Advancing by a random prime skips through the array
			// in a manner analogous to double hashing.
			i = (short) ((i + skip) % vacantPoints.size());
		} while (i != start);
		return PASS;
	}

	/** Analyzes all the games in one SGF file. */
	private void analyzeGames(List<List<Short>> games) {
		for (List<Short> game : games) {
			for (Short move : game) {
				analyzeMove(move);
				board.play(move);
			}
			board.clear();
		}

	}

	private List<Pattern> getPatterns() {
		return list;
	}

	private void analyzeFiles(File file) {
		File[] allFiles = file.listFiles();
		if (allFiles != null) {
			for (File tempFile : allFiles) {
				analyzeFiles(tempFile);
			}
		} else {
			if (file.getPath().endsWith(".sgf")) {
				List<List<Short>> games = parser.parseGamesFromFile(file, 500);
				analyzeGames(games);
			}
		}
	}

	public static void main(String[] args) {
		PatternExtractor extractor = new PatternExtractor();
		extractor
				.analyzeFiles(new File(
						"/Network/Servers/maccsserver.lclark.edu/Users/slevenick/Desktop/patternfiles/kgs-19-2006"));

		for (int i = 0; i < PATTERN_COUNT; i++) {
			if (extractor.runs[i] != 0) {
				extractor.list.add(new Pattern(i, (float) extractor.wins[i]
						/ (float) extractor.runs[i]));
			} else {
				// Add values of .5 to unfilled points?
			}
		}
		Collections.sort(extractor.list);
		try (PrintWriter writer = new PrintWriter(OUTPUT_FILE, "UTF-8")) {
			for (Pattern pattern : extractor.getPatterns()) {
				writer.println(pattern.getWinRate() +"," +  pattern.getHash());
			}
		} catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	class ValueComparator implements Comparator<Integer> {
		private Map<Integer, Float> map;

		ValueComparator(HashMap<Integer, Float> map) {
			this.map = map;
		}

		@Override
		public int compare(Integer key1, Integer key2) {
			if (map.get(key1) > map.get(key2)) {
				return 1;
			} else {
				return -1;
			}
		}

	}
}
