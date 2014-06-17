package edu.lclark.orego.experiment;

import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.StoneCounter;
import edu.lclark.orego.move.MoverFactory;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.score.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;

/** Tests the speed of playouts in one thread. */
public final class PlayoutSpeed {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		MersenneTwisterFast random = new MersenneTwisterFast();
		Board original = new Board(19);
		Board copy = new Board(19);
		new StoneCounter(original); // This appears unused, but original knows about its observers
		StoneCounter mercyObserver = new StoneCounter(copy);
		Scorer scorer = new ChinesePlayoutScorer(copy, 7.5);
		// The first mover is created only to make any BoardObservers
		MoverFactory.escapeCapturer(original);
		Mover mover = MoverFactory.escapeCapturer(copy);
		final int runs = 100000;
		long total = 0;
		int[] wins = new int[3];
		int playoutLength = 0;
		for (int run = 0; run < runs; run++) {
			long before = System.nanoTime();
			copy.copyDataFrom(original);
			do {
				mover.selectAndPlayOneMove(random);
				playoutLength++;
			} while (copy.getPasses() < 2 && mercyObserver.mercyWinner()==null);
			wins[scorer.winner().index()]++;
			long after = System.nanoTime();
			total += (after - before);
		}
		System.out.println((runs / 1000.0) / (total / 1000000000.0) + " kpps");
		System.out.println("Black wins: " + wins[BLACK.index()]);
		System.out.println("White wins: " + wins[WHITE.index()]);
		System.out.println("Ties: " + wins[VACANT.index()]);
		System.out.println("Average playout length: " + playoutLength / runs);
	}

}
