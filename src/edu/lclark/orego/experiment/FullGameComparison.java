package edu.lclark.orego.experiment;

import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.PlayerBuilder;

/**
 * Plays a game between two Players. Prints out each's playouts per second.
 * Modify the code to change the particular players.
 */
public final class FullGameComparison {

	public static void main(String[] args) {
		// Modify this to change the players in question
		final Player player1 = new PlayerBuilder().threads(1).build();
		final Player player2 = new PlayerBuilder().threads(1).shape(true).shapeBias(20).shapeMinStones(9).shapeScalingFactor(.99f).build();
		long runs1 = 0;
		long runs2 = 0;
		for (int i = 0; i < 100; i++) {
			short move = player1.bestMove();
			player1.acceptMove(move);
			player2.acceptMove(move);
			move = player2.bestMove();
			player1.acceptMove(move);
			player2.acceptMove(move);
		}
		runs1 += player1.getMcRunnable(0).getPlayoutsCompleted();
		runs2 += player2.getMcRunnable(0).getPlayoutsCompleted();
		System.out.println("Player 1: " + runs1 / 100.0 + "playouts per second");
		System.out.println("Player 2: " + runs2 / 100.0 + "playouts per second");
		// This kills the thread executor inside the player
		System.exit(0);
	}

}
