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
		// TODO: should we look at tables?
	}
	
	@Test
	public void testConstants() {
		assertEquals(21, WinLossStates.END_SCALE);
		assertEquals(.95, WinLossStates.CONFIDENCE_LEVEL, .0001);
		assertEquals(.500, WinLossStates.WIN_THRESHOLD, .0001);
		assertEquals(1.3, WinLossStates.JUMP_CONSTANT_K, .00001);
	}

	@Test
	public void testUpdateState() {
		
	}
}
