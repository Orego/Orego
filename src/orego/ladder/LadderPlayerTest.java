package orego.ladder;
import orego.ui.Orego;

import static orego.core.Colors.BLACK;

import org.junit.Before;
import org.junit.Test;

public class LadderPlayerTest {
	private Orego game;
	
	@Before
	public void setUp() {
		String args[] = new String[0];
		game = new Orego(args);
	}
	@Test
	public void testPlayLadder() {
		String[] problem = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				".....O.............",// 15
				"....O#O............",// 14
				"....O..............",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"..#................",// 8
				".#O#...............",// 7
				".#.................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				".....O.............",// 2
				"..................."// 1
	          // ABCDEFGHJKLMNOPQRST
		};
		
		game.getPlayer().getBoard().setUpProblem(BLACK, problem);
		game.handleCommand("genmove black");
	}

}
