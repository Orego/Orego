package orego.mcts;

import static org.junit.Assert.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;

import org.junit.Before;
import org.junit.Test;

public class KomiRunnableTest {

	private DynamicKomiPlayer player;
	private KomiRunnable komiRunnable;

	@Before
	public void setUp() {
		player = new DynamicKomiPlayer();
		player.reset();
		komiRunnable = (KomiRunnable) player.getRunnable(0);
	}

	@Test
	public void testRun() {
		player.setPlayoutLimit(999);
		assertEquals(7.5, player.getBoard().getKomi(), .001);
		komiRunnable.run();
		assertEquals(7.5, player.getBoard().getKomi(), .001);
		for (int i = 21; i < 39; i++) {
			player.getBoard().play(i);
		}
		for (int i = 41; i < 43; i++) {
			player.getBoard().play(i);
		}
		player.getRoot().addLosses(67, 1000);
		player.setPlayoutLimit(1000);
		komiRunnable.run();
		assertEquals(1, Math.abs(7.5 - player.getBoard().getKomi()), .001);
	}

	@Test
	public void testColorIsBlack() {
		player.setPlayoutLimit(1000);
		assertEquals(7.5, player.getBoard().getKomi(), .001);
		assertTrue(player.getBoard().getColorToPlay() == BLACK);
		for (int i = FIRST_POINT_ON_BOARD; i < 39; i++) {
			player.getBoard().play(i);
		}
		player.getBoard().play(41);
		player.getRoot().addWins(67, 1000);
		komiRunnable.run();
		assertEquals(7.5, player.getBoard().getKomi(), .001);
		player.getBoard().play(42);
		komiRunnable.run();
		assertEquals(1, Math.abs(7.5 - player.getBoard().getKomi()), .001);

	}

}
