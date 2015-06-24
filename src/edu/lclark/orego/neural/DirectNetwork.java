package edu.lclark.orego.neural;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;
import static edu.lclark.orego.move.Mover.PRIMES;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;

@SuppressWarnings("serial")
/** A network that responds directly to its board. */
public class DirectNetwork implements Serializable {

	private Board board;

	private CoordinateSystem coords;

	private Extractor extractor;

	private int maxMove = 2;

	private Network net;

	private final MersenneTwisterFast random;

	private boolean verbose = true;

	public DirectNetwork(Board board, HistoryObserver historyObserver) {
		this.board = board;
		coords = board.getCoordinateSystem();
		this.extractor = new Extractor(board, historyObserver);
		final int area = coords.getArea();
		net = new Network(area * 4, area * 5, area);
		random = new MersenneTwisterFast();
	}

	/** Returns the network's output for point p. */
	public float getOutputActivation(short p) {
		return net.getOutputActivations()[netIndex(coords.row(p),
				coords.column(p)) + 1];
	}

	/**
	 * Returns an index in the network (which does not use off-board padding
	 * like Board) for the point at row, col.
	 */
	private int netIndex(int row, int col) {
		return coords.getWidth() * row + col;
	}

	/**
	 * Return a list of games in file; each game is represented as a list of
	 * moves (shorts). If file is a directory, recursively analyze everything in
	 * it.
	 */
	List<List<Short>> processFiles(File file, SgfParser parser) {
		final List<List<Short>> games = new ArrayList<>();
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				games.addAll(processFiles(f, parser));
			}
		} else if (file.getPath().endsWith(".sgf")) {
			// System.out.println("Reading file " + file.getName());
			games.addAll(parser.parseGamesFromFile(file, 2));
		}
		return games;
	}

	/**
	 * Copied from a different part of Orego that selects a move other then the
	 * one specified.
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

	public void train() {
		// Get games from files
		final SgfParser parser = new SgfParser(coords, true);
		final List<List<Short>> games = processFiles(
				new File(SYSTEM.getExpertGamesDirectory()), parser);
		// Count the number of points to train on
		int numberOfTrainingPoints = 0;
		for (final List<Short> game : games) {
			numberOfTrainingPoints += game.size();
		}
		// Declare stuff
		float[][] training = new float[numberOfTrainingPoints][];
		// TODO It would be better to pick a different bad move each time we
		// train on a point
		int[][] trainingCorrect = new int[numberOfTrainingPoints][2];
		int pointNumber = 0;
		// Input data
		for (final List<Short> game : games) {
			board.clear();
			for (final short good : game) {
				training[pointNumber] = extractor.toInputVector();
				short bad = selectRandomMove(good);
				trainingCorrect[pointNumber] = new int[] {
						// TODO row and column extraction could happen inside
						// index
						netIndex(coords.row(good), coords.column(good)),
						netIndex(coords.row(bad), coords.column(bad)) };
				board.play(good);
				pointNumber++;
			}
		}
		// Train the network
		for (int i = 0; i < 1000; i++) {
			// TODO Should this be random or should we just pass through all the
			// games?
			int k = (int) (numberOfTrainingPoints * Math.random());
			net.train(training[k], trainingCorrect[k][0], trainingCorrect[k][1]);
		}
	}

	public void train(int epochs) {
		for (int i = 0; i < epochs; i++) {
			trainFiles(new File(SYSTEM.getExpertGamesDirectory()));
		}
	}

	void trainFiles(File file) {
		if (file.isDirectory()) {
			if (verbose) {
				System.out.println("Analyzing files in " + file.getName());
			}
			for (final File tempFile : file.listFiles()) {
				trainFiles(tempFile);
			}
		} else if (file.getPath().endsWith(".sgf")) {
			final SgfParser parser = new SgfParser(coords, true);
			final List<List<Short>> games = parser.parseGamesFromFile(file,
					maxMove);
			for (final List<Short> game : games) {
				board.clear();
				for (final short good : game) {
					float[] training = new float[extractor.toInputVector().length];
					training = extractor.toInputVector();
					short bad = selectRandomMove(good);
					net.train(training,
							netIndex(coords.row(good), coords.column(good)),
							netIndex(coords.row(bad), coords.column(bad)));
					board.play(good);
				}
			}
		}
	}

	public void update() {
		net.update(extractor.toInputVector());
	}

}
