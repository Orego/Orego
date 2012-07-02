package orego.mcts;

import static org.junit.Assert.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;

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
		for (int i = 21; i < 39; i++) {
			komiPlayer.getBoard().play(i);
		}
		for (int i = 41; i < 43; i++) {
			komiPlayer.getBoard().play(i);
		}
		for (int i = 40; i < 180; i++) {
			komiPlayer.getRoot().addLosses(i, 1000);
			komiPlayer.getRoot().addWins(i, 12);
		}
		komiPlayer.valueSituationalCompensation();
		assertEquals(29.5, komiPlayer.getBoard().getKomi(), .001);

		komiPlayer.reset();
		komiPlayer.getBoard().setUpHandicap(2);
		komiPlayer.valueSituationalCompensation();
		assertEquals(14.5, komiPlayer.getBoard().getKomi(), .001);
		for (int i = 21; i < 39; i++) {
			komiPlayer.getBoard().play(i);
		}
		for (int i = 41; i < 43; i++) {
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

	@Test
	public void testGameNearComplete() {
		String[] diagram = { "...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"###########........",// 15
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
		komiPlayer.setUpProblem(BLACK, diagram);
		komiPlayer.getRoot().addLosses(25, 1000);
		komiPlayer.getBoard().setKomi(-20);
		komiPlayer.valueSituationalCompensation();
		assertEquals(-20.5, komiPlayer.getBoard().getKomi(), .001);
		komiPlayer.getBoard().play(27);
		komiPlayer.getBoard().play(28);
		komiPlayer.valueSituationalCompensation();
		assertTrue(komiPlayer.getBoard().getKomi() > 0);

	}
}
