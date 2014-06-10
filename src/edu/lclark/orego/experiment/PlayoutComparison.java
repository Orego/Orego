package edu.lclark.orego.experiment;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.move.*;
import edu.lclark.orego.score.ChinesePlayoutScorer;
import edu.lclark.orego.score.Scorer;

import java.util.*;

/** Runs two playout policies against each other and reports the win rates for each. Speed is ignored. */
public class PlayoutComparison {

	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		MersenneTwisterFast random = new MersenneTwisterFast();
		Board original = new Board(19);
		Board copy = new Board(19);
		Scorer scorer = new ChinesePlayoutScorer(copy, 7.5);
		// Movers attached to original ensure that it has the correct observers
		MoverFactory.aladdin(original);
		MoverFactory.opportunist(original);
		Mover mover1 = MoverFactory.aladdin(copy);
		Mover mover2 = MoverFactory.opportunist(copy);
		Map<Mover, Integer> wins = new HashMap<>();
		wins.put(mover1, 0);
		wins.put(mover2, 0);
		playGames(random, original, copy, scorer, mover1, mover2, wins);
		playGames(random, original, copy, scorer, mover2, mover1, wins);
		System.out.println("Version 1 wins: " + wins.get(mover1));
		System.out.println("Version 2 wins: " + wins.get(mover2));
	}

	@SuppressWarnings("boxing")
	private static void playGames(MersenneTwisterFast random, Board original, Board copy,
			Scorer scorer, Mover mover1, Mover mover2,
			Map<Mover, Integer> wins) {	
		final int runs = 100000;
		for (int run = 0; run < runs; run++) {
			copy.copyDataFrom(original);
			do {
				if (copy.getColorToPlay() == BLACK) {
					mover1.selectAndPlayOneMove(random);
				} else {
					mover2.selectAndPlayOneMove(random);
				}
			} while (copy.getPasses() < 2);
			Color winner = scorer.winner();
			if (winner == BLACK) {
				wins.put(mover1, wins.get(mover1) + 1);
			} else if (winner == WHITE) {
				wins.put(mover2, wins.get(mover2) + 1);
			}
		}
	}

}
