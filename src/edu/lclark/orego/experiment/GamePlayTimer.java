package edu.lclark.orego.experiment;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.CopiableStructureFactory;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.SimpleSearchNodeBuilder;
import edu.lclark.orego.mcts.SimpleTreeUpdater;
import edu.lclark.orego.mcts.TranspositionTable;
import edu.lclark.orego.mcts.UctDescender;

public class GamePlayTimer {

	public static void main(String[] args) {
		final int milliseconds = 1000;
		final int threads = 1;
		Player player1 = new Player(threads, CopiableStructureFactory.useWithPriors(19, 7.5));
		Player player2 = new Player(threads, CopiableStructureFactory.useWithPriors(19, 7.5));
		Board board1 = player1.getBoard();
		Board board2 = player2.getBoard();
		CoordinateSystem coords = board1.getCoordinateSystem();
		TranspositionTable table1 = new TranspositionTable(1024, new SimpleSearchNodeBuilder(coords),
				coords);
		TranspositionTable table2 = new TranspositionTable(1024, new SimpleSearchNodeBuilder(coords),
				coords);
		player1.setTreeDescender(new UctDescender(board1, table1, 1000));
		player2.setTreeDescender(new UctDescender(board2, table2, 1));
		SimpleTreeUpdater updater1 = new SimpleTreeUpdater(board1, table1, 0);
		SimpleTreeUpdater updater2 = new SimpleTreeUpdater(board2, table2, 0);
		player1.setTreeUpdater(updater1);
		player2.setTreeUpdater(updater2);
		player1.setMsecPerMove(milliseconds);
		player2.setMsecPerMove(milliseconds);
		player1.clear();
		player2.clear();
		long runs1 = 0;
		long runs2 = 0;
		for (int i = 0; i < 30; i++) {
			short move = player1.bestMove();
			player1.acceptMove(move);
			player2.acceptMove(move);
			move = player2.bestMove();
			player1.acceptMove(move);
			player2.acceptMove(move);
			if(i == 1){
				runs1 -= player1.getMcRunnable(0).getPlayoutsCompleted();
				runs2 -= player1.getMcRunnable(0).getPlayoutsCompleted();
			}
		}
		runs1 += player1.getMcRunnable(0).getPlayoutsCompleted();
		runs2 += player2.getMcRunnable(0).getPlayoutsCompleted();
		System.out.println("Player 1: " + runs1 / 28.0 + "playouts per second");
		System.out.println("Player 2: " + runs2 / 28.0 + "playouts per second");
		// This kills the thread executor inside the player
		System.exit(0);
	}

}
