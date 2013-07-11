package orego.mcts;

import static orego.core.Board.NINE_PATTERN;
import static orego.core.Board.THREE_PATTERN;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.at;
import static orego.core.Coordinates.pointToString;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import orego.core.Board;
import orego.patternanalyze.PatternInformation;
import orego.util.IntSet;

import org.junit.Before;
import org.junit.Test;

public class PatternPlayerTest {

	private PatternPlayer player;

	private Board board;

	@Before
	public void setUp() throws Exception {
		player = new PatternPlayer();
		player.reset();
		board = new Board();
	}

	@Test
	public void testValidTableEntry() {
		String[] problem = { "...................",// 19
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
		player.setUpProblem(BLACK, problem);
		float d5rate = player.getInformation(THREE_PATTERN,
				player.getBoard().getPatternHash(THREE_PATTERN, at("D5")),BLACK)
				.getRate();
		long d5runs = player.getInformation(THREE_PATTERN,
				player.getBoard().getPatternHash(THREE_PATTERN, at("D5")),BLACK)
				.getRuns();
		float k1rate = player.getInformation(THREE_PATTERN,
				player.getBoard().getPatternHash(THREE_PATTERN, at("K1")),BLACK)
				.getRate();
		long k1runs = player.getInformation(THREE_PATTERN,
				player.getBoard().getPatternHash(THREE_PATTERN, at("K1")),BLACK)
				.getRuns();
		assertTrue(d5rate > k1rate);
		assertTrue(d5runs > k1runs);
	}

	@Test
	public void testSwitchBestMove() {
		String[] problem = { "###########OOOOOOO.",// 19
				"###########OOOOOOOO",// 18
				"###########OOOOOOOO",// 17
				"###########OOOOOOOO",// 16
				"###########OOOOOOOO",// 15
				"###OOO#####OOOOOOOO",// 14
				"###O.O#####O..OOOOO",// 13
				"###OOO#####OOOOOOOO",// 12
				"###########OOOOOOOO",// 11
				"###########OOOOOOOO",// 10
				"###########OOOOOOOO",// 9
				"###########OOOOOOOO",// 8
				"###########OOOOOOOO",// 7
				"###########OOOOOOOO",// 6
				"###########OOOOOOOO",// 5
				"###########OOOOOOOO",// 4
				"###########OOOOOOOO",// 3
				"###########OOOOOOOO",// 2
				".##########OOOOOOO." // 1
		// 		 ABCDEFGHJKLMNOPQRST
		};
		

		player.setUpProblem(WHITE, problem);
		
		//set up win rates
		IntSet vacantPoints = player.getBoard().getVacantPoints();
		for (int i = 0; i < vacantPoints.size(); i++) {
			for (int pattern = 0; pattern <= NINE_PATTERN; pattern++) {
				PatternInformation info = player.getInformation(pattern, player
						.getBoard()
						.getPatternHash(pattern, vacantPoints.get(i)),player.getBoard().getColorToPlay());
				if (vacantPoints.get(i) == at("A1")) {
					info.setRate(.49f);
					info.setRuns(100);
				} else if (vacantPoints.get(i) == at("O13")) {
					info.setRate(.5f);
					info.setRuns(100);
				} else {
					info.setRate(.35f);
					info.setRuns(100);
				}
			}
		}
		
		//test first playout suggestion over time
		player.setUpRunnables();
		player.bestMove();
		
		//get hightest win rate
		int move=0;
		double highWinRate = 0;
		for (int i = 0; i < vacantPoints.size(); i++) {
			if (highWinRate < player.getWinRate(vacantPoints.get(i))){
				highWinRate = player.getWinRate(vacantPoints.get(i));
				move = vacantPoints.get(i);
			}
		}
		assertEquals(at("A1"),move);
	}

}
