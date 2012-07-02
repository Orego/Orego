package orego.wls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StateTest {
	private State state;
	

	@Test
	public void testCompare() {
		// compare proportions below 1/2
		State reducedState = new State(1, 4);
		State biggerState = new State(2, 8);
		
		assertTrue(reducedState.getConfidence() > state.getConfidence());
		
		// compare proportions above 1/2
		reducedState = new State(3, 4);
		biggerState = new State(12, 16);
		
		assertTrue(reducedState.getConfidence() < biggerState.getConfidence());
		
		// compare proportions exactly at a half
		reducedState = new State(1, 2);
		biggerState = new State(2, 4);
		
		assertTrue(reducedState.getConfidence() < biggerState.getConfidence());
		
	}
	
	@Test
	public void testLosses() {
		State state = new State(1, 6);
		assertEquals(5, state.getLosses());
		
		state = new State(4, 12);
		assertEquals(8, state.getLosses());
	}
	@Test
	public void testComputeConfidence() {
		// TODO: recompute all of the confidence values with 95% confidence
		
		// test upper bound on proportion less than the WIN_THRESHOLD (1/2)
		State state = new State(1, 6);
		
		assertEquals(1.0/6.0, state.getWinRunsProportion(), .0001);
		
		// we pre-calculate the confidence and then add the extra -1 to shift down
		assertEquals(0.5822024 - 1, .0001, state.getConfidence());
		
		// test lower bound on proportion greater than WIN_THRESHOLD (1/2)
		state = new State(12, 16);
		
		assertEquals(12.0 / 16.0, state.getWinRunsProportion(), .0001);
		
		// we pre-calculate (with 95% confidence) the confidence interval and *don't* add +1 since lower bound
		assertEquals(0.5002644, state.getConfidence(), .0001);
	}
	
	@Test
	public void testToString() {
		// look at greatest state (21/21)
		State state = new State(21, 21);
		
		assertEquals("\nState: 253\nWins: 21\nRuns: 21\nLosses: 0\n Confidence: 0.974\n", state.toString());
	}
	
	@Test
	public void testWLProportion() {
		State state = new State(12, 19);
		
		assertEquals(12.0/19.0, state.getWinRunsProportion(), .0001);
		
		state = new State(0, 0);
		assertEquals(Double.MIN_VALUE, state.getWinRunsProportion(), .0001);
		
		state = new State(21, 21);
		assertEquals(21.0/21.0, state.getWinRunsProportion(), .0001);
		
	}
	
	@Test
	public void testZValueTable() {
		assertEquals(.95, WinLossStates.CONFIDENCE_LEVEL, .001); // assume at 95%
		
		State state = new State(1, 3);
		double zVal = state.getZValue(.95000);
		
		assertEquals(1.96, zVal, .0001);
		
		zVal = state.getZValue(.97500);
		
		assertEquals(1.65, zVal, .0001);
		
		zVal = state.getZValue(.74857);
		assertEquals(.674490, zVal, .00001);
	}
}
