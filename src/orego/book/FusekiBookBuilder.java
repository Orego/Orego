package orego.book;

import static orego.core.Coordinates.*;
import static orego.experiment.Debug.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import orego.core.Board;

/**
 * Reads in a directory of files containing SGF games, then stores the
 * information into a map, which is then saved in a file. The file can be read
 * by FusekiBook.
 */
public class FusekiBookBuilder extends BookBuilder {

	/**
	 * Builds the final fuseki book file.
	 */
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		setDebugToStderr(true);
		FusekiBookBuilder builder = new FusekiBookBuilder(10);
		// Uncomment the next line to build the book from scratch.
		builder.buildRawBook("SgfFiles");
		builder.buildFinalBook("SgfFiles");
	}

	public FusekiBookBuilder(int manyTimes) {
		super(manyTimes);
	}

	public Object computeFinalEntries() {
		findHighestCounts();
		return getFinalMap();
	}

	public String getFinalBookName() {
		return "Fuseki";
	}

	public String getRawBookName() {
		return "RawFuseki";
	}

	protected void processFile(File file)
			throws FileNotFoundException, IOException {
		Board board = new Board();
		int[][] moveset = new int[8][MAX_BOOK_DEPTH];
		List<List<Integer>> games = getGames(file);
		for (List<Integer> moves : games) {
			// Store moves
			for (int t = 0; (t < MAX_BOOK_DEPTH) && (t < moves.size()); t++) {
				int move = moves.get(t);
				if (move == NO_POINT) {
					t--;
					continue;
				}
				moveset[0][t] = move;
				if (move == PASS) {
					break;
				}
				moveset[1][t] = rotate90(move);
				moveset[2][t] = rotate180(move);
				moveset[3][t] = rotate270(move);
				moveset[4][t] = reflectA(move);
				moveset[5][t] = reflectB(move);
				moveset[6][t] = reflectC(move);
				moveset[7][t] = reflectD(move);
			}
			// Store the hash-move pairs
			for (int transformation = 0; transformation < 8; transformation++) {
				board.clear();
				int t = 0;
				do {
					storeMove(moveset[transformation][t], board.getHash());
					board.play(moveset[transformation][t]);
					t++;
				} while (t < MAX_BOOK_DEPTH);
			}
		}
		debug("Map contains " + getBigMap().size() + " board hashes");
	}

}
