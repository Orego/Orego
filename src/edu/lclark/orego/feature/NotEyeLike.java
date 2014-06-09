package edu.lclark.orego.feature;

import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.*;

/**
 * True unless p is "like" an eye for the color to play, that is, is surrounded
 * by friendly stones and having no more than one (zero at the board edge)
 * diagonally adjacent enemy stones. It is almost always a bad idea to play in
 * such a point. The point p is assumed to be vacant.
 */
public final class NotEyeLike implements Predicate {

	private final Board board;
	
	private static int[] eyelikeThreshold;

	public NotEyeLike(Board board) {
		this.board = board;
		CoordinateSystem coords = board.getCoordinateSystem();
		eyelikeThreshold = new int[coords.getFirstPointBeyondBoard()];
		for (short p : coords.getAllPointsOnBoard()) {
			eyelikeThreshold[p] = 2;
			for (int i = FIRST_DIAGONAL_NEIGHBOR; i <= LAST_DIAGONAL_NEIGHBOR; i++) {
				short n = coords.getNeighbors(p)[i];
				if (!coords.isOnBoard(n)) {
					eyelikeThreshold[p] = 1;
				}
			}
		}
	}

	@Override
	public boolean at(short p) {
		assert board.getColorAt(p) == VACANT;
		StoneColor color = board.getColorToPlay();
		if (!board.hasMaxNeighborsForColor(color, p)) {
			return true;
		}
		int count = 0;
		int oppositeIndex = color.opposite().index();
		short[] neighbors = board.getCoordinateSystem().getNeighbors(p);
		for (int i = FIRST_DIAGONAL_NEIGHBOR; i <= LAST_DIAGONAL_NEIGHBOR; i++) {
			if (board.getColorAt(neighbors[i]).index() == oppositeIndex) {
				count++;
			}
		}
		return count >= eyelikeThreshold[p];
	}

}
