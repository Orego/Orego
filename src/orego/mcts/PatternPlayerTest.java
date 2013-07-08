package orego.mcts;

import static orego.core.Colors.BLACK;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

import static orego.core.Board.*;
import static orego.core.Coordinates.*;

public class PatternPlayerTest {
	
	private PatternPlayer player;
	
	private Board board;
	
	@Before
	public void setUp() throws Exception {
		player = new PatternPlayer();
		board = new Board();
	}

	@Test
	public void testValidTableEntry() {
		String[] problem = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				".OOO...............",// 9
				"O#.#O#.#...........",// 8
				"OO.OOO.O...........",// 7
				"...................",// 6
				"..#...O.O..........",// 5
				"...O...O...........",// 4
				"##....#............",// 3
				"OO#..#O#...........",// 2
				".O#....O#.........." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		float d5rate = player.getInformation(THREE_PATTERN,board.getPatternHash(THREE_PATTERN, at("D5"))).getRate();
		int d5runs = player.getInformation(THREE_PATTERN,board.getPatternHash(THREE_PATTERN, at("D5"))).getRuns();
		float a1rate = player.getInformation(THREE_PATTERN,board.getPatternHash(THREE_PATTERN, at("A1"))).getRate();
		int a1runs = player.getInformation(THREE_PATTERN,board.getPatternHash(THREE_PATTERN, at("A1"))).getRuns();
		assertTrue(d5rate > a1rate);
		assertTrue(d5runs > a1runs);
	}

}
