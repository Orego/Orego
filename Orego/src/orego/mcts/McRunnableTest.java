package orego.mcts;

import static orego.core.Colors.*;
import static orego.core.Board.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import orego.play.ThreadedPlayer;

import org.junit.Before;
import org.junit.Test;

public class McRunnableTest {

	private ThreadedPlayer player;
	
	private McRunnable runnable;
	
	@Before
	public void setUp() throws Exception {
		player = new MctsPlayer();
		player.reset();
		runnable = (McRunnable) player.getRunnable(0);
	}

	@Test
	public void testPlayoutAfterTwoPasses() {
		runnable.getBoard().setPasses(2);
		runnable.playout();
		assertEquals(0, runnable.getTurn());
		assertEquals(-(int) (runnable.getBoard().getKomi()), runnable
				.getBoard().playoutScore());
	}

	@Test
	public void testPlayoutMaxMoves() {
		for (int i = 0; i < MAX_MOVES_PER_GAME - 1; i++) {
			runnable.acceptMove(PASS);
			runnable.getBoard().setPasses(0);
		}
		// Playing out from here should run up against the max game length
		assertEquals(VACANT, runnable.playout());
	}
	
	@Test
	public void testDebug1() {
		if(BOARD_WIDTH == 19) {
			String[] problem = {
					"#########OOOOOOOOOO",//19
					"#########OOOOOOOOOO",//18
					"#########OOOOOOOOOO",//17
					"#########OOOOOOOOOO",//16
					"#########OOOOOOOOOO",//15
					"##########OOOOOOOOO",//14
					"##########OOOOOOOOO",//13
					"##########OOOOOOOOO",//12
					"##########OOOOOOOOO",//11
					"##########OOOOOOOOO",//10
					"..##.#..###########",//9
					"..#O.#.#.##########",//8
					"..#O##.##OOOOOOOOOO",//7
					"...#.##O########OOO",//6
					"..###OOOOOOOOO#####",//5
					"###OOOOOOOOO#######",//4
					"OOOO.O#############",//3
					"#OOOO##OOOOOOOOO###",//2
					".O###.#O.##########" //1
			       //ABCDEFGHJKLMNOPQRST
			};
			player.getBoard().setUpProblem(BLACK, problem);
			player.acceptMove(PASS);
			for (int i = 0; i < 10000; i++) {
				runnable.performMcRun();
			}
			assertEquals(10000, runnable.getPlayoutsCompleted());
		} else {
			// This position caused a crash during a game
			String[] problem = {
					"..##.#..#",
					"..#O.#.#.",
					"..#O##.##",
					"...#.##O#",
					"..###OOOO",
					"###OOOOOO",
					"OOOO.O###",
					"#OOOO##OO",
					".O###.#O.",
			};
			player.getBoard().setUpProblem(BLACK, problem);
			player.acceptMove(PASS);
			for (int i = 0; i < 10000; i++) {
				runnable.performMcRun();
			}
			assertEquals(10000, runnable.getPlayoutsCompleted());
		}
	}
	
	@Test
	public void testCaptureLargerGroup() {
		String[] problem = new String[] {
					"...................",//19
					"...................",//18
					"...................",//17
					"...................",//16
					"...................",//15
					"...................",//14
					"...................",//13
					"...................",//12
					"...................",//11
					"...................",//10
					"...................",//9
					"...................",//8
					"...................",//7
					"..O................",//6
					".OOO...............",//5
					"OO#O...............",//4
					"#.#O...............",//3
					"..O#O..............",//2
					"#O................."//1
				  // ABCDEFGHJKLMNOPQRST
				};
		player.setUpProblem(BLACK, problem);
		player.acceptMove(at("b2"));
		assertEquals(at("b3"), runnable.selectAndPlayOneMove(runnable.getRandom(), player.getBoard()));
	}

}
