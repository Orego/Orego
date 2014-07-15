package edu.lclark.orego.score;

import static edu.lclark.orego.core.CoordinateSystem.FIRST_ORTHOGONAL_NEIGHBOR;
import static edu.lclark.orego.core.CoordinateSystem.LAST_ORTHOGONAL_NEIGHBOR;
import static edu.lclark.orego.core.NonStoneColor.OFF_BOARD;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;

/**
 * Scores using Chinese rules (area scoring). Assumes that everything on the
 * board is alive.
 */
@SuppressWarnings("serial")
public final class ChineseFinalScorer implements FinalScorer {

	private final Board board;

	/**
	 * The color of the territory currently being explored, or VACANT if no
	 * neighboring stones have yet been found.
	 */
	private Color colorToScore;

	private final CoordinateSystem coords;

	/**
	 * The amount of komi that white gets. For speed this is stored as a
	 * negative number
	 */
	private final double komi;

	/**
	 * True if the territory currently being explored is potentially valid,
	 * i.e., does not have neighboring stones of two different colors.
	 */
	private boolean validTerritory;

	/**
	 * Used in recursive depth-first search of territories.
	 */
	private final ShortSet visitedPoints;

	public ChineseFinalScorer(Board board, double komi) {
		this.board = board;
		this.komi = -komi;
		coords = board.getCoordinateSystem();
		visitedPoints = new ShortSet(coords.getFirstPointBeyondBoard());
	}

	@Override
	public double getKomi() {
		return -komi;
	}

	@Override
	public double score() {
		return score(board);
	}

	/**
	 * Searches for the contiguous territory around p. Returns the number of
	 * vacant points in this territory. Also modifies visitedPoints,
	 * colorToScore, and validTerritory.
	 */
	private int searchNeighbors(short p, Board boardToScore) {
		int result = 1;
		final short[] neighbors = coords.getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			final short n = neighbors[i];
			final Color neighborColor = boardToScore.getColorAt(n);
			if (neighborColor == OFF_BOARD) {
				continue;
			}

			if (colorToScore == VACANT) {
				colorToScore = neighborColor;
			}
			if (neighborColor == VACANT) {
				if (!visitedPoints.contains(n)) {
					visitedPoints.add(n);
					result += searchNeighbors(n, boardToScore);
				}
			} else if (neighborColor == colorToScore) {
				continue;
			} else {
				validTerritory = false;
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

	@Override
	public double score(Board boardToScore) {
		double result = komi;
		visitedPoints.clear();
		for (final short p : coords.getAllPointsOnBoard()) {
			final Color color = boardToScore.getColorAt(p);
			if (color == BLACK) {
				result++;
			} else if (color == WHITE) {
				result--;
			}

		}
		final ShortSet vacantPoints = boardToScore.getVacantPoints();
		for (int i = 0; i < vacantPoints.size(); i++) {
			final short p = vacantPoints.get(i);
			if (visitedPoints.contains(p)) {
				continue;
			}
			colorToScore = VACANT;
			validTerritory = true;
			visitedPoints.add(p);
			final int territory = searchNeighbors(p, boardToScore);
			if (validTerritory) {
				if (colorToScore == WHITE) {
					result -= territory;
				} else {
					result += territory;
				}
			}
		}
		return result;
	}

}
