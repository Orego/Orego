package edu.lclark.orego.mcts;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;

import org.junit.Before;
import org.junit.Test;

public class TextIncorporatorTest {

	private Player player;
	
	private TextIncorporator textIncorporator;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return player.getBoard().getCoordinateSystem().at(label);
	}

	@Before
	public void setUp() throws Exception {
		player = new Player(1, CopiableStructureFactory.feasible(5));
		textIncorporator = new TextIncorporator();
		player.setRunIncorporator(textIncorporator);
	}

	@Test
	public void testClear() {
		McRunnable runnable = player.getMcRunnable(0);
		runnable.performMcRun();
		assertNotEquals("", textIncorporator.toString());
		player.clear();
		assertEquals("", textIncorporator.toString());
	}

	@Test
	public void testIncorporateRun() {
		McRunnable runnable = player.getMcRunnable(0);
		runnable.acceptMove(at("b1"));
		runnable.acceptMove(at("c4"));
		runnable.acceptMove(at("a2"));
		textIncorporator.incorporateRun(BLACK, runnable);
		assertEquals("BLACK: [B1, C4, A2]\n", textIncorporator.toString());
		runnable.copyDataFrom(player.getBoard());
		runnable.acceptMove(at("e5"));
		runnable.acceptMove(at("d2"));
		runnable.acceptMove(at("a4"));
		textIncorporator.incorporateRun(WHITE, runnable);
		assertEquals("BLACK: [B1, C4, A2]\nWHITE: [E5, D2, A4]\n", textIncorporator.toString());
	}

}
