package orego.ladder;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class LadderPlayerTest {
	private LadderPlayer player;

	@Before
	public void setUp() {
		player = new LadderPlayer();
	}

	@Test
	public void testPlayLadder() {
		String[] diagram1 = {
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
		String[] diagram2 = {
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
		};
		
		double tries = 20.0;

		int correct1 = 0;
		for (int i = 0; i < tries; i++) {
			player.reset();
			player.setUpProblem(WHITE, diagram1);
			player.getBoard().play(at("E13"));
			if (pointToString(player.bestMove()).equals("F13")) {
				correct1++;
			}
		}
		assertTrue(correct1 / tries > 0.5);
		
		int correct2 = 0;
		for (int i = 0; i < tries; i++) {
			player.reset();
			player.setUpProblem(WHITE, diagram2);
			player.getBoard().play(at("E13"));
			if (!pointToString(player.bestMove()).equals("F13")) {
				correct2++;
			}
		}
		assertTrue(correct2 / tries > 0.9);
	}
}
