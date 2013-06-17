package orego.book;

import orego.core.Board;
import orego.core.Coordinates;
import static orego.core.Coordinates.*;

/**
 * Selects a popular move if one is available, and then suggests moves in open
 * corners or edges.
 */
public class LateOpeningBook extends FusekiBook {

	private static final long serialVersionUID = 1L;

	public LateOpeningBook() {
		super();
	}

	public int nextMove(Board board) {
		System.out.println("Hi, I'm the LateOpeningBook!");
		// If FusekiBook recommends a move, recommend it to the player.
		int move = super.nextMove(board);
		if (move != NO_POINT) {
			return move;
		}
		
		// Otherwise, try to play in a wide open space in a corner.
		// If we find an empty 9x9 region in any corner of the (19x19) board, we will play in the middle.
		
		int e5 = at("e5");
		
		for (int p : getAllPointsOnBoard()) {
			if (Math.abs(row(p) - row(e5)) < 4) 
		}
		
		for (int p : getAllPointsOnBoard()) {
			// Do something with p
		}
		
		
		// Otherwise, try to play in a wide open edge.

		return move;
	}
}
