package edu.lclark.orego.move;

import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.*;
import edu.lclark.orego.feature.Predicate;
import edu.lclark.orego.util.*;
import ec.util.MersenneTwisterFast;

/**
 * Makes random moves that satisfy some criterion.
 */
public final class PredicateMover implements Mover {

	private final Board board;
	
	private final Predicate filter;
	
	/**
	 * @param filter Only moves with this feature will be considered.
	 */
	public PredicateMover(Board board, Predicate filter) {
		this.board = board;
		this.filter = filter;
	}

	@Override
	public short selectAndPlayOneMove(
			MersenneTwisterFast random) {
		ShortSet vacantPoints = board.getVacantPoints();
		short start = (short)(random.nextInt(vacantPoints.size()));
		short i = start;
		short skip = PRIMES[random.nextInt(PRIMES.length)];
		do {
			short p = vacantPoints.get(i);
			if ((board.getColorAt(p) == VACANT) && filter.at(p)) {
				if (board.playFast(p) == OK) {
					return p;
				}
			}
			// Advancing by a random prime skips through the array
			// in a manner analogous to double hashing.
			i = (short)((i + skip) % vacantPoints.size());
		} while (i != start);
		board.pass();
		return PASS;
	}

}
