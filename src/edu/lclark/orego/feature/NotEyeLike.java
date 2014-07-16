package edu.lclark.orego.feature;

import static edu.lclark.orego.core.CoordinateSystem.FIRST_DIAGONAL_NEIGHBOR;
import static edu.lclark.orego.core.CoordinateSystem.LAST_DIAGONAL_NEIGHBOR;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;

/**
 * True unless p is "like" an eye for the color to play, that is, is surrounded
 * by friendly stones and having no more than one (zero at the board edge)
 * diagonally adjacent enemy stones. It is almost always a bad idea to play in
 * such a point. The point p is assumed to be vacant.
 */
@SuppressWarnings("serial")
public final class NotEyeLike implements Predicate {

	private final Board board;

	/**
	 * Number of effective diagonal neighbors at each point due to being on the
	 * edge. (This is 1 at an edge, 0 elsewhere.)
	 */
	private final int[] edgeEnemies;

	public NotEyeLike(Board board) {
		this.board = board;
		final CoordinateSystem coords = board.getCoordinateSystem();
		edgeEnemies = new int[coords.getFirstPointBeyondBoard()];
		for (final short p : coords.getAllPointsOnBoard()) {
			edgeEnemies[p] = 0;
			for (int i = FIRST_DIAGONAL_NEIGHBOR; i <= LAST_DIAGONAL_NEIGHBOR; i++) {
				final short n = coords.getNeighbors(p)[i];
				if (!coords.isOnBoard(n)) {
					edgeEnemies[p] = 1;
				}
			}
		}
	}

	@Override
	public boolean at(short p) {
		assert board.getColorAt(p) == VACANT;
		final StoneColor color = board.getColorToPlay();
		if (!board.hasMaxNeighborsForColor(color, p)) {
			return true;
		}
		int count = edgeEnemies[p];
		final StoneColor enemy = color.opposite();
		final short[] neighbors = board.getCoordinateSystem().getNeighbors(p);
		for (int i = FIRST_DIAGONAL_NEIGHBOR; i <= LAST_DIAGONAL_NEIGHBOR; i++) {
			if (board.getColorAt(neighbors[i]) == enemy) {
				count++;
				if (count >= 2) {
					return true;
				}
			}
		}
		return false;
	}

}
