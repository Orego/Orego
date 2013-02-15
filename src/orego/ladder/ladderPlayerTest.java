package orego.ladder;
import orego.core.*;
import orego.play.*;
import orego.ui.Orego;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

public class ladderPlayerTest {
	private Orego game;
	private Board board;
	private Player bl_player;
	private Player wh_player;
	
	@Before
	public void setUp(){
		String args[]=new String[0];
		game=new Orego(args);
		board=new Board();
	}
	@Test
	public void testPlayLadder() {
		String[] problem = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				".....OO............",// 15
				"....O#O............",// 14
				"....O#O............",// 13
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
		game.getPlayer().getBoard().setUpProblem(BLACK, problem);
		
		//game.handleCommand("showboard");
		game.handleCommand("genmove black");
		//assertEquals()
	}

}
