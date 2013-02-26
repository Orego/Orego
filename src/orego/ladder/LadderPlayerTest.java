package orego.ladder;
import orego.core.Coordinates;
import orego.ui.Orego;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.at;

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
		player.reset();
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
				"..................."//  1
	          // ABCDEFGHJKLMNOPQRST
		};
		player.setUpProblem(WHITE, diagram);
		player.getBoard().play(at("E13"));
		System.out.println(player.getBoard());
		System.out.println("Best move for black: " + Coordinates.pointToString(player.bestMove()));
	}
}
