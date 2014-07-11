package edu.lclark.orego.score;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;

/**
 * Scores using Chinese rules (area scoring).
 */
@SuppressWarnings("serial")
public final class ChinesePlayoutScorer implements PlayoutScorer {

	private final Board board;

	/**
	 * The amount of komi that white gets. For speed this is stored as a
	 * negative number
	 */
	private final double komi;

	public ChinesePlayoutScorer(Board board, double komi) {
		this.board = board;
		this.komi = -komi;
	}

	@Override
	public double getKomi() {
		return -komi;
	}

	@Override
	public double score() {
		final CoordinateSystem coords = board.getCoordinateSystem();
		double result = komi;
		for (final short p : coords.getAllPointsOnBoard()) {
			final Color color = board.getColorAt(p);
			if (color == BLACK) {
				result++;
			} else if (color == WHITE) {
				result--;
			} else {
				if (board.hasMaxNeighborsForColor(BLACK, p)) {
					result++;
				} else if (board.hasMaxNeighborsForColor(WHITE, p)) {
					result--;
				}
			}
		}
		return result;
	}

	@Override
	public Color winner() {
		final double score = score();
		if (score > 0) {
			return BLACK;
		} else if (score < 0) {
			return WHITE;
		}
		return VACANT;
	}

}
