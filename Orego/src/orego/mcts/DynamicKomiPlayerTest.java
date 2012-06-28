package orego.mcts;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DynamicKomiPlayerTest {

	private DynamicKomiPlayer komiPlayer;

	@Before
	public void setUp() {
		komiPlayer = new DynamicKomiPlayer();
		komiPlayer.reset();
	}

	@Test
	public void testValueSituationalCompensation() {
		komiPlayer.getBoard().setUpHandicap(5);
		komiPlayer.valueSituationalCompensation();
		assertEquals(30.5, komiPlayer.getBoard().getKomi(), .001);
		for (int i = 21; i < 43; i++) {
			komiPlayer.getBoard().play(i);
		}
		for (int i = 21; i < 180; i++) {
			komiPlayer.getRoot().addLosses(i, 1000);
			komiPlayer.getRoot().addWins(i, 12);
		}
		komiPlayer.valueSituationalCompensation();
		assertEquals(29.5, komiPlayer.getBoard().getKomi(), .001);

		komiPlayer.reset();
		komiPlayer.getBoard().setUpHandicap(2);
		komiPlayer.valueSituationalCompensation();
		assertEquals(14.5, komiPlayer.getBoard().getKomi(), .001);
		for (int i = 21; i < 43; i++) {
			komiPlayer.getBoard().play(i);
		}
		for (int i = 21; i < 100; i++) {
			komiPlayer.getRoot().addLosses(i, 10);
			komiPlayer.getRoot().addWins(i, 120);
		}
		komiPlayer.valueSituationalCompensation();
		assertEquals(15.5, komiPlayer.getBoard().getKomi(), .001);
	}
	
	@Test
	public void testChangesToRest() {
		komiPlayer.reset();
		assertEquals(KomiRunnable.class, komiPlayer.getRunnable(0).getClass());
		
	}
}
