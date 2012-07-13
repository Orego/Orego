package orego.response;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.at;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import orego.core.Colors;
import orego.mcts.McRunnable;
import orego.policy.RandomPolicy;

import org.junit.Before;
import org.junit.Test;

public class ResponsePlayerTest {
	
	ResponsePlayer player;

	@Before
	public void setUp() throws Exception {
		player = new ResponsePlayer();
		player.reset();
	}
	
	@Test
	public void testIncorporateRun() {
		// save original thresholds to reset later
		int originalOneThreshold = player.getOneThreshold();
		int originalTwoThreshold = player.getTwoThreshold();
		
		// set testing thresholds (after one run, we want to incorporate)
		player.setOneThreshold(1);
		player.setTwoThreshold(1);
		
		// we need two boards to test. ResponsePlayer only
		// creates a level two table entry after it has seen a move
		// *twice*.
		McRunnable run1 = new McRunnable(player, null);
		run1.acceptMove(28);
		run1.acceptMove(25);
		run1.acceptMove(47);
		run1.acceptMove(52);
		
		McRunnable run2 = new McRunnable(player, null);
		run2.acceptMove(28);
		run2.acceptMove(25);
		run2.acceptMove(47);
		run2.acceptMove(52);
		
		// incorporate these moves (black wins for these moves)
		player.incorporateRun(Colors.BLACK, run1);
		player.incorporateRun(Colors.BLACK, run2);
		
		HashMap<Integer, AbstractResponseList> responses = player.getResponses();
		
		// TODO: test is specific to RawResponseList. Nothing we can do about this?
		
		RawResponseList blackZeroList = (RawResponseList) responses.get(ResponsePlayer.levelZeroEncodedIndex(Colors.BLACK));
		
		
		RawResponseList whiteZeroList = (RawResponseList) responses.get(ResponsePlayer.levelZeroEncodedIndex(Colors.WHITE));
		
		assertThat(blackZeroList, notNullValue());
		// Make sure all of the Black lists are right
		assertThat(blackZeroList.getWins()[28], equalTo(3));
		assertThat(blackZeroList.getRuns()[28], equalTo(4));
		assertThat(blackZeroList.getWins()[47], equalTo(3));
		assertThat(blackZeroList.getRuns()[47], equalTo(4));
		
		
		// test back one move from 47
		RawResponseList blackOneList = (RawResponseList) responses.get(ResponsePlayer.levelOneEncodedIndex(25, Colors.BLACK));
		
		assertThat(blackOneList, notNullValue());
        assertThat(blackOneList.getWins(47), equalTo(3));
		
        // test back two moves from 47
		RawResponseList blackTwoList = (RawResponseList) responses.get(ResponsePlayer.levelTwoEncodedIndex(28, 25, Colors.BLACK));
		
		assertThat(blackTwoList, notNullValue());
		
		assertThat(blackTwoList.getWins()[47], equalTo(2));
		
		// Make sure all of the white lists are right
		assertThat(whiteZeroList, notNullValue());
		
		assertThat(whiteZeroList.getWins()[25], equalTo(1));
		assertThat(whiteZeroList.getRuns()[25], equalTo(4));
		
		RawResponseList whiteOneList = (RawResponseList) responses.get(ResponsePlayer.levelOneEncodedIndex(28, Colors.WHITE));
		
		assertThat(whiteOneList, notNullValue());
		assertThat(whiteOneList.getWins()[25], equalTo(1));
		
		RawResponseList whiteTwoList = (RawResponseList) responses.get(ResponsePlayer.levelTwoEncodedIndex(25, 47, Colors.WHITE));
		assertThat(whiteTwoList, notNullValue());
		
		assertThat(whiteTwoList.getWins()[52], equalTo(1));
		
		// reset to original threshold
		player.setOneThreshold(originalOneThreshold);
		player.setTwoThreshold(originalTwoThreshold);
	}
	
	protected void fakeRun(int winner, String... labels) {
		int[] moves = new int[labels.length + 2];
		int i;
		for (i = 0; i < labels.length; i++) {
			moves[i] = at(labels[i]);
		}
		
		McRunnable runnable = new McRunnable(player, new RandomPolicy());
		player.fakePlayMoves(runnable, moves);
		runnable.copyDataFrom(player.getBoard());
		for (int p : moves) {
			runnable.acceptMove(p);
		}
		player.incorporateRun(winner, runnable);
	}
	
	@Test
	public void testBestStoredMove() {
		fakeRun(BLACK, "c4", "c5", "c6", "c7");
		fakeRun(BLACK, "c4", "c5", "c6", "c7");
		fakeRun(WHITE, "c5", "c4", "c6", "c7");
		assertEquals(at("c4"), player.bestStoredMove());
	}
	
	@Test
	public void testPassInSeki() {
		String[] problem = { 
				"#.#########OOOOOOOO",// 19
				".##########OOOOOOO.",// 18
				"###########OOOOOOOO",// 17
				"###########OOOOOOO.",// 16
				"###########OOOOOOOO",// 15
				"#########OOOOOOOOOO",// 14
				"#########OOOOOOOOOO",// 13
				"#########OOOOOOOOOO",// 12
				"#########OOOOOOOOOO",// 11
				"#########OOOOOOOOOO",// 10
				"#########OOOOOOOOOO",// 9
				"#########OOOOOOOOOO",// 8
				"#########OOOOOOOOOO",// 7
				"#########OOOOOOOOOO",// 6
				"#########OOOOOOOOOO",// 5
				"##OO###OOOO######OO",// 4
				"OOO#OO#########OOOO",// 3
				".OO#.O#.######OOOOO",// 2
				"O.O#.O##.######OOOO" // 1
		// ABCDEFGHJKLMNOPQRST
		};
		player.setUpProblem(BLACK, problem);
		McRunnable runnable = new McRunnable(player, new RandomPolicy());
		
		for (int i = 0; i < 1000; i++) {
			runnable.performMcRun();
		}
		int move = runnable.getPlayer().bestMove();
		assertEquals(PASS, move);

	}
	
	@Test
	public void testKillDeadStonesToOvercomeLargeTerritory() {
		String[] problem = { "#..................",// 19
				"O..................",// 18
				"O..................",// 17
				"O..................",// 16
				"O..................",// 15
				"O..................",// 14
				"OOOOOOOOOOOOOOOOOOO",// 13
				"OOOOOOOOOOOOOOOOOOO",// 12
				"OOOOOOOOOOOOOOOOOOO",// 11
				"OOOOOOOOOOOOOOOOOOO",// 10
				"###################",// 9
				"#..................",// 8
				"#..................",// 7
				"#..................",// 6
				"#..................",// 5
				"#..................",// 4
				"#..................",// 3
				"####...............",// 2
				".#.#..............." // 1
		// ABCDEFGHJKLMNOPQRST
		};
		player.getBoard().setUpProblem(BLACK, problem);
		McRunnable runnable = new McRunnable(player, new RandomPolicy());
		player.acceptMove(PASS);
		for (int i = 0; i < 1000; i++) {
			runnable.performMcRun();
		}
		int move = runnable.getPlayer().bestMove();
		// White must capture the dead black stone to win
		assertFalse(PASS == move);
	}
	
	@Test
	public void testKillDeadStonesToOvercomeLargeTerritory2() {
		// Orego must realize that its own stone is dead.
		String[] problem = { "##.O.OOOOOOOOOOOOO.",// 19
				"OOOOOOOOOOOOOOOOOOO",// 18
				"OOOOOOOOOOOOOOOOOOO",// 17
				"OOOOOOOOOOOOOOOOOOO",// 16
				"OOOOOOOOOOOOOOOOOOO",// 15
				"OOOOOOOOOOOOOOOOOOO",// 14
				"OOOOOOOOOOOOOOOOOOO",// 13
				"OOOOOOOOOOOOOOOOOOO",// 12
				"OOOOOOOOOOOOOOOOOOO",// 11
				"OOOOOO#############",// 10
				"###################",// 9
				"###################",// 8
				"###################",// 7
				"###################",// 6
				"###################",// 5
				"###################",// 4
				"####.O#############",// 3
				"#######.###########",// 2
				".#.##..############" // 1
		// ABCDEFGHJKLMNOPQRST
		};
		player.getBoard().setUpProblem(BLACK, problem);
		McRunnable runnable = new McRunnable(player, new RandomPolicy());
		player.acceptMove(PASS);
		for (int i = 0; i < 10000; i++) {
			runnable.performMcRun();
		}
		int move = runnable.getPlayer().bestMove();
		// White must capture the dead black stones to win
		// assertEquals(at("c19"), move);
		assertFalse(PASS == move);
	}
	
	@Test
	public void testConnect() {
		String[] problem = { 
				"##########OOOOOOOOO",// 19
				"##########OOOOOOOOO",// 18
				"##..######OOOOOOOOO",// 17
				"###.######OOOO..OOO",// 16
				"##########OOOO.OOOO",// 15
				"##########OOOOOOOOO",// 14
				"##########OOOOOOOOO",// 13
				"##########OOOOOOOOO",// 12
				"##########OOOOOOOOO",// 11
				"OOOOOOOOO.OOOOOOOOO",// 10
				"OOOOOOOOO##########",// 9
				"OOOOOOOOO##########",// 8
				"OOOOOOOOO##########",// 7
				"OOOOOOOOO##########",// 6
				"OOOO.OOOO##########",// 5
				"OOOO..OOO#####.####",// 4
				"OOOOOOOOO####..####",// 3
				"OOOOOOOOO##########",// 2
				"OOOOOOOOO##########" // 1
			  // ABCDEFGHJKLMNOPQRST
		};
		player.setUpProblem(BLACK, problem);
		//TODO: check level zero and one tables -- chooses move PASS
		//when not consulting level two table
		

		McRunnable runnable = new McRunnable(player, new RandomPolicy());
		for (int i = 0; i < 1000; i++) {
			runnable.performMcRun();
		}
		
		
		// Note: best stored move finds the final move after all the Mc
		int move = runnable.getPlayer().bestStoredMove();
		assertEquals(at("k10"), move);
	} 
}
