package edu.lclark.orego.neural;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;
import static edu.lclark.orego.move.Mover.PRIMES;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

	public static void main(String[] args) {
		Board board = new Board(19);
		DirectNetwork network = new DirectNetwork(board, new HistoryObserver(
				board));
		network.train(3);
		network.writeBook();
	}

	public static DirectNetwork readFromDisk(Board board,
			HistoryObserver historyObserver) {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				new File(OREGO_ROOT + "networks" + File.separator
						+ "network.data")))) {
			DirectNetwork net = (DirectNetwork) in.readObject();
			net.board = board;
			net.extractor = new Extractor(board, historyObserver);
			return net;
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	// TODO Do we really need this?
	private Board board;

	private CoordinateSystem coords;

	private double correctTrainings;

	private double correctValidations;

	private Extractor extractor;

	private int gameCount;

	private int maxMove = 3;

	private Network net;

	private final MersenneTwisterFast random;
	
	private double trainTested;
	
	private double validationsTested;
	
	private boolean verbose = false;

	private DirectNetwork(Board board, HistoryObserver historyObserver) {
		gameCount = 0;
		this.board = board;
		coords = board.getCoordinateSystem();
		this.extractor = new Extractor(board, historyObserver);
		final int area = coords.getArea();
		net = new Network(area * 4, area * 5, area);
		random = new MersenneTwisterFast();
	}

	/** Returns the network's output for point p. */
	public float getOutputActivation(short p) {
		return net.getOutputActivations()[netIndex(p) + 1];
	}

	public float[] getOutputActivations() {
		return net.getOutputActivations();
	}

	/**
	 * Returns an index in the network (which does not use off-board padding
	 * like Board) for p.
	 */
	private int netIndex(short p) {
		return coords.getWidth() * coords.row(p) + coords.column(p);
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

	void test(File file, SgfParser parser) {
		if (file.isDirectory()) {
			if (verbose) {
				System.out.println("Analyzing files in " + file.getName());
			}
			for (final File tempFile : file.listFiles()) {
				test(tempFile, parser);
			}
		} else if (file.getPath().endsWith(".sgf")) {
			final List<List<Short>> games = parser.parseGamesFromFile(file,
					maxMove);
			for (final List<Short> game : games) {
				gameCount++;
				if (gameCount % 10 == 0) {
					board.clear();
					for (final short correct : game) {
						validationsTested++;
						if (netIndex(correct) == net.maxOutput()) {
							correctValidations++;
						}
						board.play(correct);
					}
					break;
				}
				board.clear();
				for (final short correct : game) {
					trainTested++;
					if (netIndex(correct) == net.maxOutput()) {
						correctTrainings++;
					}
					board.play(correct);
				}
			}
		}
	}

	void test(int epoch, File file, SgfParser parser) {
		validationsTested = 0;
		trainTested = 0;
		correctValidations = 0;
		correctTrainings = 0;
		test(file, parser);
		System.out.println((epoch + 1) + "\t"
				+ (correctValidations / validationsTested) + " " + validationsTested + "\t"
				+ (correctTrainings / trainTested)+ " " + trainTested);
	}

	/** Trains the network given a specified number of epochs */
	public void train(int epochs) {
		final SgfParser parser = new SgfParser(coords, true);
		for (int i = 0; i < epochs; i++) {
			File file = new File(SYSTEM.getExpertGamesDirectory());
			trainFiles(file, parser);
			gameCount = 0;
			test(i, file, parser);
			gameCount = 0;
		}
	}

	/** Given file, trains network once on every move in every game recursively */
	void trainFiles(File file, SgfParser parser) {
		if (file.isDirectory()) {
			if (verbose) {
				System.out.println("Analyzing files in " + file.getName());
			}
			for (final File tempFile : file.listFiles()) {
				trainFiles(tempFile, parser);
			}
		} else if (file.getPath().endsWith(".sgf")) {
			final List<List<Short>> games = parser.parseGamesFromFile(file,
					maxMove);
			for (final List<Short> game : games) {
				gameCount++;
				if (gameCount % 10 == 0) {
					break;
				}
				board.clear();
				for (final short good : game) {
					net.train(extractor.toInputVector(), netIndex(good),
							netIndex(selectRandomMove(good)));
					board.play(good);
				}
			}
		}
	}

	public void update() {
		net.update(extractor.toInputVector());
	}

	/** Writes the book to a file. */
	public void writeBook() {
		final File directory = new File(OREGO_ROOT + "networks"
				+ File.separator + "network.data");
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(directory))) {
			out.writeObject(this);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
