package orego.mcts;

import static org.junit.Assert.*;

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
		for (int i = 41; i < 42; i++) {
			player.getBoard().play(i);
		}
		player.setPlayoutLimit(1000);
		komiRunnable.run();
		assertEquals(1, Math.abs(7.5 - player.getBoard().getKomi()), .001);
	}

}
