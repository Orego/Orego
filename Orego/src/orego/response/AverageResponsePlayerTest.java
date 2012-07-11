package orego.response;

import static org.junit.Assert.*;

import java.util.HashMap;

import orego.core.Colors;
import orego.core.Coordinates;
import orego.mcts.McRunnable;

import org.junit.Before;
import org.junit.Test;

public class AverageResponsePlayerTest {

	AverageResponsePlayer player;

	@Before
	public void setup() {
		player = new AverageResponsePlayer();
		player.reset();
	}

	@Test
	public void testFindAppropriateMove() {

		McRunnable runnable = new McRunnable(player, null);
		int move1 = Coordinates.at("c16");
		int move2 = Coordinates.at("d8");
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

		runnable.getBoard().clear();

		HashMap<Integer, AbstractResponseList> responses = player
				.getResponses();
		int index2 = ResponsePlayer.levelTwoEncodedIndex(history2, history1,
				Colors.BLACK);
		int index1 = ResponsePlayer
				.levelOneEncodedIndex(history1, Colors.BLACK);
		int index0 = ResponsePlayer.levelZeroEncodedIndex(Colors.BLACK);
		assertNotNull(responses);
		assertNotNull(responses.get(index1));
		responses.get(index0).addWin(move1);
		responses.get(index0).addWin(move1);
		responses.get(index0).addWin(move1);
		responses.get(index2).addWin(move1);
		responses.get(index2).addWin(move1);
		responses.get(index2).addWin(move1);
		responses.get(index1).addWin(move1);
		responses.get(index1).addWin(move1);

		responses.get(index2).addWin(move2);
		responses.get(index2).addWin(move2);
		responses.get(index2).addWin(move2);
		for (int i = 0; i < 100; i++) {
			responses.get(index0).addWin(history2);
			responses.get(index1).addWin(history2);
		}
		player.getBoard().play(history2);
		player.getBoard().play(history1);
		assertEquals(move1, player.bestStoredMove());

	}

}
