package orego.ladder;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import orego.core.Coordinates;
import orego.mcts.Lgrf2Player;

import org.junit.Before;
import org.junit.Test;

public class LadderPlayerTest {
	private LadderPlayer player;
	private Lgrf2Player lgrfPlayer;

	@Before
	public void setUp() {
		player = new LadderPlayer();
		lgrfPlayer = new Lgrf2Player();
	}

	/** Assert that LadderPlayer wants to play a winning ladder. */
	@Test
	public void testWinningLadder() {
		String[] diagram = {
				"...................",// 19
				".....OOOOO.O.......",// 18
				"...OO#####O........",// 17
				"..O########O.......",// 16
				"..OOO#OOOOOO.......",// 15
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
		
		lgrfPlayer.reset();
		lgrfPlayer.setUpProblem(WHITE, diagram);
		lgrfPlayer.getBoard().play(at("E13"));
//		assertTrue(lgrfPlayer.bestMove() == at("F13"));
	}

	/** Assert that LadderPlayer does not want to play a losing ladder. */
	@Test
	public void testLosingLadder() {		
		String[] diagram = {
				"...................",// 19
				".....OOOOO.O.......",// 18
				"...OO#####O........",// 17
				"..O########O.......",// 16
				"..OOO#OOOOOO.......",// 15
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
		
		lgrfPlayer.reset();
		lgrfPlayer.setUpProblem(WHITE, diagram);
		lgrfPlayer.getBoard().play(at("E13"));
//		assertTrue(lgrfPlayer.bestMove() == at("F13"));
	}
	
	
	/** This test only works when the ladder biasing is larger than 10*length because the ladder is fairly short 
	 * Tests that black does want to play out this ladder because it will make one of the outside stones be in atari. */
	@Test
	public void testStoneInAtari() {		
		String[] diagram = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				".....O.............",// 15
				"....O#O............",// 14
				"...................",// 13
				"...................",// 12
				".....#.............",// 11
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
		assertTrue(player.bestMove() == at("F13"));
	}
	
	@Test
	public void testOtherStoneInAtari() {		
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
				"............#......",// 9
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
		assertTrue(player.bestMove() == at("F13"));
	}
	
	@Test
	public void testPlaysSuicide() {		
		String[] diagram = {
				"...................",// 19
				"...................",// 18
				"..................#",// 17
				"................##.",// 16
				"...............#OO.",// 15
				"................##.",// 14
				"..................#",// 13
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
		assertTrue(player.bestMove() != at("T15"));

	}
	
	
}
