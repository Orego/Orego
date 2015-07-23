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
		for (int s = 2; s <= 20; s++) {
			int[] counts = new int[s];
				for (int p : PRIMES) {
					counts[p % s]++;
				}
			System.out.println(s + ": " + java.util.Arrays.toString(counts));
		}
		System.exit(1);
		if (board.getTurn() == 0) {
			System.out.println(vacantPoints.size());
		}
		final short start = (short) random.nextInt(vacantPoints.size());
		short i = start;
		final short skip = PRIMES[random.nextInt(PRIMES.length)];
		do {
			final short p = vacantPoints.get(i);
//			if (board.getTurn() == 0) {
//				System.out.print(board.getCoordinateSystem().toString(p) + " ");
//			}
			if (board.getColorAt(p) == VACANT && filter.at(p)) {
				Legality legality = fast ? board.playFast(p) : board.play(p);
				if (legality == OK) {
//					if (board.getTurn() == 1) {
//						System.out.println();
//					}
					return p;
				}
			}
			// Advancing by a random prime skips through the array
			// in a manner analogous to double hashing.
			i = (short) ((i + skip) % vacantPoints.size());
		} while (i != start);
		board.pass();
//		if (board.getTurn() == 0) {
//			System.out.println("PASS");
//		}
		return PASS;
	}

}
