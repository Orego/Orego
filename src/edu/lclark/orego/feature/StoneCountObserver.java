package edu.lclark.orego.feature;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.score.Scorer;
import edu.lclark.orego.util.ShortList;

/** Keeps track of how many stones there are of each color. */
@SuppressWarnings("serial")
public final class StoneCountObserver implements BoardObserver {

	private final int[] counts;

	/** If black stones - white stones >= this, black can be declared the winner. */
	private final int blackMercyThreshold;

	/** If black stones - white stones <= this, white can be declared the winner. */
	private final int whiteMercyThreshold;

	public StoneCountObserver(Board board, Scorer scorer) {
		counts = new int[2];
		final double komi = scorer.getKomi();
		final int base = Math.max(board.getCoordinateSystem().getArea() / 6, (int)(2 * komi));
		blackMercyThreshold = base + (int)(Math.ceil(komi));
		whiteMercyThreshold = -base + (int)(Math.floor(komi));
		board.addObserver(this);
	}

	@Override
	public void clear() {
		counts[0] = 0;
		counts[1] = 0;
	}

	@Override
	public void copyDataFrom(BoardObserver that) {
		final StoneCountObserver original = (StoneCountObserver) that;
		counts[0] = original.counts[0];
		counts[1] = original.counts[1];
	}

	/** Returns the number of stones of this color. */
	public int getCount(StoneColor color) {
		return counts[color.index()];
	}

	/**
	 * Returns the color, if any, with far more stones on the board than the
	 * other color. If there is no such color, returns null.
	 */
	public StoneColor mercyWinner() {
		final int difference = counts[BLACK.index()] - counts[WHITE.index()];
		if (difference >= blackMercyThreshold) {
			return BLACK;
		} else if (difference <= whiteMercyThreshold) {
			return WHITE;
		}
		return null;
	}

	@Override
	public void update(StoneColor color, short location,
			ShortList capturedStones) {
		if (location != PASS) {
			counts[color.index()]++;
			counts[color.opposite().index()] -= capturedStones.size();
		}
	}

}
