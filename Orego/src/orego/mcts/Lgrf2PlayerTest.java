package orego.mcts;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.*;
import static orego.core.Coordinates.PASS;
import static orego.mcts.MctsPlayerTest.TABLE_SIZE;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class Lgrf2PlayerTest {

	private Lgrf2Player player;

	private McRunnable runnable;

	@Before
	public void setUp() throws Exception {
		player = new Lgrf2Player();
		player.setProperty("pool", "" + TABLE_SIZE);
		player.setProperty("threads", "1");
		player.setProperty("heuristics", "Pattern:Capture");
		player.setPlayoutLimit(1000);
		player.reset();
		runnable = (McRunnable) (player.getRunnable(0));
	}

	/**
	 * Incorporates the indicates moves as if they had been generated by a real
	 * playout. Two passes are added to the end.
	 */
	protected void fakeRun(int winner, String... labels) {
		int[] moves = new int[labels.length + 2];
		int i;
		for (i = 0; i < labels.length; i++) {
			moves[i] = at(labels[i]);
		}
		moves[i] = PASS;
		moves[i + 1] = PASS;
		player.fakeGenerateMovesToFrontierOfTree(runnable, moves);
		runnable.copyDataFrom(player.getBoard());
		for (int p : moves) {
			runnable.acceptMove(p);
		}
		player.incorporateRun(winner, runnable);
	}

	@Test
	public void testIncorporateRun() {
		fakeRun(WHITE, "a1", "b1", "c1", "d1");
		assertEquals(at("b1"), player.getReplies1()[WHITE][at("a1")]);
		assertEquals(NO_POINT, player.getReplies1()[BLACK][at("b1")]);
		assertEquals(at("d1"), player.getReplies1()[WHITE][at("c1")]);
	}

	@Test
	public void testIncorporateRun2() {
		fakeRun(BLACK, "a1", "b1", "b2", "c2", "c1", "d1", "a2", "b1", "b3");
		assertEquals(at("b3"), player.getReplies1()[BLACK][at("b1")]);
		assertEquals(at("b2"), player.getReplies2()[BLACK][at("a1")][at("b1")]);
		assertEquals(at("b3"), player.getReplies2()[BLACK][at("a2")][at("b1")]);
	}

	@Test
	public void testForget() {
		fakeRun(BLACK, "a1", "b1", "b2", "c2", "c1", "d1", "a2", "b1", "b3");
		fakeRun(WHITE, "a1", "b1", "b2", "e1", "c1", "d1", "e3", "e4", "b3");
		assertEquals(at("b3"), player.getReplies1()[BLACK][at("b1")]);
		assertEquals(NO_POINT, player.getReplies2()[BLACK][at("a1")][at("b1")]);
		assertEquals(at("e1"), player.getReplies2()[WHITE][at("b1")][at("b2")]);
		assertEquals(NO_POINT, player.getReplies2()[BLACK][at("a1")][at("b1")]);
		assertEquals(at("a2"), player.getReplies2()[BLACK][at("c1")][at("d1")]);
	}

	@Test
	public void testPreviousMoves() {
		fakeRun(BLACK, "a1", "b1", "b2", "c2", "c1", "d1", "a2", "b1", "b3");
		player.acceptMove(at("a1"));
		player.acceptMove(at("b1"));
		runnable.copyDataFrom(player.getBoard());
		runnable.playout();
		assertEquals(at("b2"), runnable.getMove(2));
	}

	@Test
	public void testLifeOrDeath() {

		player.reset();
		String[] diagram = { "#######....########",// 19
				"###################",// 18
				"###################",// 17
				"###################",// 16
				"###################",// 15
				"###################",// 14
				"###################",// 13
				"###################",// 12
				"###################",// 11
				".##################",// 10
				"OOOOOOOOOOOOOOOOOOO",// 9
				"OOOOOOOOOOOOOOOOOOO",// 8
				"OOOOOOOOOOOOOOOOOOO",// 7
				"OOOOOOOOOOOOOOOOOOO",// 6
				"OOOOOOOOOOOOOOOOOOO",// 5
				"OOOOOOOOOOOOOOOOOOO",// 4
				"OOOOOOOOOOOOOOOOOOO",// 3
				"OOOOOOOOOOOOOOOOOOO",// 2
				".OOOOOOOOOOOOOOOOO." // 1
		// ABCDEFGHJKLMNOPQRST
		};
		player.setUpProblem(BLACK, diagram);
		player.getBoard().play(at("a10"));
		player.bestMove();
		assertEquals(at("k19"),
				player.getReplies2()[BLACK][at("a10")][at("j19")]);
		assertEquals(at("j19"),
				player.getReplies2()[BLACK][at("a10")][at("k19")]);
	}
}
