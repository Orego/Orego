package edu.lclark.orego.time;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

public class UniformTimeManagerTest {

	private TimeManager manager;
	
	@Before
	public void setUp() throws Exception {
		Board board = new Board(5);
		manager = new UniformTimeManager(board);
	}

	@Test
	public void testSetRemainingSeconds() {
		manager.setRemainingSeconds(100);
		assertEquals(9000, manager.getMsec());
		manager.setRemainingSeconds(1000);
		manager.startNewTurn();
		assertEquals(99000, manager.getMsec());
	}

}
