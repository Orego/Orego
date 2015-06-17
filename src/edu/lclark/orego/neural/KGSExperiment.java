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
		// Get games from files
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
		Network net = new Network(area * 4, area, area);
		float[][] training = new float[numberOfTrainingPoints][];
		// TODO It would be better to pick a different bad move each time we train on a point
		int[][] trainingCorrect = new int[numberOfTrainingPoints][2];
		int pointNumber = 0;
		// Input data
		for (final List<Short> game : games) {
			board.clear();
			for (final short good : game) {
				training[pointNumber] = extractor.toInputVector();
				short bad = selectRandomMove(good);
				trainingCorrect[pointNumber] = new int[] {
						// TODO row and column extraction could happen inside index
						index(coords.row(good), coords.column(good)),
						index(coords.row(bad), coords.column(bad)) };
				board.play(good);
				pointNumber++;
			}
		}
		// Train the network
		for (int i = 0; i < 100000; i++) {
			if (i % 100 == 0) {
				System.out.println(i);
			}
			// TODO Should this be random or should we just pass through all the
			// games?
			int k = (int) (numberOfTrainingPoints * Math.random());
			net.train(training[k], trainingCorrect[k][0], trainingCorrect[k][1]);
		}
		// Make test data
		board.clear();
		net.update(extractor.toInputVector());
		// Print test data
		float[] out = net.getOutputActivations();
		for (int j = 0; j < 19 * 19; j++) {
			System.out.printf("%1.4f ", out[j + 1]);
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
