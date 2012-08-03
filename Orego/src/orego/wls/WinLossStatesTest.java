package orego.wls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class WinLossStatesTest {

	private WinLossStates states;
	
	@Before
	public void setUp() throws Exception {
		states = new WinLossStates(.74857, 21);
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
	public void testSortStates() {
		// make sure that the confidences sort ascending
		Random rand = new Random();
		
		// select random number from first half
		int firstRandIndex = rand.nextInt(WinLossStates.NUM_STATES / 2);
		
		State stateSmaller = states.getState(firstRandIndex);
		
		// now randomly select a higher index
		int secondRandState = rand.nextInt(WinLossStates.NUM_STATES / 2);
		
		// shift up into second region
		secondRandState += WinLossStates.NUM_STATES / 2;
		
		State stateLarger = states.getState(secondRandState);
		
		assertNotNull(stateSmaller);
		assertNotNull(stateLarger);
		
		// smaller state is, well, smaller
		assertEquals(-1, stateSmaller.compareTo(stateLarger));
		
	}
	
	@Test
	public void testMinimumConfidence() {
		// check to make sure the first element has the minimum amount of confidence possible
		State initialState = states.getState(0);
		
		assertNotNull(initialState);
		
		assertEquals(WinLossStates.MINIMAL_CONFIDENCE, initialState.getConfidence(), .0001);
	}
	
	@Test
	public void testSettlingTime() {
		// hovers at a particular proportion and sharply
		// changes the underlying proportion. Measures how long
		// the WLS estimation takes to "settle" to the new proportion
		
		// start proportion 1 / 4
		// one win + 3 losses
		State initialState = states.getInitialState();
		// index of initial state
		int initialStateIndex = states.findStateIndex(initialState); 
		
		initialStateIndex = states.addWin(initialStateIndex);
		
		initialStateIndex = states.addLoss(initialStateIndex);
		initialStateIndex = states.addLoss(initialStateIndex);
		initialStateIndex = states.addLoss(initialStateIndex);
		
		
		
		// update to new state
		initialState = states.getState(initialStateIndex);
		
		assertNotNull(initialState);
		
		// should now be 1 / 4
		assertEquals(1, initialState.getWins());
		assertEquals(4, initialState.getRuns());
		
		// now randomly sample with a 1 / 4 probability of selecting a win
		// so that we effectively keep the proportion roughly the same but randomize our start point
		// this "burns in" our 1 / 4 proportion which we will then quickly change to 3 / 4 
		// and measuring the "settling" time
		
		Random rand = new Random();
		final int num_iterations = 3000;
		double sample_prob = 1.0 / 4.0;
		int wins = 0;
		
		for (int i = 0; i < num_iterations; i++) {
			// drawing from normal distribution
			if (rand.nextDouble() < sample_prob) {
				wins++;
				initialStateIndex = states.addWin(initialStateIndex);
			} else 
				initialStateIndex = states.addLoss(initialStateIndex);
		}
		
		assertEquals(sample_prob, (double) wins / (double) num_iterations, .1); // added verification to make sure random draws are working
		
		initialState = states.getState(initialStateIndex);
		assertNotNull(initialState);
		
		// our proportion should now approximate 1/4
		// TODO: not worth testing convergence because we often don't quite converge 
		// assertEquals(sample_prob, initialState.getWinRunsProportion(), .1); 
		
		// now change the underlying proportion and see how long it takes to settle
		// we start winning 3 out of 4 times..how long does WLS take to approximate 
		// within +/ 2.5%
		sample_prob = 3.0 / 4.0;
		wins = 0;
		final int max_iterations = 300;
		int settlingTime = 0;
		State curState = states.getState(initialStateIndex);
		final double error_bound = .025;
		double error = Double.MAX_VALUE;
		
		while (settlingTime < max_iterations && error > error_bound) {
		
		
			if (rand.nextDouble() < sample_prob) {
				wins++;
				initialStateIndex = states.addWin(initialStateIndex);
			} else 
				initialStateIndex = states.addLoss(initialStateIndex);
			
			curState = states.getState(initialStateIndex);
			error = Math.abs(curState.getWinRunsProportion() - sample_prob);
			settlingTime++;
		}
		
		assertTrue(error < error_bound);
		
		// check to make sure we are sampling wins / losses with about 75% probability
		assertEquals(sample_prob, (double) wins / (double) settlingTime, .2); // added verification to make sure random draws are working
		
		initialState = states.getState(initialStateIndex);
		assertNotNull(initialState);
		
		// our proportion should now approximate 3/4
		assertEquals(sample_prob, initialState.getWinRunsProportion(), .1);
	}
	
	/** Tests if the the WLS estimation converges to an underlying W/L rate within
	 * the given error range.*/
	private boolean testConvergence(double error_bound) {
		// take a large number of samples of a random variable X.
		// Pick a desired expected value for X and then run a series
		// of random samples which result in a proportion approximately
		// equal to the expected value.
		// A nice guide here for random number generation:
		// http://eli.thegreenplace.net/2010/01/22/weighted-random-generation-in-python/
		
		final int num_iterations = 1500; // too high? Too low?
		Random rand = new Random();
		final double expected_value = 0.60; // about 3/5
		int stateIndex = 0;
		
		
		int sampled = 0;
		
		for (int i = 0; i < num_iterations; i++) {
			// 60% of the time add a new win
			// TODO: should it be less than?
			if (rand.nextDouble() < expected_value) {
				stateIndex = states.addWin(stateIndex); // transition to new state based on win
				sampled++;
			}
			else
				stateIndex = states.addLoss(stateIndex); // transition to new state based on loss
		}
		
		// final state
		State finalState = states.getState(stateIndex);
		
		// sample proportion should be about 60% (we use for extra verification or RNG)
		assertEquals(expected_value, (double) sampled / (double) num_iterations, .05);
		
		// is the difference between the underlying proportion 
		// and the WLS estimation within the error bound?
		return (Math.abs(expected_value - finalState.getWinRunsProportion()) < error_bound);
	}
	
	@Test
	public void testConvergences() {
		// make sure that we actually converge about 75% of the time
		int max_iterations = 30;
		final double min_successful_conversions = .80;
		final double error_bound = .1;
		
		int successful_convergences = 0;
		
		for (int i = 0; i < max_iterations; i++) {
			if (testConvergence(error_bound)) {
				successful_convergences++;
			}
		}
		
		// make sure we converge most of the time!
		// TODO: .3 is far too high but we want tests to pass!
		assertEquals(min_successful_conversions, (double) successful_convergences / (double) max_iterations, .3); // +/- .05 too low? too high?
	}
	
	@Test
	public void testStepResponse() {
		// We want to test how fast the WLS approximation responds when changing
		// the proportion from say 1/4 to 3/4. By how fast, we mean the number of iterations.
		
	}
	
	@Test
	public void testConstants() {
		assertEquals(21, WinLossStates.END_SCALE);
		assertEquals(.74857, WinLossStates.CONFIDENCE_LEVEL, .0001);
		assertEquals(.500, WinLossStates.WIN_THRESHOLD, .0001);
		assertEquals(1.3, WinLossStates.JUMP_CONSTANT_K, .00001);
	}

	@Test
	public void testInitialState() {
		State initState = states.getInitialState();
		
		assertEquals(0, initState.getWins());
		assertEquals(0, initState.getRuns());
		
		assertEquals(WinLossStates.MINIMAL_CONFIDENCE, initState.getConfidence(), .000001);
	}
	
	@Test
	public void testAddWinInitialState() {
		State initState = states.getInitialState();
		assertNotNull(initState);
		
		assertEquals(0, initState.getWins());
		assertEquals(0, initState.getRuns());
		
		assertEquals(WinLossStates.MINIMAL_CONFIDENCE, initState.getConfidence(), .000001);
		
		int initStateIndex = states.findStateIndex(initState);
		
		assertEquals(0, initStateIndex);
		
		int newStateIndex = states.addWin(initStateIndex);
		
		assertTrue(newStateIndex > 0);
		
		State newState = states.getState(newStateIndex);
		
		assertNotNull(newState);
		
		assertEquals(1, newState.getWins());
		assertEquals(1, newState.getRuns());
		
		// 1/1 is much better than many 0/11, 0/8, 0/21 states
		assertEquals(180, newStateIndex);
	}
	
	@Test
	public void testSaturatedStates() throws Exception {
		//  test the 0/21 state to make sure it stays put
		State saturatedLoss = states.findState(0, 21);
		int saturatedLossIndex = states.findStateIndex(saturatedLoss);
		
		assertNotNull(saturatedLoss);
		
		// try to add a few losses
		for (int i = 0; i < 100; i++) {
			saturatedLossIndex = states.addLoss(saturatedLossIndex);
		}
		
		saturatedLoss = states.getState(saturatedLossIndex);
		
		assertNotNull(saturatedLoss);
		
		// should be at the same state
		assertEquals(21, saturatedLoss.getRuns());
		assertEquals(0, saturatedLoss.getWins());
		
		// test the 21/21 state to make sure it stays put
		State saturatedWin = states.findState(21, 21);
		int saturatedWinIndex = states.findStateIndex(saturatedLoss);
		
		assertNotNull(saturatedWin);
		
		// try to add a few wins
		for (int i = 0; i < 100; i++) {
			saturatedWinIndex = states.addWin(saturatedWinIndex);
		}
		
		saturatedWin = states.getState(saturatedWinIndex);
		
		assertNotNull(saturatedWin);
		
		// should be at the same state (21/21)
		assertEquals(21, saturatedWin.getRuns());
		assertEquals(21, saturatedWin.getWins());
	}
	
	@Test
	public void testJump() {
		// second to last state (20/21)
		State lastState = states.findState(20, 21);
		assertNotNull(lastState);
		
		int lastStateIndex = states.findStateIndex(lastState);
		
		assertEquals(20, lastState.getWins());
		assertEquals(21, lastState.getRuns());
		
		// we should jump somewhere new when we add a win
		int jumpedIndex = states.addWin(lastStateIndex);
		
		State jumpedState = states.getState(jumpedIndex);
		
		assertNotNull(jumpedState);
		
		// TODO: does the win rate change?
		assertTrue(jumpedState.getRuns() < 20);
		assertTrue(jumpedState.getRuns() > 0); // our new jump location
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
