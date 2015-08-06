package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static org.junit.Assert.*;
import static edu.lclark.orego.util.TestingTools.asOneString;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.CoordinateSystem.RESIGN;

public class PlayerTest {

	private Player player;
	
	private CoordinateSystem coords;

	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(5).memorySize(64)
				.openingBook(false).build();
		coords = player.getBoard().getCoordinateSystem();
	}

	@Test
	public void test1() {
		String[] before = {
				".##OO",
				".#OO.",
				".#O..",
				".#OO.",
				".##OO",
		};
		player.getBoard().setUpProblem(before, BLACK);
		short move = player.bestMove();
		assertEquals(at("e3"), move);
	}

	@Test
	public void testFilter() {
		String[] before = {
				".##OO",
				"##OO.",
				"##O.O",
				".#OO.",
				".##OO",
		};
		for (int i = 0; i < 20; i++) {
			player.clear();
			player.getBoard().setUpProblem(before, BLACK);
			short move = player.bestMove();
			// This move should not be chosen as it is eyelike for black
			assertNotEquals(at("a5"), move);
		}
	}

	@Test
	public void testResign() {
		String[] before = {
				".##OO",
				"##OO.",
				"##O.O",
				".#OO.",
				".##OO",
		};
		player.clear();
		player.getBoard().setUpProblem(before, BLACK);
		short move = player.bestMove();
		// Black is doomed -- DOOMED! -- and therefore should resign
		assertEquals(RESIGN, move);
	}

	@Test
	public void testUndo() {
		String[] before = {
				".....",
				"..O..",
				".....",
				"..#..",
				".....",
		};
		player.clear();
		assertFalse(player.undo());
		player.getBoard().setUpProblem(before, BLACK);
		player.acceptMove(at("e3"));
		long fancyHash = player.getBoard().getFancyHash();
		player.acceptMove(at("e1"));
		assertTrue(player.undo());
		assertEquals(fancyHash, player.getBoard().getFancyHash());
		String[] after = {
				".....",
				"..O..",
				"....#",
				"..#..",
				".....",
		};
		assertEquals(asOneString(after), player.getBoard().toString());
	}

	@Test
	public void testGetPlayouts() {
		player.clear();
		for (int i = 0; i < 5; i++) {
			player.getMcRunnable(0).performMcRun();
		}
		for (int i = 0; i < 8; i++) {
			player.getMcRunnable(2).performMcRun();
		}
		for (int i = 0; i < 2; i++) {
			player.getMcRunnable(3).performMcRun();
		}
		assertEquals(15, player.getPlayoutCount());
	}

	@Test
	public void testCleanup() {
		player = new PlayerBuilder().msecPerMove(100).threads(1).boardWidth(5).memorySize(64)
				.openingBook(false).komi(0).build();
		String[] before = {
				".##O#",
				"##OO.",
				"##O.O",
				".#OO.",
				".##OO",
		};
		player.clear();
		player.getBoard().setUpProblem(before, WHITE);
		player.setCleanupMode(true);
		short move = player.bestMove();
		assertEquals(at("e4"), move);
		player.acceptMove(move);
		player.acceptMove(at("a1"));
		move = player.bestMove();
		assertEquals(PASS, move);
	}

	// Coup De Grace tests use a 19x19 board because options are so limited on
	// smaller boards and regular MCTS works so well. This makes it hard to
	// distinguish whether our biasing is actually working on small boards.
	@Test
	public void testCoupDeGrace() {
		player = new PlayerBuilder().msecPerMove(100).threads(1).boardWidth(19).coupDeGrace(true)
				.memorySize(64).openingBook(false).build();
		coords = player.getBoard().getCoordinateSystem();
		String[] problem = new String[] {
				"..O.O.#..#O#######.",// 19
				".OO.O#####O#######.",// 18
				"O.O.O#.#.#O########",// 17
				".OOOO#####O##.###.#",// 16
				"OOOOOOOO##O#######.",// 15
				".O.....OOOO####....",// 14
				".OOOOOOO..O#####.#.",// 13
				"OO.O..O.OOO##.#.#..",// 12
				".OOO.O..O.O#.#.#.#.",// 11
				"..O.OOOOOOO#..##...",// 10
				".OOOO.O..#O#####.#.",// 9
				"...O.OO######.#.#..",// 8
				"OOOO.O.#OOO##....#.",// 7
				"..O.OO##O########..",// 6
				"..O.O.#OO#OOOO##.#.",// 5
				"..O.O.#O.#O.O.O#.#.",// 4
				".OO.O.#O##OOOOO##..",// 3
				"O.O.O.#OOO##O######",// 2
				"..O.O.##O.#.######." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		int successes = 0;
		int failures = 0;
		for (int i = 0; i < 10; i++) {
			player.clear();
			player.getBoard().setUpProblem(problem, BLACK);
			short move = player.bestMove(); // To generate some playouts
			player.setCleanupMode(true);
			move = player.bestMove();
			if (move == at("J4") || move == at("m1")) {
				successes++;
			} else if (move == at("K1")) {
				failures++;
			}
		}
		assertEquals(0, failures);
		assertTrue(successes >= 5);
	}

	@Test
	public void testDoNotCoupDeGraceTooEarly() {
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(19).coupDeGrace(true)
				.memorySize(64).openingBook(false).build();
		String[] problem = new String[] {
				"...................",// 19
				"...................",// 18
				"...#...........#...",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"...................",// 5
				"...#...........#...",// 4
				"...................",// 3
				"...................",// 2
				"..................." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		int failures = 0;
		for (int i = 0; i < 10; i++) {
			player.clear();
			player.getBoard().setUpProblem(problem, WHITE);
			player.acceptMove(at("b2"));
			player.bestMove(); // To generate some playouts
			int move = player.bestMove();
			if (move == at("a2") || move == at("b1")) {
				failures++;
			}
		}
		assertTrue(failures <= 2);
	}

	@Test
	public void testPassToWin() {
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(5).memorySize(64)
				.openingBook(false).komi(0).coupDeGrace(true).build();
		String[] diagram = {
				"###O#",
				"##OO.",
				"##OO.",
				".##OO",
				"#.#O.",
		};
		player.getBoard().setUpProblem(diagram, WHITE);
		player.acceptMove(PASS);
		assertEquals(PASS, player.bestMove());
	}

	@Test
	public void testDontPassIfBehind() {
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(5).memorySize(64)
				.openingBook(false).komi(0).build();
		String[] diagram = {
				"##O.#",
				"##OO.",
				"##OO.",
				".##OO",
				"#.#O.",
		};
		player.getBoard().setUpProblem(diagram, BLACK);
		assertFalse(player.canWinByPassing());
	}
	
	@Test
	public void testGetDeadStones(){
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(19).memorySize(64)
				.openingBook(false).komi(0).build();
		coords = player.getBoard().getCoordinateSystem();
		String[] diagram = {
				"...O....O.O#.......",
				"..OOO...O.O#..#....",
				".O..O.OOO.O##......",
				".OOO.OOOOOOO##.####",
				".OO.OO.O.OOO#####O.",
				"#O###OOOO...OOOOOO.",
				"##..##OOO..OOOOO.OO",
				"..#.#OO.OOO.OO..O..",
				"....##.OO..........",
				"...#.##O.O...OOO...",
				"...O#.#O......O.OOO",
				"....###OO...O..OO.O",
				"......##O.O...OOOO#",
				"....#.#OO.....O####",
				"..#..#O.O.O.OO#..#.",
				"...###O.O...O#.##..",
				"...O###O....OO#.#..",
				"......#OO.OOO##.#..",
				"......###.O#####...",
		};
		player.getBoard().setUpProblem(diagram, WHITE);
		ShortSet deadStones = player.findDeadStones(0.75, WHITE);
		assertEquals(2, deadStones.size());
		assertTrue(deadStones.contains(coords.at("D3")));
		assertTrue(deadStones.contains(coords.at("D9")));
	}
	
	@Test
	public void testGetDeadStones2(){
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(19).memorySize(64)
				.openingBook(false).komi(0).build();
		coords = player.getBoard().getCoordinateSystem();
		String[] diagram = {
				"..##.OOO...#.......",
				".###.##O...O.......",
				"..###.#O.....O.....",
				"..#..#OO....#..O...",
				"...###O.....O......",
				"..#.##O............",
				".###.#O..OO........",
				".#..##OOO##O.......",
				".####.##O.O........",
				".#...##OOO.....O...",
				".#.##.#OOOOOO......",
				".#.#.#####O...O.OOO",
				"...#..#.#.#O...OO.O",
				"...#.#..#.#OOO.O#OO",
				"#..##.##O###OOOO###",
				"####.#OOO.#OOO##.#.",
				"...#..###.#O###.###",
				"#.#.#########...#..",
				"................##.",
		};
		player.getBoard().setUpProblem(diagram, WHITE);
		ShortSet deadStones = player.findDeadStones(0.75, BLACK);
		assertEquals(4, deadStones.size());
		assertTrue(deadStones.contains(coords.at("k12")));
		assertTrue(deadStones.contains(coords.at("l12")));
		assertTrue(deadStones.contains(coords.at("m19")));
		assertTrue(deadStones.contains(coords.at("n16")));
	}
	
	@Test
	public void testGetDeadStones3(){
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(19).memorySize(64)
				.openingBook(false).komi(0).build();
		coords = player.getBoard().getCoordinateSystem();
		String[] diagram = {
				"OOOO.OOO####.OO#.#O",
				"OO.OOO.OOO#.#####.O",
				".OOOO.OO.OO#.##.#.O",
				"OOOOOOOOOO####.#.#O",
				"O.O.O.O.OO##.O#.#.#",
				"OOOOO..O.O###.###..",
				".OO.O.OOO##.#.#.O##",
				".OOOOOOOOO##.O#####",
				"OOOO.OOO.OO###..##.",
				"OO.OO.O.O.O##..#.O#",
				"O.OO.OOOOOO#..#.#O#",
				"OOOOOO.O.O##...#.#.",
				".O....OOO##.##.##.#",
				"OO.O.O.OO###.####.#",
				"OO...OOOO##.###.#..",
				".OOOOO.OOO###.###..",
				"OOOO.OOOO##.##.##.#",
				".OOOO.OO##O##.#.##.",
				"O.O.OO.O##.O##.##.#",
		};
		player.getBoard().setUpProblem(diagram, WHITE);
		ShortSet deadStones = player.findDeadStones(0.75, WHITE);
		assertEquals(13, deadStones.size());
	}
	
	@Test
	public void testGetDeadStones4() {
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(9).memorySize(64)
				.openingBook(false).komi(0).build();
		coords = player.getBoard().getCoordinateSystem();
		String[] diagram = {
				"...#O....",
				"...#O....",
				"...#OOO..",
				"...#O.O..",
				"...#OOO..",
				"...#O.O..",
				"...#OOOOO",
				"...#O...#",
				"...#O...#",
		};
		player.getBoard().setUpProblem(diagram, WHITE);
		ShortSet deadStones = player.findDeadStones(0.75, BLACK);
		assertEquals(2, deadStones.size());
		assertTrue(deadStones.contains(at("j1")));
		assertTrue(deadStones.contains(at("j2")));
	}
	
	@Test
	public void testGetDeadStones5(){
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(19).memorySize(64)
				.openingBook(false).komi(0).build();
		coords = player.getBoard().getCoordinateSystem();
		String[] diagram = {
				".OOOO#........#....",
				".O##O#...##....##..",
				".OO#O##...###..#.#.",
				".O########..#..##.#",
				".OOOOOOOO#.....#.##",
				"..O#..O.O#...#..##.",
				".O.O.O.OO##..#.....",
				"...O...OOO#..###...",
				"..O..O..O.O#.......",
				"........OOO#.....#.",
				"#........O##...###.",
				"......O.OO#........",
				".........O###..###.",
				".#O....O.OOO##...#.",
				"...........O#.#....",
				"...O......OO##.###.",
				".......O..OO#O#..#.",
				"...........O#O###..",
				"...........OOOO#...",
		};
		player.getBoard().setUpProblem(diagram, WHITE);
		ShortSet deadStones = player.findDeadStones(0.75, BLACK);
		assertEquals(3, deadStones.size());
	}
	
	@Test
	public void testGetDeadStonesAfterTwoPasses() {
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(19).memorySize(64)
				.openingBook(false).komi(0).build();
		coords = player.getBoard().getCoordinateSystem();
		String[] diagram = {
				".OOOO#........#....",
				".O##O#...##....##..",
				".OO#O##...###..#.#.",
				".O########..#..##.#",
				".OOOOOOOO#.....#.##",
				"..O#..O.O#...#..##.",
				".O.O.O.OO##..#.....",
				"...O...OOO#..###...",
				"..O..O..O.O#.......",
				"........OOO#.....#.",
				"#........O##...###.",
				"......O.OO#........",
				".........O###..###.",
				".#O....O.OOO##...#.",
				"...........O#.#....",
				"...O......OO##.###.",
				".......O..OO#O#..#.",
				"...........O#O###..",
				"...........OOOO#...",
		};
		player.getBoard().setUpProblem(diagram, WHITE);
		player.getBoard().pass();
		player.getBoard().pass();
		ShortSet deadStones = player.findDeadStones(0.75, BLACK);
		assertEquals(3, deadStones.size());
		
	}
	
	@Test
	public void testTableOverflow() {
		// This tests a bug, occasionally encountered on KGS, where Orego
		// crashes while pondering because the transposition table fills up
		player = new PlayerBuilder().msecPerMove(2000).threads(8).boardWidth(5).memorySize(1).openingBook(false).build();
		// If there is a problem, this will crash
		player.bestMove();
	}

	@Test
	public void testEyelikePoint() {
		// We should NEVER play in an eyelike point
		player = new PlayerBuilder().msecPerMove(1).gestation(0).komi(0.5).boardWidth(5).memorySize(1).openingBook(false).build();
		coords = player.getBoard().getCoordinateSystem();
		player.clear();
		McRunnable runnable = player.getMcRunnable(0);
		// Store c3 as a good response to a1 in the LGRF2 tables
		runnable.acceptMove(at("a1"));
		runnable.acceptMove(at("c3"));
		player.getUpdater().updateTree(WHITE, runnable);
		// Set up a situation where c3 is infeasible
		player.acceptMove(PASS);
		player.acceptMove(at("c2"));
		player.acceptMove(PASS);
		player.acceptMove(at("c4"));
		player.acceptMove(PASS);
		player.acceptMove(at("b3"));
		player.acceptMove(PASS);
		player.acceptMove(at("d3"));
		// Now play a1, after which c3 is infeasible
		runnable.copyDataFrom(player.getBoard());
		runnable.acceptMove(at("a1"));
		runnable.playout(false);
		player.getUpdater().updateTree(WHITE, runnable);
		player.getUpdater().updateTree(WHITE, runnable);
		player.acceptMove(at("a1"));
		short move = player.getDescender().bestPlayMove();
		assertNotEquals(at("c3"), move);
	}

	@Test
	public void testLateUndo() {
		while (player.getBoard().getPasses() < 2) {
			short move = player.bestMove();
			if (move == RESIGN) {
				move = PASS;
			}
			player.acceptMove(move);
		}
		// This was sometimes causing an ArrayIndexOutOfBoundsException
		// late in the game
		player.undo();
	}

	@Test
	public void testPlayoutsCompletedResetEachRun() {
		player.bestMove();
		long run1 = player.getMcRunnable(0).getPlayoutsCompleted();
		player.bestMove();
		long run2 = player.getMcRunnable(0).getPlayoutsCompleted();
		assertTrue(run2 < 1.5 * run1);
	}

}
