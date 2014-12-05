package edu.lclark.orego.experiment;

import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.PlayerBuilder;

/** Tests the speed of playouts in one thread. */
public final class PlayoutSpeed {

	public static void main(String[] args) {
		final int threads = 1;
		final int msec = 10000;
		final Player player = new PlayerBuilder().threads(threads)
				.msecPerMove(msec).openingBook(false)
				.shape(true)
				.shapeScalingFactor(0.999f).shapeBias(10).shapeMinStones(8)
//				.liveShape(true)
				.build();
		player.bestMove();
		long runs = 0;
		for (int i = 0; i < threads; i++) {
			runs += player.getMcRunnable(i).getPlayoutsCompleted();
		}
		System.out.println("Runs: " + runs);
		System.out.println((double) runs / msec + " kpps");
		// This kills the thread executor inside the player
		System.exit(0);
	}

}
