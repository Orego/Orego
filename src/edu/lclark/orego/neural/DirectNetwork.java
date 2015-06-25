package edu.lclark.orego.neural;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;
import static edu.lclark.orego.move.Mover.PRIMES;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

	private Board board;

	private CoordinateSystem coords;

	private Extractor extractor;

	private int maxMove = 1;

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

	public float[] getOutputActivations() {
		return net.getOutputActivations();
	}
	
	/** Returns the network's output for point p. */
	public float getOutputActivation(short p) {
		return net.getOutputActivations()[netIndex(p) + 1];
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


	/**Trains the network given a specified number of epochs*/
	public void train(int epochs) {
		final SgfParser parser = new SgfParser(coords, true);
		for (int i = 0; i < epochs; i++) {
			trainFiles(new File(SYSTEM.getExpertGamesDirectory()), parser);
		}
		
	}

	/**Given file, trains network once on every move in every game*/
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
				board.clear();
				for (final short good : game) {
					net.train(extractor.toInputVector(),
							netIndex(good),
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
		final File directory = new File(OREGO_ROOT + "networks" + File.separator
				+ "neuralbook.data");
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(directory))) {
			out.writeObject(this);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
