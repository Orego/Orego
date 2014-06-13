package edu.lclark.orego.experiment;

import edu.lclark.orego.feature.StoneCounter;
import edu.lclark.orego.mcts.*;

/** Tests the speed of playouts in one thread. */
public final class PlayoutSpeed {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		final int milliseconds = 10000;
		final int threads = 1;
		Player player = new StubPlayer(19, threads);
		new StoneCounter(player.getBoard());
		player.setMillisecondsPerMove(milliseconds);
		player.bestMove();
		long runs = 0;
		for (int i = 0; i < threads; i++) {
			runs += player.getMcRunnable(i).getPlayoutsCompleted();
		}
		System.out.println("Runs: " + runs);
		System.out.println((((double)runs) / milliseconds) + " kpps");
	}

}
