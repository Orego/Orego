package orego.ladder;
import orego.ui.Orego;

import static orego.core.Colors.BLACK;

import org.junit.Before;
import org.junit.Test;

public class LadderPlayerTest {
	private LadderPlayer player;
	private Orego game;
	
	@Before
	public void setUp() {
		player = new LadderPlayer();
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
		
		System.out.println(player.getBoard());
		
		player.getBoard().setUpProblem(BLACK, problem);
		//System.err.println(player.bestMove());
		//game.handleCommand("genmove black");
	}

}
