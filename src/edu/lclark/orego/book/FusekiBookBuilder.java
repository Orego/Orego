package edu.lclark.orego.book;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import static java.util.Arrays.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	public static final int MEDIUM_ARRAY_LIMIT = 50;

	public static void main(String[] args) {
		final FusekiBookBuilder builder = new FusekiBookBuilder(20, 50,
				"books", true);
		// Directory below contains SGF
		builder.processFiles(new File(
				"/Network/Servers/maccsserver.lclark.edu/Users/mdreyer/Desktop/KGS Files/"));
		builder.writeRawBook();
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

	/**
	 * A move is only stored in the final map if it has been seen at least this
	 * many times.
	 */
	private final int countThreshold;

	/** The object to be written to the output file. */
	private final SmallHashMap finalMap;

	/** Moves at or beyond this depth into the game are ignored. */
	private final int maxMoves;

	/** Directory to store the raw and final books. */
	private final String objectFilePath;

	/**
	 * Maps board fancy hashes to responses. Once there has been a second
	 * response, bigMap is used.
	 */
	private final SmallHashMap smallMap;

	/** If true, prints messages to stdout indicating progress. */
	private final boolean verbose;

	public FusekiBookBuilder(int maxMoves, int countThreshold,
			String directoryName, boolean verbose) {
		// If countThreshold were 1, we'd have to look in smallMap in
		// buildFinalBook
		assert countThreshold > 1;
		smallMap = new SmallHashMap();
		bigMap = new BigHashMap<>();
		finalMap = new SmallHashMap();
		this.maxMoves = maxMoves;
		this.countThreshold = countThreshold;
		boards = new Board[8];
		coords = CoordinateSystem.forWidth(19);
		for (int i = 0; i < boards.length; i++) {
			boards[i] = new Board(coords.getWidth());
		}
		objectFilePath = OREGO_ROOT + directoryName;
		new File(objectFilePath).mkdir();
		this.verbose = verbose;
	}

	/** Builds the final book from the raw book. */
	@SuppressWarnings({ "unchecked", "boxing" })
	void buildFinalBook() {
		try {
			try (ObjectInputStream in = new ObjectInputStream(
					new FileInputStream(objectFilePath + File.separator
							+ "rawfuseki19.data"))) {
				bigMap = (BigHashMap<short[]>) in.readObject();
			}
			try (ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(objectFilePath + File.separator
							+ "fuseki19.data"))) {
				findHighestCounts();
				out.writeObject(maxMoves);
				out.writeObject(finalMap);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Finds the most common move and returns it if it occurred at least
	 * countThreshold times. Otherwise, returns NO_POINT.
	 */
	private short findHighest(short[] counts) {
		short winner = CoordinateSystem.NO_POINT;
		if (counts.length <= MEDIUM_ARRAY_LIMIT) {
			sort(counts);
			final short[] frequency = new short[coords
					.getFirstPointBeyondBoard()];
			short mostFrequent = 0;
			for (short p = 0; p < counts.length; p++) {
				frequency[counts[p]]++;
				if (frequency[counts[p]] >= mostFrequent) {
					mostFrequent = frequency[counts[p]];
					if (mostFrequent >= countThreshold) {
						winner = counts[p];
					}
				}
			}
		} else {
			for (final short p : coords.getAllPointsOnBoard()) {
				if (counts[p] >= countThreshold && counts[p] >= counts[winner]) {
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
	private void findHighestCounts() {
		for (final long boardHash : bigMap.getKeys()) {
			final short[] moves = bigMap.get(boardHash);
			// This null check is necessary -- see BigHashMap.getKeys()
			if (moves != null) {
				final short move = findHighest(moves);
				if (move != CoordinateSystem.NO_POINT) {
					finalMap.put(boardHash, move);
				}
			}

		}
	}

	/**
	 * Analyze file, modifying smallMap and bigMap. If file is a directory,
	 * recursively analyze everything in it.
	 */
	void processFiles(File file) {
		if (file.isDirectory()) {
			if (verbose) {
				System.out.println("Analyzing files in " + file.getName());
			}
			for (final File tempFile : file.listFiles()) {
				processFiles(tempFile);
			}
		} else if (file.getPath().endsWith(".sgf")) {
			final SgfParser parser = new SgfParser(coords, true);
			final List<List<Short>> games = parser.parseGamesFromFile(file,
					maxMoves);
			processGames(games);
		}
	}

	/** Processes the moves in game, updating bigMap and smallMap. */
	private void processGame(List<Short> game) {
		final short[] transformations = new short[8];
		for (final short move : game) {
			transformations[0] = move;
			transformations[1] = rotate90(move);
			transformations[2] = rotate90(transformations[1]);
			transformations[3] = rotate90(transformations[2]);
			transformations[4] = reflect(move);
			transformations[5] = rotate90(transformations[4]);
			transformations[6] = rotate90(transformations[5]);
			transformations[7] = rotate90(transformations[6]);
			for (int i = 0; i < transformations.length; i++) {
				processMove(transformations[i], boards[i].getFancyHash());
				boards[i].play(transformations[i]);
			}
		}
	}

	/** Updates smallMap and bigMap for all of the specified games. */
	private void processGames(List<List<Short>> games) {
		for (final List<Short> game : games) {
			for (final Board board : boards) {
				board.clear();
			}
			processGame(game);
		}
	}

	/** Analyze move as a response to fancyHash, updating bigMap and smallMap. */
	private void processMove(short move, long fancyHash) {
		if (bigMap.containsKey(fancyHash)) {
			// The entry in bigMap is either a list of moves (medium) or a
			// move-indexed array of counts
			final short[] array = bigMap.get(fancyHash);
			if (array.length < MEDIUM_ARRAY_LIMIT) {
				// It's medium, but there's room to make it larger
				final short[] temp = new short[array.length + 1];
				for (int i = 0; i < array.length; i++) {
					temp[i] = array[i];
				}
				temp[temp.length - 1] = move;
				bigMap.put(fancyHash, temp);
			} else if (array.length == MEDIUM_ARRAY_LIMIT) {
				// It has hit the medium size limit; convert it to large
				final short[] temp = new short[coords
						.getFirstPointBeyondBoard()];
				for (int i = 0; i < array.length; i++) {
					temp[array[i]]++;
				}
				temp[move]++;
				bigMap.put(fancyHash, temp);
			} else {
				// It's already large; just increment a count
				array[move]++;
				if (array[move] < 0) {
					array[move] = Short.MAX_VALUE;
				}
			}
		} else if (smallMap.containsKey(fancyHash)) {
			// We've seen this hash once before; move from small to medium
			final short[] temp = new short[2];
			temp[0] = smallMap.get(fancyHash);
			temp[1] = move;
			bigMap.put(fancyHash, temp);
		} else {
			// First time we've seen this hash; add to small map
			smallMap.put(fancyHash, move);
		}
	}

	/** Returns the point at move reflected over the line c = r. */
	public short reflect(short move) {
		final int row = coords.row(move);
		final int col = coords.column(move);
		final int r2 = coords.getWidth() - 1 - col;
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

	/** Writes the raw book to a file. */
	public void writeRawBook() {
		final File directory = new File(objectFilePath + File.separator
				+ "rawfuseki19.data");
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(directory))) {
			out.writeObject(bigMap);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
