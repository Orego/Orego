package edu.lclark.orego.experiment;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.feature.Conjunction;
import edu.lclark.orego.feature.Disjunction;
import edu.lclark.orego.feature.Feature;
import edu.lclark.orego.feature.NearAnotherStone;
import edu.lclark.orego.feature.NotEyeLike;
import edu.lclark.orego.feature.OnThirdOrFourthLine;
import edu.lclark.orego.move.SimpleRandom;
import edu.lclark.orego.score.ChinesePlayoutScorer;
import edu.lclark.orego.score.Scorer;
import java.util.*;

/** Runs two playout policies against each other and reports the win rates for each. */
public class PlayoutComparison {

	public static void main(String[] args) {
		MersenneTwisterFast random = new MersenneTwisterFast();
		Board board = new Board(19);
		Scorer scorer = new ChinesePlayoutScorer(board, 0);
		Feature f = new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board)));
		SimpleRandom mover1 = new SimpleRandom(board, f);
		SimpleRandom mover2 = new SimpleRandom(board, new NotEyeLike(board));
		Map<SimpleRandom, Integer> wins = new HashMap<>();
		wins.put(mover1, 0);
		wins.put(mover2, 0);
		playGames(random, board, scorer, mover1, mover2, wins);
		playGames(random, board, scorer, mover2, mover1, wins);
		System.out.println("Version 1 wins: " + wins.get(mover1));
		System.out.println("Version 2 wins: " + wins.get(mover2));
	}

	private static void playGames(MersenneTwisterFast random, Board board,
			Scorer scorer, SimpleRandom mover1, SimpleRandom mover2,
			Map<SimpleRandom, Integer> wins) {	
		final int runs = 100000;
		for (int run = 0; run < runs; run++) {
			board.clear();
			do {
				if (board.getColorToPlay() == BLACK) {
					mover1.selectAndPlayOneMove(random);
				} else {
					mover2.selectAndPlayOneMove(random);
				}
			} while (board.getPasses() < 2);
			Color winner = scorer.winner();
			if (winner == BLACK) {
				wins.put(mover1, wins.get(mover1) + 1);
			} else if (winner == WHITE) {
				wins.put(mover2, wins.get(mover2) + 1);
			}
		}
	}

}
