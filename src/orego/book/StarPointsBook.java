package orego.book;

import orego.core.Board;
import static orego.core.Coordinates.*;

/**
 * Plays on star points. Note that this book will play on a star point even if
 * that corner is not empty. This is not a strong book, just a simple example of
 * one.
 */
public class StarPointsBook implements OpeningBook {

	/**
	 * The star points (hoshi). On the 19x19 board, corner points are first, the
	 * center last.
	 */
	private int[] STAR_POINTS = 
			getBoardWidth() == 9 ? new int[] {
			at("e5"), 
			at("c3"), 
			at("c7"), 
			at("g3"), 
			at("g7") }
			: getBoardWidth() == 19 ? new int[] { 
			at("d4"), 
			at("d16"), 
			at("q4"),		
			at("q16"), 
			at("k16"), 
			at("k4"), 
			at("d10"), 
			at("q10"),
			at("k10") } 
			: null;

	public int nextMove(Board board) {
		for (int p : STAR_POINTS) {
			if (board.isLegal(p)) {
				return p;
			}
		}
		return NO_POINT;
	}

}
