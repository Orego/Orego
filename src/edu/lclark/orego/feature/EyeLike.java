package edu.lclark.orego.feature;

import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.*;

// TODO Maybe this should implement a Feature interface?
public final class EyeLike {

	/**
	 * Returns true if p is "like" an eye for the color to play, that is, is
	 * surrounded by friendly stones and having no more than one (zero at the
	 * board edge) diagonally adjacent enemy stones. It is almost always a bad
	 * idea to play in such a point. The point p is assumed to be vacant.
	 */
	public static final boolean isEyeLike(short p, Board board) {
		assert board.getColorAt(p) == VACANT;
		StoneColor color = board.getColorToPlay();
		int diagonalEnemyCount = 0;
		short[] neighbors = board.getCoordinateSystem().getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
			Color c = board.getColorAt(n);
			if (c == color) {
				continue;
			}
			if (c == OFF_BOARD) {
				diagonalEnemyCount = 1;
				continue;
			}
			return false;
		}
		for (int i = FIRST_DIAGONAL_NEIGHBOR; i <= LAST_DIAGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
			if (board.getColorAt(n) == color.opposite()) {
				diagonalEnemyCount++;
				if (diagonalEnemyCount == 2) {
					return false;
				}
			}
		}
		return true;
	}

}
