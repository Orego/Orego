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

	@Test

	public void testPlayLadderWin() {
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
		
		
		player.reset();
		player.setUpProblem(WHITE, diagram1);
		player.getBoard().play(at("E13"));
		assertEquals(player.bestMove(),Coordinates.at("F13"));//blacks best move should be F13
		assertEquals((30*10)+1, player.getWinsFor(0));//win bias of 300
		assertEquals(player.getLadLengths(0),30);
		
		player.getBoard().play(at("A19"));//make black play somewhere random to change turn
		assertFalse(player.bestMove()==Coordinates.at("F13"));//white doesn't want to immediately capture?
		System.out.println(player.getRoot());
		assertEquals(1, player.getWinsFor(0));//no wins for white
	}
	@Test
	public void testPlayLadderLose(){
		
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
		};//ladder length=48
		
		player.reset();
		player.setUpProblem(WHITE, diagram2);
		player.getBoard().play(at("E13"));
		assertFalse(player.bestMove()==Coordinates.at("F13"));//blacks best move= anything but F13
		assertEquals(1, player.getWinsFor(0));//no wins for black (defaultVal=1)
		
		player.getBoard().play(at("A19"));//make black play somewhere away from ladder
		player.bestMove();
		assertEquals((48*10)+1,player.getWinsFor(0));//white has win bias of 480 for lad#1
		assertEquals(player.getLadLengths(0),48);
		
	}
}
