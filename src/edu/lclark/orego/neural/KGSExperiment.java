package edu.lclark.orego.neural;

import static edu.lclark.orego.experiment.SystemConfiguration.*;
import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.move.Mover.PRIMES;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;

public class KGSExperiment {

	public static void main(String[] args) {
		new KGSExperiment().run();
	}

	private Board board;

	private CoordinateSystem coords;

	private SgfParser parser;

	private Extractor extractor;
	
	private final MersenneTwisterFast random;

	public KGSExperiment() {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		parser = new SgfParser(coords, true);
		random = new MersenneTwisterFast();
		extractor = new Extractor(board);
	}

	// Converts coordinates into a format that works with our array
	private int index(int row, int col) {
		return 19 * row + col;
	}

	private Board obviousTest() {
		Board test = new Board(19);
		String[] blank = { "...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", };
		test.setUpProblem(blank, WHITE);
		return test;
	}

	/**
	 * Return a list of games in file; each game is represented as a list of
	 * moves (shorts). If file is a directory, recursively analyze everything in
	 * it.
	 */
	List<List<Short>> processFiles(File file) {
		final List<List<Short>> games = new ArrayList<>();
		if (file.isDirectory()) {
			System.out.println("Analyzing files in " + file.getName());
			for (final File tempFile : file.listFiles()) {
				games.addAll(processFiles(tempFile));
			}
		} else if (file.getPath().endsWith(".sgf")) {
			// System.out.println("Reading file " + file.getName());
			games.addAll(parser.parseGamesFromFile(file, 1));
		}
		return games;
	}

	private void run() {
		// Get games from file
		final List<List<Short>> games = processFiles(new File(
				SYSTEM.getExpertGamesDirectory()));
		// Count the number of points to train on
		int numberOfTrainingPoints = 0;
		for (final List<Short> game : games) {
			numberOfTrainingPoints += game.size();
		}
		System.out.println(numberOfTrainingPoints);
		// Declare stuff
		final int area = coords.getArea();
		Network net = new Network(area * 4, area, 1, area);
		double[][] training = new double[numberOfTrainingPoints][area * 4];
		int[][] trainingCorrect = new int[numberOfTrainingPoints][2];
		int gameNumber = 0;
		// Input data
		for (final List<Short> game : games) {
			int k = 0;
			board.clear();
			for (final short move : game) {
				int p = 0; // place in training array
				for (int row = 0; row < 19; row++) {
					for (int col = 0; col < 19; col++) {
						training[k][p] = extractor.isBlack(row, col);
						training[k][p + 19 * 19] = extractor.isWhite(row, col);
						training[k][p + 19 * 19 * 2] = extractor
								.isUltimateMove(row, col);
						training[k][p + 19 * 19 * 3] = extractor
								.isPenultimateMove(row, col);
						p++;
					}
				}
				short rand = selectRandomMove(move);
				trainingCorrect[k] = new int[] {
						index(coords.row(move), coords.column(move)),
						index(coords.row(rand), coords.column(rand)) };
				k++;
				board.play(move);
			}
			gameNumber++;
		}

		// Train the network
		for (int i = 0; i < 100000; i++) {
			if (i % 100 == 0) {
				System.out.println(i);
			}
			// TODO Should this be random or should we just pass through all the
			// games?
			int k = (int) (numberOfTrainingPoints * Math.random());
			net.train(1, (int) trainingCorrect[k][0], training[k]);
			net.train(0, (int) trainingCorrect[k][1], training[k]);
		}

		// Make test data
		board.clear();
		double[] testObvious = new double[19 * 19 * 4];
		Extractor extractor = new Extractor(board);
		int p = 0;
		for (int row = 0; row < 19; row++) {
			for (int col = 0; col < 19; col++) {
				testObvious[p] = extractor.isBlack(row, col);
				testObvious[p + 19 * 19] = extractor.isWhite(row, col);
				testObvious[p + 19 * 19 * 2] = extractor.isUltimateMove(row,
						col);
				testObvious[p + 19 * 19 * 3] = extractor.isPenultimateMove(row,
						col);
				p++;
			}
		}
		// Print test data
		for (int j = 0; j < 19 * 19; j++) {
			System.out.printf("%1.4f ", net.test(testObvious)[j]);
			if (j % 19 == 18) {
				System.out.println();
			}
		}
		System.out.println();

	}

	// Copy and pasted from a different part of Orego that selects
	// a different move other then the one inputed
	short selectRandomMove(short move) {
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
}
