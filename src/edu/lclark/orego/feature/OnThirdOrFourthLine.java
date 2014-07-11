package edu.lclark.orego.feature;

import static edu.lclark.orego.core.CoordinateSystem.MAX_POSSIBLE_BOARD_WIDTH;
import static java.lang.Math.min;
import edu.lclark.orego.core.CoordinateSystem;

/** True if p is on the third or fourth line. */
@SuppressWarnings("serial")
public final class OnThirdOrFourthLine implements Predicate {

	/** Instances for various board widths. */
	private static final OnThirdOrFourthLine[] INSTANCES = new OnThirdOrFourthLine[MAX_POSSIBLE_BOARD_WIDTH + 1];

	/** Returns the unique OnThirdOrFourthLine for the width of board. */
	public static OnThirdOrFourthLine forWidth(int width) {
		final CoordinateSystem coords = CoordinateSystem.forWidth(width);
		if (INSTANCES[width] == null) {
			INSTANCES[width] = new OnThirdOrFourthLine(coords);
		}
		return INSTANCES[width];
	}

	/**
	 * Returns p's line (1-based) from the edge of the board
	 */
	private static int line(short p, CoordinateSystem coords) {
		int r = coords.row(p);
		r = min(r, coords.getWidth() - r - 1);
		int c = coords.column(p);
		c = min(c, coords.getWidth() - c - 1);
		return 1 + min(r, c);
	}

	/** True for points on third or fourth line. */
	private final boolean[] bits;

	private final int width;

	private OnThirdOrFourthLine(CoordinateSystem coords) {
		width = coords.getWidth();
		bits = new boolean[coords.getFirstPointBeyondBoard()];
		for (final short p : coords.getAllPointsOnBoard()) {
			final int line = line(p, coords);
			if (line == 3 | line == 4) {
				bits[p] = true;
			}
		}
	}

	@Override
	public boolean at(short p) {
		return bits[p];
	}

	/**
	 * Used so that serialization, as used in CopiableStructure, does not create
	 * redundant OnThirdOrFourthLine objects.
	 *
	 * @see edu.lclark.orego.mcts.CopiableStructure
	 */
	private Object readResolve() {
		return forWidth(width);
	}

}
