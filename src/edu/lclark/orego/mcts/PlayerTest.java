package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.StoneColor.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class PlayerTest {

	private Player player;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return player.getBoard().getCoordinateSystem().at(label);
	}

	/** Delegate method. */
	private String toString(short p) {
		return player.getBoard().getCoordinateSystem().toString(p);
	}

	@Before
	public void setUp() throws Exception {
		// TODO This is an awful lot of work, done (e.g.) here and in PlayoutSpeed. Encapsulate!
		final int milliseconds = 100;
		final int threads = 4;
		player = new Player(threads, CopiableStructureFactory.feasible(5));
		Board board = player.getBoard();
		CoordinateSystem coords = board.getCoordinateSystem();
		TranspositionTable table = new TranspositionTable(1024 * 1024, new SimpleSearchNodeBuilder(coords), coords);
		player.setTreeDescender(new UctDescender(board, table));
		SimpleTreeUpdater updater = new SimpleTreeUpdater(board, table);
		player.setTreeUpdater(updater);
		player.setMillisecondsPerMove(milliseconds);
	}

	@Test
	public void test1() {
		String[] before = {
				".##OO",
				".#OO.",
				".#O..",
				".#OO.",
				".##OO",
		};
		player.getBoard().setUpProblem(before, BLACK);
		short move = player.bestMove();
		assertEquals(at("e3"), move);
	}

	@Test
	public void testFilter() {
		String[] before = {
				".##OO",
				"##OO.",
				"##O.O",
				".#OO.",
				".##OO",
		};
		for (int i = 0; i < 20; i++) {
			player.clear();
			player.getBoard().setUpProblem(before, BLACK);
			short move = player.bestMove();
			System.out.println(toString(move));
			// This move should not be chosen as it is eyelike for black
			assertNotEquals(at("a5"), move);
		}
	}

}
