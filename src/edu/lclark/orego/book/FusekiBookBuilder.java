package edu.lclark.orego.book;

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

public class FusekiBookBuilder {

	private SgfParser parser;

	private SmallHashMap smallMap;

	private BigHashMap<short[]> bigMap;

	private int maxMoves;

	private HashMap<Long, Integer> finalMap;

	private final Board[] boards;

	private final CoordinateSystem coords;

	private final short[] transformations;

	/**
	 * At and below this number of responses, we store lists of responses. Above
	 * this, we store frequencies of responses (indexed by the move).
	 */
	public static final int SHORT_ARRAY_LIMIT = 50;

	public static void main(String[] args) {
		FusekiBookBuilder builder = new FusekiBookBuilder(10);
		// Uncomment the next line to build the book from scratch.
		builder.buildRawBook(builder.analyzeFile(new File("SgfFiles")));
		builder.buildFinalBook("SgfFiles");
	}

	private void buildRawBook(List<List<Short>> games) {
		for (List<Short> game : games) {
			processGame(game);
			for (Board board : boards) {
				board.clear();
			}
		}

		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream("Books" + File.separator + "RawJosekiBook"
						+ coords.getWidth() + ".data"))) {
			out.writeObject(bigMap);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
	
	@SuppressWarnings("unchecked")
	public void buildFinalBook(String folder) throws IOException,
			ClassNotFoundException {
		String directory = OREGO_ROOT_DIRECTORY + folder + File.separator
				+ getBoardWidth();
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				"Books" + File.separator + "RawJosekiBook" + getBoardWidth()
						+ ".data"));
		debug("Reading raw book...");
		// The next line would cause a warning
		setBigMap((BigHashMap<short[]>) in.readObject());
		in.close();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				directory + File.separator + getFinalBookName() + getBoardWidth()
						+ ".data"));
		out.writeObject(computeFinalEntries());
		out.close();
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
				boards[i].play(transformations[i]);
				analyzeMove(transformations[i], boards[i].getFancyHash());
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

	public FusekiBookBuilder(int maxMoves) {
		smallMap = new SmallHashMap();
		bigMap = new BigHashMap<>();
		finalMap = new HashMap<>();
		this.maxMoves = maxMoves;
		boards = new Board[8];
		coords = CoordinateSystem.forWidth(19);
		transformations = new short[8];
	}

	private List<List<Short>> analyzeFile(File folder) {
		return new SgfParser(coords).parseGamesFromFile(folder, maxMoves);
	}

}
