package edu.lclark.orego.move;

import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.Suggester;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;

/** This tries to play a move suggested by some suggester. */
@SuppressWarnings("serial")
public final class SuggesterMover implements Mover {

	private final Board board;

	/** If suggester doesn't suggest anything, fall back to this. */
	private final Mover fallbackMover;

	private final Suggester suggester;

	/**
	 * @param fallbackMover
	 *            If the suggester cannot find a move, fallbackMover is asked
	 *            for a move.
	 */
	public SuggesterMover(Board board, Suggester suggester, Mover fallbackMover) {
		this.board = board;
		this.suggester = suggester;
		this.fallbackMover = fallbackMover;
	}

	@Override
	public short selectAndPlayOneMove(MersenneTwisterFast random) {
		final ShortSet suggestedMoves = suggester.getMoves();
		assert !suggestedMoves.contains(edu.lclark.orego.core.CoordinateSystem.PASS) : suggester.getClass();
		if (suggestedMoves.size() > 0) {
			final short start = (short) random.nextInt(suggestedMoves.size());
			short i = start;
			final short skip = PRIMES[random.nextInt(PRIMES.length)];
			do {
				final short p = suggestedMoves.get(i);
				assert board.getColorAt(p) == VACANT;
				if (board.playFast(p) == OK) {
					return p;
				}
				// Advancing by a random prime skips through the array
				// in a manner analogous to double hashing.
				i = (short) ((i + skip) % suggestedMoves.size());
			} while (i != start);
		}
//		assert false : fallbackMover.getClass();
		return fallbackMover.selectAndPlayOneMove(random);
	}

}
