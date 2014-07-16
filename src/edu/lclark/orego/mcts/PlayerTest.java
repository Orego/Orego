package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static org.junit.Assert.*;
import static edu.lclark.orego.util.TestingTools.asOneString;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.CoordinateSystem.RESIGN;

public class PlayerTest {

	private Player player;

	/** Delegate method to call at on board. */
	private short at(String label) {
		return player.getBoard().getCoordinateSystem().at(label);
	}

	@Before
	public void setUp() throws Exception {
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(5).memorySize(64)
				.openingBook(false).build();
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
		System.out.println(successes);
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
		assertTrue(deadStones.contains(player.getBoard().getCoordinateSystem().at("D3")));
		assertTrue(deadStones.contains(player.getBoard().getCoordinateSystem().at("D9")));

	}

}
