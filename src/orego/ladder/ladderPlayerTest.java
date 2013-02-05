package orego.ladder;
import orego.core.*;
import orego.play.*;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;


import org.junit.Test;

public class ladderPlayerTest {
	private Board board;
	private Player bl_player;
	private Player wh_player;
	@Test
	public void testPlayLadder() {
		String[] problem = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				".....OO............",// 15
				"....O#O............",// 14
				"....O..............",// 13
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
		//should build chain staring at f13
		board.setUpProblem(BLACK, problem);
		
		board.play("f13");

		assertEquals(BLACK, board.getColor(at("b1")));
		assertEquals(6, board.getLibertyCount(at("b2")));
	}

}
