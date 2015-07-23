package edu.lclark.orego.move;

import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.feature.Suggester;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortList;

/** This tries to play a move suggested by some suggester. */
@SuppressWarnings("serial")
public final class SuggesterMover implements Mover {

	private final Board board;

	private final ShortList candidates;

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
		candidates = new ShortList(board.getCoordinateSystem().getArea());
	}

	@Override
	public short selectAndPlayOneMove(MersenneTwisterFast random, boolean fast) {
		candidates.clear();
		candidates.addAll(suggester.getMoves());
		while (candidates.size() > 0) {
			final short p = candidates.removeRandom(random);
			assert board.getColorAt(p) == VACANT;
			Legality legality = fast ? board.playFast(p) : board.play(p);
			if (legality == OK) {
				return p;
			}
		} 
		return fallbackMover.selectAndPlayOneMove(random, fast);
	}

}
