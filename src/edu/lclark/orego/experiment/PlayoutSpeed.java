package edu.lclark.orego.experiment;

import edu.lclark.orego.mcts.*;
import edu.lclark.orego.move.CopiableStructureFactory;

/** Tests the speed of playouts in one thread. */
public final class PlayoutSpeed {

	public static void main(String[] args) {
		final int milliseconds = 10000;
		final int threads = 1;
		Player player = new StubPlayer(threads, CopiableStructureFactory.feasible(19));
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
