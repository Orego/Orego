package edu.lclark.patterns;

import java.io.File;
import java.util.List;

import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.*;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.move.Mover.*;

public class PatternExtractor {

	private final static int PATTERN_COUNT = 65536;

	private Board board;

	private CoordinateSystem coords;

	private float[] winRates;

	private int[] runs;

	private int[] wins;

	private MersenneTwisterFast random;

	public PatternExtractor() {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		winRates = new float[PATTERN_COUNT];
		runs = new int[PATTERN_COUNT];
		wins = new int[PATTERN_COUNT];
		random = new MersenneTwisterFast();
	}
	
	float[] getWinRates(){
		return winRates;
	}

	private void analyzeMove(short move) {
		updateTables(true, move);
		updateTables(false, selectRandomMove(move));
	}

	/**
	 * Updates tables by generating hashes for patterns based on the color to
	 * play. Color reversed patterns are stored in the same slot.
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
