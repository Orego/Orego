package orego.ladder;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import orego.core.Coordinates;

import org.junit.Before;
import org.junit.Test;

public class LadderPlayerTest {
	private LadderPlayer player;

	@Before
	public void setUp() {
		player = new LadderPlayer();
	}

	/** Assert that LadderPlayer wants to play a winning ladder. */
	@Test
	public void testWinningLadder() {
		String[] diagram = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				".....O.............",// 15
				"....O#O............",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				".............#.....",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
			  // ABCDEFGHJKLMNOPQRST
		};
		
		player.reset();
		player.setUpProblem(WHITE, diagram);
		player.getBoard().play(at("E13"));
		assertTrue(player.bestMove() == at("F13"));
	}

	/** Assert that LadderPlayer does not want to play a losing ladder. */
	@Test
	public void testLosingLadder() {		
		String[] diagram = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				".....O.............",// 15
				"....O#O............",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
			  // ABCDEFGHJKLMNOPQRST
		}; // length is 48
		
		player.reset();
		player.setUpProblem(WHITE, diagram);
		player.getBoard().play(at("E13"));
		assertTrue(player.bestMove() != at("F13"));
	}
}
