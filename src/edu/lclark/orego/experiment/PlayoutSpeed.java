package edu.lclark.orego.experiment;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.*;

/** Tests the speed of playouts in one thread. */
public final class PlayoutSpeed {

	public static void main(String[] args) {
		final int milliseconds = 10000;
		final int threads = 1;
		Player player = new Player(threads, CopiableStructureFactory.useWithPriors(19, 7.5));
		Board board = player.getBoard();
		CoordinateSystem coords = board.getCoordinateSystem();
		TranspositionTable table = new TranspositionTable(new SimpleSearchNodeBuilder(coords), coords);
		player.setTreeDescender(new UctDescender(board, table, 75));
		SimpleTreeUpdater updater = new SimpleTreeUpdater(board, table, 0);
		player.setTreeUpdater(updater);
		player.setMsecPerMove(milliseconds);
		player.clear();
		player.bestMove();
		System.out.println(table.dagSize(updater.getRoot()) + " tree nodes");
//		System.out.println(updater.getRoot().deepToString(board, table, 3));
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
