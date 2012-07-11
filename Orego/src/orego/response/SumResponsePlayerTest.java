package orego.response;

import static org.junit.Assert.*;

import java.util.HashMap;

import orego.core.Colors;
import orego.core.Coordinates;
import orego.mcts.McRunnable;

import org.junit.Before;
import org.junit.Test;

public class SumResponsePlayerTest {

	SumResponsePlayer player;

	@Before
	public void setup() {
		player = new SumResponsePlayer();
		player.reset();
	}

	@Test
	public void testFindAppropriateMove() {
		// play a fake game
		McRunnable runnable = new McRunnable(player, null);
		int move1 = Coordinates.at("a5");
		int move2 = Coordinates.at("a6");
		int history1 = Coordinates.at("h1");
		int history2 = Coordinates.at("h2");

		runnable.acceptMove(history2);
		runnable.acceptMove(history1);
		runnable.acceptMove(move1);
		player.incorporateRun(Colors.BLACK, runnable);

		runnable.getBoard().clear();

		runnable.acceptMove(history2);
		runnable.acceptMove(history1);
		runnable.acceptMove(move2);
		player.incorporateRun(Colors.BLACK, runnable);

		HashMap<Integer, AbstractResponseList> responses = player
				.getResponses();
		int index2 = ResponsePlayer.levelTwoEncodedIndex(history2, history1,
				Colors.BLACK);
		int index1 = ResponsePlayer
				.levelOneEncodedIndex(history1, Colors.BLACK);
		int index0 = ResponsePlayer.levelZeroEncodedIndex(Colors.BLACK);
		for (int i = 0; i < 10; i++) {
			assertNotNull(responses);
			assertNotNull(responses.get(index1));
			responses.get(index1).addWin(move1);
			responses.get(index2).addWin(move1);
			responses.get(index1).addWin(move2);
			responses.get(index0).addWin(move2);
		}
		responses.get(index2).addWin(move2);
		assertEquals(move2, player.bestStoredMove());
	}

}
