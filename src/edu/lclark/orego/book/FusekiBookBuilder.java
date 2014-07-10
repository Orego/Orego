package edu.lclark.orego.book;

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

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

public class FusekiBookBuilder {

	/**
	 * At and below this number of responses, we store lists of responses. Above
	 * this, we store frequencies of responses (indexed by the move).
	 */
	public static final int SHORT_ARRAY_LIMIT = 50;

	private String objectFilePath;

	public static void main(String[] args) {
		FusekiBookBuilder builder = new FusekiBookBuilder(20, 50, "Books", true);
		// Uncomment the next line to build the book from scratch.
		builder.analyzeFiles(new File(
				"/Network/Servers/maccsserver.lclark.edu/Users/mdreyer/Desktop/KGS Files/"));
		builder.writeFile();
		builder.buildFinalBook();
	}

	private BigHashMap<short[]> bigMap;

	private final Board[] boards;

	private final CoordinateSystem coords;

	private HashMap<Long, Short> finalMap;

	private final int maxMoves;

	private final int requiredSeen;

	private SmallHashMap smallMap;

	private final short[] transformations;

	/** If true, prints messages to stdout indicating progress. */
	private final boolean verbose;
	
	public FusekiBookBuilder(int maxMoves, int requiredSeen, String directoryName, boolean verbose) {
		smallMap = new SmallHashMap();
		bigMap = new BigHashMap<>();
		finalMap = new HashMap<>();
		this.maxMoves = maxMoves;
		this.requiredSeen = requiredSeen;
		boards = new Board[8];
		coords = CoordinateSystem.forWidth(19);
		transformations = new short[8];
		for (int i = 0; i < boards.length; i++) {
			boards[i] = new Board(coords.getWidth());
		}
		objectFilePath = OREGO_ROOT + directoryName;
		new File(objectFilePath).mkdir();
		this.verbose = verbose;
	}

	public void writeFile() {
		File directory = new File(objectFilePath + File.separator + "RawFusekiBook19.data");
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(directory))) {
			out.writeObject(bigMap);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void analyzeFiles(File file) {
		File[] allFiles = file.listFiles();
		if (allFiles != null) {
			if (verbose) {
				System.out.println("Analyzing files in " + file.getName());
			}
			for (File tempFile : allFiles) {
				analyzeFiles(tempFile);
			}
		} else {
			if (file.getPath().endsWith(".sgf")) {
				SgfParser parser = new SgfParser(coords);
				List<List<Short>> games = parser.parseGamesFromFile(file,
						maxMoves);
				try {
					buildRawBook(games);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					System.err.println(file.getPath());
					System.exit(1);
				}
			}
		}
	}

	private void analyzeMove(short move, long hash) {
		if (bigMap.containsKey(hash)) {
			short[] array = bigMap.get(hash);
			if (array.length < SHORT_ARRAY_LIMIT) {
				// Expanding medium map
				short[] temp = new short[array.length + 1];
				for (int i = 0; i < array.length; i++) {
					temp[i] = array[i];
				}
				temp[temp.length - 1] = move;
				bigMap.put(hash, temp);
			} else if (array.length == SHORT_ARRAY_LIMIT) {
				// Converting medium map to big map
				short[] temp = new short[coords.getFirstPointBeyondBoard()];
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
			short[] temp = new short[2];
			temp[0] = (short) smallMap.get(hash);
			temp[1] = move;
			bigMap.put(hash, temp);
		} else {
			// Adding entry to small map
			smallMap.put(hash, move);
		}

	}

	@SuppressWarnings({ "unchecked", "boxing" })
	public void buildFinalBook() {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(objectFilePath
				+ File.separator + "RawFusekiBook19.data"))) {
			bigMap = (BigHashMap<short[]>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(objectFilePath + File.separator + "FusekiBook19.data"))) {
			findHighestCounts();
			out.writeObject(maxMoves);
			out.writeObject(finalMap);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void buildRawBook(List<List<Short>> games) {
		for (List<Short> game : games) {
			processGame(game);
			for (Board board : boards) {
				board.clear();
			}
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
			short[] frequency = new short[coords.getFirstPointBeyondBoard()];
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
			for (short p : coords.getAllPointsOnBoard()) {
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
		for (long boardHash : bigMap.getKeys()) {
			short[] inMap = bigMap.get(boardHash);
			// This null check is necessary because bigMap.getKeys() returns
			// the raw array from the hash table, which can contain null "keys"
			if (inMap != null) {
				short move = findHighest(inMap);
				if (move != CoordinateSystem.NO_POINT) {
					finalMap.put(boardHash, move);
				}
			}

		}
	}

	private void processGame(List<Short> game) {
		for (short move : game) {
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
		int row = coords.row(move);
		int col = coords.column(move);
		int r2 = col;
		int c2 = row;
		short p = coords.at(r2, c2);
		return p;
	}

	/**
	 * Returns the point at move reflected over the line c = 10 (i.e. c = k).
	 */
	public short reflectB(short move) {
		int row = coords.row(move);
		int col = coords.column(move);
		int r2 = row;
		int c2 = coords.getWidth() - 1 - col;
		short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move reflected over the line c = r. */
	public short reflectC(short move) {
		int row = coords.row(move);
		int col = coords.column(move);
		int r2 = coords.getWidth() - 1 - col;
		int c2 = coords.getWidth() - 1 - row;
		short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move reflected over the line r = 10. */
	public short reflectD(short move) {
		int row = coords.row(move);
		int col = coords.column(move);
		int r2 = coords.getWidth() - 1 - row;
		int c2 = col;
		short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move rotated counterclockwise by 180 degrees. */
	public short rotate180(short move) {
		int row = coords.row(move);
		int col = coords.column(move);
		int r2 = coords.getWidth() - 1 - row;
		int c2 = coords.getWidth() - 1 - col;
		short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move rotated counterclockwise by 270 degrees. */
	public short rotate270(short move) {
		int row = coords.row(move);
		int col = coords.column(move);
		int r2 = col;
		int c2 = coords.getWidth() - 1 - row;
		short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move rotated counterclockwise by 90 degrees. */
	public short rotate90(short move) {
		int row = coords.row(move);
		int col = coords.column(move);
		int r2 = coords.getWidth() - 1 - col;
		int c2 = row;
		short p = coords.at(r2, c2);
		return p;
	}

}
