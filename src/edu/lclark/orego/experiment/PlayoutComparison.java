package edu.lclark.orego.experiment;

import static edu.lclark.orego.core.NonStoneColor.OFF_BOARD;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.move.MoverFactory.escapeCapturer;
import static edu.lclark.orego.move.MoverFactory.escapePatternCapturer;

import java.util.HashMap;
import java.util.Map;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.AtariObserver;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.StoneCountObserver;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.score.ChinesePlayoutScorer;
import edu.lclark.orego.score.PlayoutScorer;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

/**
 * Runs two Movers against each other and reports the win rates for each. Speed
 * is ignored.
 */
public final class PlayoutComparison {

	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		final Board board = new Board(19);
		final AtariObserver atariObserver = new AtariObserver(board);
		final HistoryObserver historyObserver = new HistoryObserver(board);
		final Mover mover1 = escapePatternCapturer(board, atariObserver,
				historyObserver);
		final Mover mover2 = escapeCapturer(board, atariObserver);
		final Map<Mover, Integer> wins = new HashMap<>();
		wins.put(mover1, 0);
		wins.put(mover2, 0);
		final PlayoutScorer scorer = new ChinesePlayoutScorer(board, 7.5);
		final StoneCountObserver mercyObserver = new StoneCountObserver(board, scorer);
		playGames(mover1, mover2, wins, board, scorer, mercyObserver);
		playGames(mover2, mover1, wins, board, scorer, mercyObserver);
		System.out.println("Version 1 wins: " + wins.get(mover1));
		System.out.println("Version 2 wins: " + wins.get(mover2));
	}

	@SuppressWarnings("boxing")
	private static void playGames(Mover black, Mover white,
			Map<Mover, Integer> wins, Board board, PlayoutScorer scorer,
			StoneCountObserver mercyObserver) {
		final MersenneTwisterFast random = new MersenneTwisterFast();
		final int runs = 100000;
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
						black.selectAndPlayOneMove(random, true);
					} else {
						white.selectAndPlayOneMove(random, true);
					}
				}
				if (board.getPasses() >= 2) {
					// Game ended
					winner = scorer.winner();
					break;
				}
				final Color mercyWinner = mercyObserver.mercyWinner();
				if (mercyWinner != null) {
					// One player has far more stones on the board
					winner = mercyWinner;
					break;
				}
			} while (true);
			if (winner == BLACK) {
				wins.put(black, wins.get(black) + 1);
			} else if (winner == WHITE) {
				wins.put(white, wins.get(white) + 1);
			}
			assert winner != OFF_BOARD;
		}
	}

}
