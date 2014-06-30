package edu.lclark.orego.score;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;

@SuppressWarnings("serial")
public final class ChinesePlayoutScorer implements PlayoutScorer {

	private Board board;

	/**
	 * The amount of komi that white gets. For speed this is stored as a
	 * negative number
	 */
	private double komi;

	@Override
	public double getKomi() {
		return -komi;
	}

	public ChinesePlayoutScorer(Board board, double komi) {
		this.board = board;
		this.komi = -komi;
	}

	@Override
	public double score() {
		CoordinateSystem coords = board.getCoordinateSystem();
		double result = komi;
		for (short p : coords.getAllPointsOnBoard()) {
			Color color = board.getColorAt(p);
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
		double score = score();
		if (score > 0) {
			return BLACK;
		} else if (score < 0) {
			return WHITE;
		}
		return VACANT;
	}

}
