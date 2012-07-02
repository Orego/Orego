package orego.wls;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class WinLossStatesTest {

	private WinLossStates states;
	
	@Before
	public void setUp() throws Exception {
		states = new WinLossStates(.95, 21);
	}

	@Test
	public void testTables() {
		// pick a few random values to test the table
		State state = states.getState(0); // at 0/0
		assertNotNull(state);
		
		assertEquals(0, state.getWins());
		assertEquals(0, state.getRuns());
		
		// pick the last state (21/21)
		state = states.getState(WinLossStates.NUM_STATES - 1);
		assertNotNull(state);
		
		assertEquals(21, state.getWins());
		assertEquals(21, state.getRuns());
	}
	
	@Test
	public void testConstants() {
		assertEquals(21, WinLossStates.END_SCALE);
		assertEquals(.95, WinLossStates.CONFIDENCE_LEVEL, .0001);
		assertEquals(.500, WinLossStates.WIN_THRESHOLD, .0001);
		assertEquals(1.3, WinLossStates.JUMP_CONSTANT_K, .00001);
	}

	@Test
	public void testJump() {
		// very last state (21/21)
		int nextStateIndex = states.addWin(WinLossStates.NUM_STATES - 1);
		
		assertFalse(nextStateIndex == WinLossStates.NO_STATE_EXISTS);
		
		// we should jump somewhere new
		// TODO: should we actually test the formula
		State nextState = states.getState(nextStateIndex);
		
		assertNotNull(nextState);
		
		// TODO: does the win rate change?
		assertTrue(nextState.getRuns() < 21);
		assertTrue(nextState.getRuns() > 0); // our new jump location
	}
	
	
	@Test
	public void testUpdateState() {
		int nextStateIndex = states.addWin(0); // start at 0/0
		assertFalse(nextStateIndex == WinLossStates.NO_STATE_EXISTS);
		
		State nextState = states.getState(nextStateIndex);
		
		assertNotNull(nextState);
		assertEquals(1, nextState.getWins());
		assertEquals(1, nextState.getRuns());
		// TODO: test confidence?
		
		
		nextStateIndex = states.addLoss(0);
		assertFalse(nextStateIndex == WinLossStates.NO_STATE_EXISTS);
		
		nextState = states.getState(nextStateIndex);
		
		assertNotNull(nextState);
		assertEquals(0, nextState.getWins());
		assertEquals(1, nextState.getRuns());
		// TODO: test confidence?
		
		// TODO: test a few other values
	}
}
