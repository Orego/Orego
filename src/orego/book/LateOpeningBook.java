package orego.book;

import orego.core.Board;
import orego.core.Colors;
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

	@Override
	public int nextMove(Board board) {
		// If FusekiBook recommends a move, recommend it to the player.
		int move = super.nextMove(board);
		if (move != NO_POINT) {
			return move;
		}
		// Otherwise, try to play in a wide open space in a corner.
		move = getGoodCornerMove(board);
		if (move != NO_POINT) {
			return move;
		}
		// If we find an empty 5x7 region on any side of the (19x19) board, we will play on line 3.
		move = getGoodEdgeMove(board);
		return move;
	}
	
	/** Checks if a 9x9 space around each corner is open.
	 * If so, it returns the move at the 4 x 4 position of that corner 
	 * */
	protected int getGoodCornerMove(Board board) {
		boolean playAtCorner = true;
		//Check south-east corner.
		int a7 = at("a7");
		int g1 = at("g1");
		southeastloop:
		for(int i = row(a7); i <= row(g1); i++) {
			for(int j = column(a7); j <= column(g1); j++) {
				if(board.getColor(at(i, j)) != Colors.VACANT) {
					playAtCorner = false;
					break southeastloop;
				}
			}
		}
		if(playAtCorner) {			
			return at("c3");
		} else {
			playAtCorner = true;
		}
		//Check south-west corner.
		int n7 = at("n7");
		int t1 = at("t1");
		southwestloop:
		for(int i = row(n7); i <= row(t1); i++) {
		for(int j = column(n7); j <= column(t1); j++) {
				if(board.getColor(at(i, j)) != Colors.VACANT) {
					playAtCorner = false;
					break southwestloop;
				}
			}
		}
		if(playAtCorner) {			
			return at("r3");
		} else {
			playAtCorner = true;
		}
		//Check north-west corner.
		int a19 = at("a19");
		int g13 = at("g13");
		northwestloop:
		for(int i = row(a19); i <= row(g13); i++) {
			for(int j = column(a19); j <= column(g13); j++) {
				if(board.getColor(at(i, j)) != Colors.VACANT) {
					playAtCorner = false;
					break northwestloop;
				}
			}
		}
		if(playAtCorner) {			
			return at("c17");
		} else {
			playAtCorner = true;
		}
		//Check the north-east corner.
		int n19 = at("n19");
		int t13 = at("t13");
		northeastloop:
		for(int i = row(n19); i <= row(t13); i++) {
		for(int j = column(n19); j <= column(t13); j++) {
				if(board.getColor(at(i, j)) != Colors.VACANT) {
					playAtCorner = false;
					break northeastloop;
				}
			}
		}
		if(playAtCorner) {			
			return at("r17");
		}
		return NO_POINT;
	}
	
	protected int getGoodEdgeMove(Board board) {
		int move = NO_POINT;
		for(int i = column(at("d3")); i <= column(at("q3")); i++) {
			move = at(row(at("a3")), i);
			if(horizontalWideOpenSpaceExists(move, board)) {
				return move;
			}
		}
		for(int i = column(at("d17")); i <= column(at("q17")); i++) {
			move = at(row(at("a17")), i);
			if(horizontalWideOpenSpaceExists(move, board)) {
				return move;
			}
		}
		for(int i = row(at("c16")); i <= row(at("c4")); i++) {
			move = at(i, column(at("c1")));
			if(verticalWideOpenSpaceExists(move, board)) {
				return move;
			}
		}
		for(int i = row(at("r16")); i <= row(at("r4")); i++) {
			move = at(i, column(at("r1")));
			if(verticalWideOpenSpaceExists(move, board)) {
				return move;
			}
		}
		return NO_POINT;
	}
	
	/** Checks a vertical 7x5 space around the given point for any stones. */
	protected boolean verticalWideOpenSpaceExists(int p, Board board) {
		for(int i = (row(p) - 3); i <= (row(p) + 3); i++) {
			for(int j = (column(p) - 2); j <= (column(p) + 2); j++) {				
				if(board.getColor(at(i, j)) != Colors.VACANT) {			
					return false;
				}
			}
		}
		return true;
	}
	
	/** Checks a horizontal 5x7 space around the given point for any stones. */
	protected boolean horizontalWideOpenSpaceExists(int p, Board board) {
		for(int i = (row(p) - 2); i <= (row(p) + 2); i++) {
			for(int j = (column(p) - 3); j <= (column(p) + 3); j++) {
				if(board.getColor(at(i, j)) != Colors.VACANT) {			
					return false;
				}
			}
		}
		return true;
	}
}
