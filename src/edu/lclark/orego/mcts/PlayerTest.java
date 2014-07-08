package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.StoneColor.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.CoordinateSystem.RESIGN;

public class PlayerTest {

	private Player player;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return player.getBoard().getCoordinateSystem().at(label);
	}

	@Before
	public void setUp() throws Exception {
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(5).build();
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
			// This move should not be chosen as it is eyelike for black
			assertNotEquals(at("a5"), move);
		}
	}
	
	@Test
	public void testResign() {
		String[] before = {
				".##OO",
				"##OO.",
				"##O.O",
				".#OO.",
				".##OO",
		};
		player.clear();
		player.getBoard().setUpProblem(before, BLACK);
		short move = player.bestMove();
		// Black is doomed -- DOOMED! -- and therefore should resign
		assertEquals(RESIGN, move);
	}

}
