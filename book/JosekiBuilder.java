package orego.book;

import static orego.core.Coordinates.ON_BOARD;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.column;
import static orego.core.Coordinates.row;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import orego.core.Board;

/**
 * Superclass for joseki books.
 * 
 * @see JosekiBookBuilder
 */
public abstract class JosekiBuilder extends BookBuilder {

	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * Returns true if move is in the specified corner (0 is NW, 1 is NE, 2 is
	 * SE, 3 is SW.
	 */
	public static boolean isInCorner(int move, int corner) {
		if (move == PASS) {
			return false;
		}
		assert ON_BOARD[move];
		if (column(move) < 9) { // Left side
			if (row(move) < 9) { // Upper side
				return corner == 0;
			} else if (row(move) > 9) { // Lower side
				return corner == 3;
			}
			return false;
		} else if (column(move) > 9) { // Right side
			if (row(move) < 9) { // Upper side
				return corner == 1;
			} else if (row(move) > 9) { // Lower side
				return corner == 2;
			}
		}
		return false;
	}

	public JosekiBuilder(int manyTimes) {
		super(manyTimes);
	}

	public String getRawBookName() {
		return "RawJoseki";
	}

	protected void processFile(BufferedReader reader) throws IOException {
		Board board = new Board();
		List<List<Integer>> games = getGames(reader);
		for (List<Integer> moves : games) {
			// Store moves
			for (int corner = 0; corner < 4; corner++) {
				int[][] moveset = new int[8][MAX_BOOK_DEPTH];
				for (int t = 0; (t < MAX_BOOK_DEPTH) && (t < moves.size()); t++) {
					if (isInCorner(moves.get(t), corner)) {
						moveset[0][t] = moves.get(t);
						int move = moveset[0][t];
						moveset[1][t] = rotate90(move);
						moveset[2][t] = rotate180(move);
						moveset[3][t] = rotate270(move);
						moveset[4][t] = reflectA(move);
						moveset[5][t] = reflectB(move);
						moveset[6][t] = reflectC(move);
						moveset[7][t] = reflectD(move);
					}
				}
				// Store the hash-move pairs
				for (int transformation = 0; transformation < 8; transformation++) {
					board.clear();
					for (int k = 0; k < MAX_BOOK_DEPTH; k++) {
						if ((moveset[transformation][k] != PASS)
								&& (board.getHash() != 0L)) {
							storeMove(moveset[transformation][k], board
									.getHash());
						}
						board.play(moveset[transformation][k]);
					}
				}
				// Same thing, but with colors inverted (by making black pass
				// for the first move)
				for (int transformation = 0; transformation < 8; transformation++) {
					board.clear();
					board.pass();
					for (int k = 0; k < MAX_BOOK_DEPTH; k++) {
						if ((moveset[transformation][k] != PASS)
								&& (board.getHash() != -1L)) {
							storeMove(moveset[transformation][k], board
									.getHash());
						}
						board.play(moveset[transformation][k]);
					}
				}
			}
		}
	}

}