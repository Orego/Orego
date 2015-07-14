package edu.lclark.orego.experiment;

import static edu.lclark.orego.core.NonStoneColor.OFF_BOARD;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;

import java.util.HashMap;
import java.util.Map;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.StoneCountObserver;
import edu.lclark.orego.genetic.Pattern;
import edu.lclark.orego.genetic.Phenotype;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.score.ChinesePlayoutScorer;
import edu.lclark.orego.score.PlayoutScorer;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class PlayoutComparisonNetwork {

	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		final Board board = new Board(9);
		//TODO: mover1 should be new type of mover
		final Phenotype mover1 = new Phenotype(board);
//		mover1.randomizeBiases();
//		mover1.setBias(board.getCoordinateSystem().at("e5"), (byte) 127);
//		mover1.setBias(board.getCoordinateSystem().at("d5"), (byte) 126);
//		mover1.setBias(board.getCoordinateSystem().at("f5"), (byte) 126);
//		mover1.setBias(board.getCoordinateSystem().at("e3"), (byte) 126);
//		mover1.setBias(board.getCoordinateSystem().at("e4"), (byte) 126);
		//TODO: set info
//		final Phenotype mover2 = new Phenotype(board);
//		mover2.setBias(board.getCoordinateSystem().at("d5"), (byte) 127);
//		mover2.randomizeBiases();
		Pattern mover2 = new Pattern(board, new HistoryObserver(board));
		mover2.setPattern(new int[0]);
		final Map<Mover, Integer> wins = new HashMap<>();
		wins.put(mover1, 0);
		wins.put(mover2, 0);
		final PlayoutScorer scorer = new ChinesePlayoutScorer(board, 7.5);
		final StoneCountObserver mercyObserver = new StoneCountObserver(board, scorer);
		long before = System.nanoTime();
		playGames(mover1, mover2, wins, board, scorer, mercyObserver);
		long after = System.nanoTime();
		System.out.println(100/((after - before)/1000000000.0) + " games per second");
		playGames(mover2, mover1, wins, board, scorer, mercyObserver);
		System.out.println("Version 1 wins: " + wins.get(mover1));
		System.out.println("Version 2 wins: " + wins.get(mover2));
	}
	
	
	@SuppressWarnings("boxing")
	private static void playGames(Mover black, Mover white,
			Map<Mover, Integer> wins, Board board, PlayoutScorer scorer,
			StoneCountObserver mercyObserver) {
		final MersenneTwisterFast random = new MersenneTwisterFast();
		final int runs = 100;
		final CoordinateSystem coords = board.getCoordinateSystem();
		for (int run = 0; run < runs; run++) {
			board.clear();
			Color winner = OFF_BOARD;
			do {
				if (board.getTurn() >= coords.getMaxMovesPerGame()) {
					// Playout ran out of moves, probably due to superko
					winner = VACANT;
					break;
				}
				if (board.getPasses() < 2) {
					if (board.getColorToPlay() == BLACK) {
						short p = black.selectAndPlayOneMove(random, true);
//						System.out.println("BLACK " + board.getCoordinateSystem().toString(p));
					} else {
						short p = white.selectAndPlayOneMove(random, true);
//						System.out.println("WHITE " + board.getCoordinateSystem().toString(p));
					}
				}
//				System.out.println(board);
				if (board.getPasses() >= 2) {
					// Game ended
					winner = scorer.winner();
					break;
				}
				final Color mercyWinner = mercyObserver.mercyWinner();
				if (mercyWinner != null) {
					// One player has far more stones on the board
//					System.out.println("Mercy cutoff!");
					winner = mercyWinner;
					break;
				}
			} while (true);
//			System.out.println(winner);
			if (winner == BLACK) {
				wins.put(black, wins.get(black) + 1);
			} else if (winner == WHITE) {
				wins.put(white, wins.get(white) + 1);
			}
			assert winner != OFF_BOARD;
		}
	}
}
