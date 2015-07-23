package edu.lclark.orego.move;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.feature.Predicate;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortList;

/**
 * Makes random moves that satisfy some predicate.
 */
@SuppressWarnings("serial")
public final class PredicateMover implements Mover {

	private final Board board;

	private final ShortList candidates;

	private final Predicate filter;

	/**
	 * @param filter
	 *            Only moves satisfying filter will be considered.
	 */
	public PredicateMover(Board board, Predicate filter) {
		this.board = board;
		this.filter = filter;
		candidates = new ShortList(board.getCoordinateSystem().getArea());
	}
	
	@Override
	public short selectAndPlayOneMove(MersenneTwisterFast random, boolean fast) {
		candidates.clear();
		candidates.addAll(board.getVacantPoints());
		while (candidates.size() > 0) {
			final short p = candidates.removeRandom(random);
			if (board.getColorAt(p) == VACANT && filter.at(p)) {
				Legality legality = fast ? board.playFast(p) : board.play(p);
				if (legality == OK) {
					return p;
				}
			}	
		} 
		board.pass();
		return PASS;
	}

}
