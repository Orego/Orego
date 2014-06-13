package edu.lclark.orego.experiment;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.StoneCounter;
import edu.lclark.orego.mcts.*;
import edu.lclark.orego.move.MoverFactory;
import edu.lclark.orego.score.ChinesePlayoutScorer;

/** Tests the speed of playouts in one thread. */
public final class PlayoutSpeed {

	public static void main(String[] args) {
		final int milliseconds = 10000;
		final int threads = 1;
		Board board = new Board(19);
		CopiableStructure stuff = new CopiableStructure(
				board,
				MoverFactory.feasible(board),
				new ChinesePlayoutScorer(board, 7.5),
				new StoneCounter(board)
				);
		Player player = new StubPlayer(threads, stuff);
		player.setMillisecondsPerMove(milliseconds);
		player.bestMove();
		long runs = 0;
		for (int i = 0; i < threads; i++) {
			runs += player.getMcRunnable(i).getPlayoutsCompleted();
		}
		System.out.println("Runs: " + runs);
		System.out.println((((double)runs) / milliseconds) + " kpps");
		// This kills the thread executor inside the player
		System.exit(0);
	}

}
