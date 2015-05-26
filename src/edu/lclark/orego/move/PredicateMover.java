package edu.lclark.orego.move;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.feature.Predicate;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;

/**
 * Makes random moves that satisfy some predicate.
 */
@SuppressWarnings("serial")
public final class PredicateMover implements Mover {

	private final Board board;

	private final Predicate filter;

	/**
	 * @param filter
	 *            Only moves satisfying filter will be considered.
	 */
	public PredicateMover(Board board, Predicate filter) {
		this.board = board;
		this.filter = filter;
	}
	
	@Override
	public short selectAndPlayOneMove(MersenneTwisterFast random, boolean fast) {
		final ShortSet vacantPoints = board.getVacantPoints();
		final short start = (short) random.nextInt(vacantPoints.size());
		short i = start;
		final short skip = PRIMES[random.nextInt(PRIMES.length)];
		do {
			final short p = vacantPoints.get(i);
			if (board.getColorAt(p) == VACANT && filter.at(p)) {
				Legality legality = fast ? board.playFast(p) : board.play(p);
				if (legality == OK) {
					return p;
				}
			}
			// Advancing by a random prime skips through the array
			// in a manner analogous to double hashing.
			i = (short) ((i + skip) % vacantPoints.size());
		} while (i != start);
		board.pass();
		return PASS;
	}

}
