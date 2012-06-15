package orego.book;

import static orego.core.Coordinates.*;
import static orego.experiment.Debug.OREGO_ROOT_DIRECTORY;
import static java.util.Arrays.sort;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static orego.experiment.Debug.*;

/**
 * General class for building opening books.
 * 
 * The key data structure here consists of two maps, smallMap and bigMap. Each
 * time a board configuration (represented as a long Zobrist hash) is
 * encountered, the reponse move is stored. If this is the first time that hash
 * has been seen, the hash is mapped to the move in smallMap. The vast majority
 * of hashes are seen only once. If a hash is seen a second time, the entry is
 * moved to bigMap, mapping the hash to an array of two shorts (the two moves).
 * This array is expanded each time the hash is seen again. In the rare but
 * important case where a hash is seen many times (as defined by the constant
 * SHORT_ARRAY_LIMIT), the entry in bigMap is changed from an array of moves to
 * an array of counts; this array is a direct addressing table, with the move
 * used as an index into the table of counts.
 */
public abstract class BookBuilder {

	/**
	 * How far into the game the book stores moves.
	 */
	public static final int MAX_BOOK_DEPTH = 40;

	/**
	 * At and below this number of responses, we store lists of responses. Above
	 * this, we store frequencies of responses (indexed by the move).
	 */
	public static final int SHORT_ARRAY_LIMIT = 50;

	/**
	 * HashMap linking a hash of the board to its response moves and how often
	 * they occur. Some entries are large, and some are medium.
	 */
	private BigHashMap<short[]> bigMap;

	/** Map linking a hash of the board to its most popular response move. */
	private Map<Long, Integer> finalMap;

	/** Number of games processed. */
	private int gameCount;

	/** A move that appears this many times will be kept in the database. */
	private int manyTimes;

	/**
	 * HashMap linking a hash of the board to a response move. Used to store the
	 * move the first time a given board configuration is encountered.
	 */
	private SmallHashMap smallMap;

	public BookBuilder(int manyTimes) {
		assert manyTimes > 1 : "manyTimes must be greater than 1";
		this.manyTimes = manyTimes;
		smallMap = new SmallHashMap();
		bigMap = new BigHashMap<short[]>();
		finalMap = new HashMap<Long, Integer>();
	}

	/**
	 * Reads in the raw book and creates a final version that is usable by
	 * Orego.
	 */
	@SuppressWarnings("unchecked")
	public void buildFinalBook(String folder) throws IOException,
			ClassNotFoundException {
		String directory = OREGO_ROOT_DIRECTORY + folder + File.separator
				+ BOARD_WIDTH;
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				directory + File.separator + getRawBookName() + BOARD_WIDTH
						+ ".data"));
		debug("Reading raw book...");
		// The next line would cause a warning
		setBigMap((BigHashMap<short[]>) in.readObject());
		in.close();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				directory + File.separator + getFinalBookName() + BOARD_WIDTH
						+ ".data"));
		out.writeObject(computeFinalEntries());
		out.close();
	}

	/** Produces a raw opening book using games from SGF files. */
	public void buildRawBook(String folder) throws IOException {
		String directory = OREGO_ROOT_DIRECTORY + folder + File.separator
				+ BOARD_WIDTH;
		setUp(directory);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				directory + File.separator + getRawBookName() + BOARD_WIDTH
						+ ".data"));
		out.writeObject(getBigMap());
		out.close();
		debug(getGameCount() + " games processed");
	}

	/**
	 * Computes and returns the object to be written to a file as a final book.
	 * Assumes that the smallMap and bigMap fields contain raw book information.
	 */
	public abstract Object computeFinalEntries();

	/**
	 * Finds the most common move and returns it if it occurred at least
	 * manyTimes times. Otherwise, returns NO_POINT.
	 */
	public int findHighest(short[] counts) {
		short winner = NO_POINT;
		if (counts.length <= SHORT_ARRAY_LIMIT) {
			sort(counts);
			short[] frequency = new short[FIRST_POINT_BEYOND_BOARD];
			short mostFrequent = 0;
			for (int p = 0; p < counts.length; p++) {
				frequency[counts[p]]++;
				if (frequency[counts[p]] >= mostFrequent) {
					mostFrequent = frequency[counts[p]];
					if (mostFrequent >= manyTimes) {
						winner = counts[p];
					}
				}
			}
		} else {
			for (int p : ALL_POINTS_ON_BOARD) {
				if (counts[p] >= manyTimes && counts[p] >= counts[winner]) {
					winner = (short) p;
				}
			}
		}
		return winner;
	}

	/**
	 * Finds the most popular next move for each board configuration and stores
	 * it in finalMap.
	 */
	public void findHighestCounts() {
		debug("Finding highest counts");
		for (long boardHash : bigMap.getKeys()) {
			short[] inMap = bigMap.get(boardHash);
			// This null check is necessary because bigMap.getKeys() returns
			// the raw array from the hash table, which can contain null "keys"
			if (inMap != null) {
				int move = findHighest(inMap);
				if (move != NO_POINT) {
					finalMap.put(boardHash, move);
				}
			}

		}
		debug(finalMap.size() + " positions now have book responses");
	}

	/**
	 * Returns bigMap.
	 */
	protected orego.book.BigHashMap<short[]> getBigMap() {
		return bigMap;
	}

	/**
	 * Returns the filename for the final book. (The board size and .data will
	 * be added to this filename later.)
	 */
	public abstract String getFinalBookName();

	/**
	 * Returns the final hash map that includes only the board hash and the move
	 * to play from that board.
	 */
	protected Map<Long, Integer> getFinalMap() {
		return finalMap;
	}

	/**
	 * Reads one game from the specified BufferedReader (e.g., a file) and
	 * returns a list of moves from the game. Assumes that the opening ( has
	 * already been consumed. May or may not consume the closing ). Returns null
	 * if the game is invalid for some reason (e.g., it has the wrong board
	 * size) and does not consume the rest of the game.
	 */
	protected List<Integer> getGame(PushbackReader in) throws IOException {
		List<Integer> result = new ArrayList<Integer>();
		int c;
		while (true) {
			c = in.read();
			if (c == ')') {
				return result;
			}
			assert c != -1;
			List<String> properties = getNode(in);
			for (String property : properties) {
				String identifier = property
						.substring(0, property.indexOf('['));
				if (identifier.equals("B") || identifier.equals("W")) {
					int move = sgfToOregoCoordinate(property);
					if (move > NO_POINT) { // Also excludes pass
						result.add(move);
					} else {
						System.out
								.println("Ignoring game with early pass or bogus move");
						return null;
					}
					if (result.size() == MAX_BOOK_DEPTH) {
						return result;
					}
				} else if (identifier.equals("HA")) {
					debug("Ignoring handicap game");
					return null;
				} else if (identifier.equals("SZ")) {
					if (!property.equals("SZ[" + BOARD_WIDTH + "]")) {
						System.out
								.println("Ignoring game with strange board size");
						return null;
					}
				} else if (identifier.equals("AB") || identifier.equals("AW")) {
					debug("Ignoring game with added stones");
					return null;
				} else if (identifier.equals("AE")) {
					debug("Ignoring game with removed stones");
					return null;
				} else if (identifier.equals("PL")) {
					System.out
							.println("Ignoring game where color to play was altered");
					return null;
				} else if (identifier.equals("TB") || identifier.equals("TW")) {
					debug("Ignoring game with marked territory");
					return null;
				}
			}
		}
	}

	/**
	 * Returns the number of games read.
	 */
	protected int getGameCount() {
		return gameCount;
	}

	/**
	 * Reads games from the specified BufferedReader (e.g., a file) and returns
	 * a list, each element of which is the list of moves from one game.
	 */
	protected List<List<Integer>> getGames(BufferedReader reader)
			throws IOException {
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		PushbackReader in = new PushbackReader(reader);
		while (true) {
			// Find the beginning of the next game
			int c;
			do {
				c = in.read();
				if (c == -1) {
					return result;
				}
			} while (c != '(');
			List<Integer> game = getGame(in);
			if (game != null) {
				if (game.size() >= MAX_BOOK_DEPTH) {
					result.add(game);
					gameCount++;
				} else {
					debug("Ignoring game because it was only "
							+ game.size() + " moves long");
				}
			}
		}
	}

	/**
	 * Returns the number of times a move must be played to be recorded in the
	 * book.
	 */
	protected int getManyTimes() {
		return manyTimes;
	}

	/**
	 * Reads one node from an SGF file. Assumes that the opening ; has already
	 * been consumed, but does NOT consume the next ;.
	 */
	protected List<String> getNode(PushbackReader in) throws IOException {
		List<String> result = new ArrayList<String>();
		int c;
		while (true) {
			String property = getProperty(in);
			if (property != null) {
				result.add(property);
			}
			c = in.read();
			assert c != -1;
			if (c == ';') {
				in.unread(c);
				return result;
			} else if (c == ')') {
				in.unread(c);
				return result;
			}
			in.unread(c);
		}
	}

	/**
	 * Returns the next SGF property, or null if there are no more properties in
	 * this node.
	 */
	protected String getProperty(PushbackReader in) throws IOException {
		String result = "";
		int c;
		while (true) {
			c = in.read();
			assert c != -1;
			if ((c == ';') || (c == ')')) {
				in.unread(c);
				return null;
			} else if (c == '[') {
				result += (char) c;
				break;
			}
			if (!Character.isWhitespace(c)) {
				result += (char) c;
			}
		}
		do {
			c = in.read();
			if (c == -1) {
				in.unread(')');
				return null;
			}
			result += (char) c;
		} while (c != ']');
		return result;
	}

	/**
	 * Returns the filename for the raw book. (The board size and .data will be
	 * added to this filename later.)
	 */
	public abstract String getRawBookName();

	/** Returns the small hash map. */
	public SmallHashMap getSmallMap() {
		return smallMap;
	}

	/** Reads SGF game data from a file and stores them in smallMap and bigMap. */
	protected abstract void processFile(BufferedReader reader)
			throws FileNotFoundException, IOException;

	/**
	 * Returns the point at move reflected over the line r = -c + 19.
	 */
	public int reflectA(int move) {
		int row = row(move);
		int col = column(move);
		int r2 = col;
		int c2 = row;
		int p = at(r2, c2);
		return p;
	}

	/**
	 * Returns the point at move reflected over the line c = 10 (i.e. c = k).
	 */
	public int reflectB(int move) {
		int row = row(move);
		int col = column(move);
		int r2 = row;
		int c2 = BOARD_WIDTH - 1 - col;
		int p = at(r2, c2);
		return p;
	}

	/** Returns the point at move reflected over the line c = r. */
	public int reflectC(int move) {
		int row = row(move);
		int col = column(move);
		int r2 = BOARD_WIDTH - 1 - col;
		int c2 = BOARD_WIDTH - 1 - row;
		int p = at(r2, c2);
		return p;
	}

	/** Returns the point at move reflected over the line r = 10. */
	public int reflectD(int move) {
		int row = row(move);
		int col = column(move);
		int r2 = BOARD_WIDTH - 1 - row;
		int c2 = col;
		int p = at(r2, c2);
		return p;
	}

	/** Returns the point at move rotated counterclockwise by 180 degrees. */
	public int rotate180(int move) {
		int row = row(move);
		int col = column(move);
		int r2 = BOARD_WIDTH - 1 - row;
		int c2 = BOARD_WIDTH - 1 - col;
		int p = at(r2, c2);
		return p;
	}

	/** Returns the point at move rotated counterclockwise by 270 degrees. */
	public int rotate270(int move) {
		int row = row(move);
		int col = column(move);
		int r2 = col;
		int c2 = BOARD_WIDTH - 1 - row;
		int p = at(r2, c2);
		return p;
	}

	/** Returns the point at move rotated counterclockwise by 90 degrees. */
	public int rotate90(int move) {
		int row = row(move);
		int col = column(move);
		int r2 = BOARD_WIDTH - 1 - col;
		int c2 = row;
		int p = at(r2, c2);
		return p;
	}

	/** Sets the big map to bigMap. */
	protected void setBigMap(BigHashMap<short[]> bigMap) {
		this.bigMap = bigMap;
	}

	/**
	 * Sets the final map to finalMap.
	 */
	protected void setFinalMap(Map<Long, Integer> finalMap) {
		this.finalMap = finalMap;
	}

	/**
	 * Sets the game count to gameCount.
	 */
	protected void setGameCount(int gameCount) {
		this.gameCount = gameCount;
	}

	/** Sets the small map to smallMap. */
	protected void setSmallMap(SmallHashMap smallMap) {
		this.smallMap = smallMap;
	}

	/**
	 * Takes in a directory of SGF files, builds a HashMap of the first
	 * MAX_BOOK_DEPTH moves of each of the files.
	 */
	public void setUp(String filepath) {
		try {
			File directory = new File(filepath);
			String[] dirList = directory.list();
			for (int i = 0; i < dirList.length; i++) {
				String filename = filepath + File.separator + dirList[i];
				File file = new File(filename);
				if (file.isDirectory()) {
					setUp(filename);
				} else if (dirList[i].toLowerCase().endsWith(".sgf")) {
					debug("Processing " + dirList[i]);
					FileReader reader = new FileReader(file);
					BufferedReader bf = new BufferedReader(reader);
					processFile(bf);
					reader.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Given an SGF-formatted move, returns the corresponding Orego int
	 * representation. If the move cannot be meaningfully interpreted, NO_POINT
	 * is returned.
	 */
	protected int sgfToOregoCoordinate(String currentToken) {
		if (currentToken.equals("B[??]") || currentToken.equals("W[??]")) {
			debug(currentToken + " makes no sense");
			return NO_POINT;
		}
		if (currentToken.equals("B[]") || currentToken.equals("W[]")) {
			return PASS;
		}
		if (currentToken.length() != 5) {
			debug(currentToken + " makes no sense");
			return NO_POINT;
		}
		char char2 = currentToken.charAt(2);
		if (char2 == 't') {
			return PASS;
		}
		int char3 = currentToken.charAt(3);
		int result = -1;
		try {
			result = at(char3 - 'a', char2 - 'a');
		} catch (AssertionError e) {
			debug("Couldn't make sense of " + currentToken);
			e.printStackTrace();
			System.exit(1);
		}
		return result;
	}

	/**
	 * Modifies map to show that move appeared as a response to the board
	 * corresponding to hash.
	 * 
	 * @param move
	 *            The move from a game.
	 * @param transformation
	 *            The transformation (i.e., rotation or reflection)
	 * @param hash
	 *            The board position before move is played.
	 */
	protected void storeMove(int move, long hash) {
		if (bigMap.containsKey(hash)) {
			short[] array = bigMap.get(hash);
			if (array.length < SHORT_ARRAY_LIMIT) {
				// Expanding medium map
				short[] temp = new short[array.length + 1];
				for (int i = 0; i < array.length; i++) {
					temp[i] = array[i];
				}
				temp[temp.length - 1] = (short) move;
				bigMap.put(hash, temp);
			} else if (array.length == SHORT_ARRAY_LIMIT) {
				// Converting medium map to big map
				short[] temp = new short[FIRST_POINT_BEYOND_BOARD];
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
			temp[1] = (short) move;
			bigMap.put(hash, temp);
		} else {
			// Adding entry to small map
			smallMap.put(hash, move);
		}

	}

}