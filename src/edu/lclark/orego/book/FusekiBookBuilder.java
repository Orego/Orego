package edu.lclark.orego.book;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import static java.util.Arrays.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.sgf.SgfParser;

/**
 * Builds a fuseki book from a (possibly nested) directory of SGF files. First
 * processes the data to create a raw book, then processes this to create a
 * final book read in by FusekiBook.
 */
public final class FusekiBookBuilder {

	/**
	 * At and below this number of responses, we store lists of responses. Above
	 * this, we store frequencies of responses (indexed by the move).
	 */
	public static final int SHORT_ARRAY_LIMIT = 50;

	public static void main(String[] args) {
		final FusekiBookBuilder builder = new FusekiBookBuilder(20, 50,
				"books", true);
		// Directory below contains SGF
		builder.analyzeFiles(new File(
				"/Network/Servers/maccsserver.lclark.edu/Users/mdreyer/Desktop/KGS Files/"));
		builder.writeFile();
		// To only build final book from raw book, comment two lines above
		builder.buildFinalBook();
	}

	/**
	 * Maps board fancy hashes to short arrays. These are either medium arrays
	 * (lists of moves played in response to that board) or long arrays (count
	 * of how many times each move has been played in response to that board).
	 */
	private BigHashMap<short[]> bigMap;

	/** For each rotation and reflection. */
	private final Board[] boards;

	private final CoordinateSystem coords;

	/** The object to be written to the output file. */
	private final SmallHashMap finalMap;

	/** Moves at or beyond this depth into the game are ignored. */
	private final int maxMoves;

	/** Directory to store the raw and final books. */
	private final String objectFilePath;

	/**
	 * A move is only stored in the final map if it has been seen at least this
	 * many times.
	 */
	private final int requiredSeen;

	/**
	 * Maps board fancy hashes to responses. Once there has been a second
	 * response, bigMap is used.
	 */
	private final SmallHashMap smallMap;

	/** If true, prints messages to stdout indicating progress. */
	private final boolean verbose;

	public FusekiBookBuilder(int maxMoves, int requiredSeen,
			String directoryName, boolean verbose) {
		smallMap = new SmallHashMap();
		bigMap = new BigHashMap<>();
		finalMap = new SmallHashMap();
		this.maxMoves = maxMoves;
		this.requiredSeen = requiredSeen;
		boards = new Board[8];
		coords = CoordinateSystem.forWidth(19);
		for (int i = 0; i < boards.length; i++) {
			boards[i] = new Board(coords.getWidth());
		}
		objectFilePath = OREGO_ROOT + directoryName;
		new File(objectFilePath).mkdir();
		this.verbose = verbose;
	}

	/**
	 * Analyze file, modifying smallMap and bigMap. If file is a directory,
	 * recursively analyze everything in it.
	 */
	public void analyzeFiles(File file) {
		if (file.isDirectory()) {
			if (verbose) {
				System.out.println("Analyzing files in " + file.getName());
			}
			for (final File tempFile : file.listFiles()) {
				analyzeFiles(tempFile);
			}
		} else if (file.getPath().endsWith(".sgf")) {
			final SgfParser parser = new SgfParser(coords);
			final List<List<Short>> games = parser.parseGamesFromFile(file,
					maxMoves);
			updateMaps(games);
		}
	}

	private void analyzeMove(short move, long hash) {
		if (bigMap.containsKey(hash)) {
			final short[] array = bigMap.get(hash);
			if (array.length < SHORT_ARRAY_LIMIT) {
				// Expanding medium map
				final short[] temp = new short[array.length + 1];
				for (int i = 0; i < array.length; i++) {
					temp[i] = array[i];
				}
				temp[temp.length - 1] = move;
				bigMap.put(hash, temp);
			} else if (array.length == SHORT_ARRAY_LIMIT) {
				// Converting medium map to big map
				final short[] temp = new short[coords
						.getFirstPointBeyondBoard()];
				for (int i = 0; i < array.length; i++) {
					temp[array[i]]++;
				}
				temp[move]++;
				bigMap.put(hash, temp);
			} else {
				// Incrementing count in big map
				array[move]++;
				if (array[move] < 0) {
					array[move] = Short.MAX_VALUE;
				}
			}
		} else if (smallMap.containsKey(hash)) {
			// Expanding small map
			final short[] temp = new short[2];
			temp[0] = smallMap.get(hash);
			temp[1] = move;
			bigMap.put(hash, temp);
		} else {
			// Adding entry to small map
			smallMap.put(hash, move);
		}

	}

	@SuppressWarnings({ "unchecked", "boxing" })
	public void buildFinalBook() {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				objectFilePath + File.separator + "RawFusekiBook19.data"))) {
			bigMap = (BigHashMap<short[]>) in.readObject();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(objectFilePath + File.separator
						+ "fuseki19.data"))) {
			findHighestCounts();
			out.writeObject(maxMoves);
			out.writeObject(finalMap);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Finds the most common move and returns it if it occurred at least
	 * manyTimes times. Otherwise, returns NO_POINT.
	 */
	public short findHighest(short[] counts) {
		short winner = CoordinateSystem.NO_POINT;
		if (counts.length <= SHORT_ARRAY_LIMIT) {
			sort(counts);
			final short[] frequency = new short[coords
					.getFirstPointBeyondBoard()];
			short mostFrequent = 0;
			for (short p = 0; p < counts.length; p++) {
				frequency[counts[p]]++;
				if (frequency[counts[p]] >= mostFrequent) {
					mostFrequent = frequency[counts[p]];
					if (mostFrequent >= requiredSeen) {
						winner = counts[p];
					}
				}
			}
		} else {
			for (final short p : coords.getAllPointsOnBoard()) {
				if (counts[p] >= requiredSeen && counts[p] >= counts[winner]) {
					winner = p;
				}
			}
		}
		return winner;
	}

	/**
	 * Finds the most popular next move for each board configuration and stores
	 * it in finalMap.
	 */
	@SuppressWarnings("boxing")
	public void findHighestCounts() {
		for (final long boardHash : bigMap.getKeys()) {
			final short[] inMap = bigMap.get(boardHash);
			// This null check is necessary because bigMap.getKeys() returns
			// the raw array from the hash table, which can contain null "keys"
			if (inMap != null) {
				final short move = findHighest(inMap);
				if (move != CoordinateSystem.NO_POINT) {
					finalMap.put(boardHash, move);
				}
			}

		}
	}

	private void processGame(List<Short> game) {
		short[] transformations = new short[8];
		for (final short move : game) {
			transformations[0] = move;
			transformations[1] = reflectA(move);
			transformations[2] = reflectB(move);
			transformations[3] = reflectC(move);
			transformations[4] = reflectD(move);
			transformations[5] = rotate90(move);
			transformations[6] = rotate180(move);
			transformations[7] = rotate270(move);
			for (int i = 0; i < transformations.length; i++) {
				analyzeMove(transformations[i], boards[i].getFancyHash());
				boards[i].play(transformations[i]);
			}
		}
	}

	/**
	 * Returns the point at move reflected over the line r = -c + 19.
	 */
	public short reflectA(short move) {
		final int row = coords.row(move);
		final int col = coords.column(move);
		final int r2 = col;
		final int c2 = row;
		final short p = coords.at(r2, c2);
		return p;
	}

	/**
	 * Returns the point at move reflected over the line c = 10 (i.e. c = k).
	 */
	public short reflectB(short move) {
		final int row = coords.row(move);
		final int col = coords.column(move);
		final int r2 = row;
		final int c2 = coords.getWidth() - 1 - col;
		final short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move reflected over the line c = r. */
	public short reflectC(short move) {
		final int row = coords.row(move);
		final int col = coords.column(move);
		final int r2 = coords.getWidth() - 1 - col;
		final int c2 = coords.getWidth() - 1 - row;
		final short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move reflected over the line r = 10. */
	public short reflectD(short move) {
		final int row = coords.row(move);
		final int col = coords.column(move);
		final int r2 = coords.getWidth() - 1 - row;
		final int c2 = col;
		final short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move rotated counterclockwise by 180 degrees. */
	public short rotate180(short move) {
		final int row = coords.row(move);
		final int col = coords.column(move);
		final int r2 = coords.getWidth() - 1 - row;
		final int c2 = coords.getWidth() - 1 - col;
		final short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move rotated counterclockwise by 270 degrees. */
	public short rotate270(short move) {
		final int row = coords.row(move);
		final int col = coords.column(move);
		final int r2 = col;
		final int c2 = coords.getWidth() - 1 - row;
		final short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move rotated counterclockwise by 90 degrees. */
	public short rotate90(short move) {
		final int row = coords.row(move);
		final int col = coords.column(move);
		final int r2 = coords.getWidth() - 1 - col;
		final int c2 = row;
		final short p = coords.at(r2, c2);
		return p;
	}

	/** Updates smallMap and bigMap for all of the specified games. */
	private void updateMaps(List<List<Short>> games) {
		for (final List<Short> game : games) {
			for (final Board board : boards) {
				board.clear();
			}
			processGame(game);
		}
	}

	public void writeFile() {
		final File directory = new File(objectFilePath + File.separator
				+ "RawFusekiBook19.data");
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(directory))) {
			out.writeObject(bigMap);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
