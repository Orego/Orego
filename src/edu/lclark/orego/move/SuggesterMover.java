package edu.lclark.orego.move;

import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.Suggester;
import edu.lclark.orego.util.ShortSet;

/** This tries to play a move suggested by some suggester. */
@SuppressWarnings("serial")
public final class SuggesterMover implements Mover {

	private final Board board;

	private final Suggester suggester;

	private final Mover fallbackMover;

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
		ShortSet suggestedMoves = suggester.getMoves();
		if (suggestedMoves.size() > 0) {
			short start = (short) (random.nextInt(suggestedMoves.size()));
			short i = start;
			short skip = PRIMES[random.nextInt(PRIMES.length)];
			do {
				short p = suggestedMoves.get(i);
				assert board.getColorAt(p) == VACANT;
				if (board.playFast(p) == OK) {
					return p;
				}
				// Advancing by a random prime skips through the array
				// in a manner analogous to double hashing.
				i = (short) ((i + skip) % suggestedMoves.size());
			} while (i != start);
		}
		return fallbackMover.selectAndPlayOneMove(random);
	}

}
