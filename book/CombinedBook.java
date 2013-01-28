package orego.book;

import java.io.Serializable;
import orego.core.Board;
import static orego.core.Coordinates.*;

/**
 * Combines the FusekiBook and JosekiBook, using the JosekiBook if no move is
 * returned by the FusekiBook.
 */
public class CombinedBook implements OpeningBook, Serializable {

	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/** Fuseki book. */
	private FusekiBook fuseki;

	/** Joseki book. */
	private JosekiBook joseki;

	public CombinedBook() {
		this("SgfFiles");
	}

	public CombinedBook(String directory) {
		fuseki = new FusekiBook(directory);
		joseki = new JosekiBook(directory);
	}

	/**
	 * Makes a fuseki move if there is one, otherwise falls back to the joseki
	 * book.
	 */
	public int nextMove(Board board) {
		int move = fuseki.nextMove(board);
		if (move != NO_POINT) {
			return move;
		}
		move = joseki.nextMove(board);
		if (move != NO_POINT) {
			return move;
		}
		return NO_POINT;
	}

}
