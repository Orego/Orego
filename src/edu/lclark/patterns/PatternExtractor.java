package edu.lclark.patterns;

import java.io.File;
import java.util.List;

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

	private final float[] winRates;

	private final int[] runs;

	private final int[] wins;

	private final MersenneTwisterFast random;

	/**
	 * Analyzes 3x3 patterns in SGF files and stores a win rate for each in an
	 * array. Does not support varying board sizes because SgfParser does not
	 * currently support board sizes other than 19.
	 */
	public PatternExtractor() {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		winRates = new float[PATTERN_COUNT];
		runs = new int[PATTERN_COUNT];
		wins = new int[PATTERN_COUNT];
		random = new MersenneTwisterFast();
	}

	float[] getWinRates() {
		return winRates;
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
	private float[] analyzeGames(List<List<Short>> games) {
		for (List<Short> game : games) {
			for (Short move : game) {
				analyzeMove(move);
				board.play(move);
			}
			board.clear();
		}
		for (int i = 0; i < PATTERN_COUNT; i++) {
			if (runs[i] != 0) {
				winRates[i] = (float) wins[i] / (float) runs[i];
			} else {
				winRates[i] = 0.5f;
			}
		}
		return winRates;
	}

	public static void main(String[] args) {
		SgfParser parser = new SgfParser(CoordinateSystem.forWidth(19));
		PatternExtractor extractor = new PatternExtractor();
		File folder = new File("SgfTestFiles/19");
		File[] allFiles = folder.listFiles();
		for (File file : allFiles) {
			if (file.getPath().endsWith(".sgf")) {
				List<List<Short>> games = parser.parseGamesFromFile(new File(
						"SgfTestFiles/19/TwoGames.sgf"), 500);
				extractor.analyzeGames(games);
			}
		}
		float[] winRates = extractor.getWinRates();
		for (int i = 0; i < 65000; i++) {
			if (winRates[i] != 0.5) {
				System.out.println(winRates[i]);
			}
		}
	}
}
