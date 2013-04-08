package orego.wls;

/**
 LICENSE:
 ========

 Copyright (c) 2011 Jacques Basald'a.
 All rights reserved.

 Redistribution and use in source and binary forms are permitted
 provided that the above copyright notice and this paragraph are
 duplicated in all such forms and that any documentation,
 advertising materials, and other materials related to such
 distribution and use acknowledge that the software was developed
 by the <organization>.  The name of the author may not be used to
 endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

 */

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Random;

/**
 * Implementation of W/L states
 * 
 * Note: you should *never* maintain references to State but instead hold a byte index.
 * If you hold a reference to State you are effectively removing the benefit of having this alternative data structure.
 * 
 * Note: We don't advise changing the {@link WinLossStates#CONFIDENCE_LEVEL} as the {@link WinLossStaates#JUMP_CONSTANT_K} is tuned based on a given confidence level (currently 75%).
 * Tips for author:
 * Make note of JUMP_CONSTANT_K
 * 
 * TODO: should we synchronize this class?
 * 
 * Note: we store the different states as ints but if you choose a maximum end scale of 21, you can use bytes
 * to hold the states/action indicies.
 * 
 * @author sstewart
 *
 */
public class WinLossStates {

	// confidence level for interval estimation
	// we use ~75% level because JUMP_CONSTANT_K was tuned with this level 
	public static double CONFIDENCE_LEVEL = .74857;
	
	// our sliding "window" for the "resolution" of our encoding
	public static int END_SCALE = 21;
	
	public static final int MINIMAL_CONFIDENCE = -10;
	
	// compute the total number of states which is sum(1...[e+1]).
	// we write it in closed form. Includes an extra +1 for 0/0 entry
	public static int NUM_STATES = Integer.MAX_VALUE;
	
	public static double WIN_THRESHOLD = 0.5000; // TODO: probably shouldn't be static and changable...
	
	public static final int NO_STATE_EXISTS = Integer.MAX_VALUE;
	
	public Visualizer visualizer = new Visualizer();
	
	private static WinLossStates wls;
	
	// constant used when jumping. Tuned empirically for END_SCALE = 21
	// TODO: Can we do this automatically in the future?
	// WARNING: this constant is dependent upon our CONFIDENCE_LEVEL 
	// since it is empirically tuned with a given confidence level.
	public static double JUMP_CONSTANT_K = 1.3; 
	
	private int[] WIN;

	private int[] LOSS;

	private State[] states;
	
	// TODO: configuration of properties such as END_SCALE, WIN_THRESHOLD, etc
	public WinLossStates(double confidence, int end_scale) {
		CONFIDENCE_LEVEL = confidence;
		END_SCALE 		 = end_scale;
		
		computeNumberOfStates();
		
		WIN  = new int[NUM_STATES];
		LOSS = new int[NUM_STATES];
		states = new State[NUM_STATES];
		
		for (int i = 0; i < NUM_STATES; i++) {
			states[i] = new State(-1, -1);
		}
		
		buildTables();
	}

	public WinLossStates() {
 		this(.74857, 21);
	}
	
	/** A class for doing data visualizations of the WLS algorithm */
	public class Visualizer {
		/** Creates an R csv file for plotting the various proportions with labels
		 * @return The R csv contents
		 */
		public String visualizeStates() {
			StringBuilder builder = new StringBuilder(200);
			builder.append("confidences, wrproportions, labels\n");
			for (int i = 0; i < NUM_STATES; i++) {
				builder.append(states[i].getConfidence() + ", " + states[i].getWinRunsProportion() + ",  \"WR: " + states[i].getWins() + "/" + states[i].getRuns() + "\"\n"); 
			}
	
			return builder.toString();
		}
		
		/**
		 * Creates an R csv for visualizing the progression as we add wins/losses.
		 * Produces a graph of states against time. The states have labels and are
		 * scored by their proportion.
		 * @return The R csv contents
		 */
		public String visualizeStatePath( int num_iterations) {
			StringBuilder builder = new StringBuilder(200);
			// add some random wins with probability of about 3 / 5
		
			Random rand = new Random();
			final double expected_value = 0.60;
			int stateIndex = 0;
			
			builder.append("WL, Time, Label\n");
			for (int i = 0; i < num_iterations; i++) {
				// plot the current state
				State state = getState(stateIndex);
				
				// Ex: .75, 1203, 3/4
				builder.append(state.getWinRunsProportion() + ", " + i + ", \"WR: " + state.getWins() + "/" + state.getRuns() + "\"\n");
				
				// 60% of the time add a new win
				if (rand.nextDouble() < expected_value)
					stateIndex = addWin(stateIndex); // transition to new state based on win
				else
					stateIndex = addLoss(stateIndex); // transition to new state based on loss
			}
		
			return builder.toString();
		}
		
	}
	
	/** Gets the singleton instance of our WinLossState class*/
	public static WinLossStates getWLS() {
		if (wls == null) {
			synchronized (WinLossStates.class) {
				wls = new WinLossStates();
			}
		}
		
		return wls;
	}
	
	private void computeNumberOfStates() {
		// using closed form and add +1 to END_SCALE for initial state 0
		NUM_STATES = ((END_SCALE + 1) * (END_SCALE + 1 + 1) / 2);
	}
	
	/**
	 * Gets the next state index (for a win) from a given state.
	 * Effectively transitions *and* increments to the next state.
	 * @param stateAction the current (state, action). Just an index.
	 */
	public int addWin(int stateAction) {
		return WIN[stateAction];
	}
	
	/**
	 * Gets the next state index (for a loss) from a given state.
	 * Effectively transitions *and* de-increments. 
	 * @param stateAction the current (state,action). Just an index.
	 */
	public int addLoss(int stateAction) {
		return LOSS[stateAction];
	}
	
	/**
	 * Gets a state for a given index
	 * @param stateAction The state action pair (really just an index)
	 * @return State the state for the given index or null if out of bounds
	 */
	public State getState(int stateAction) {
		if (stateAction >= states.length) return null;
		
		return states[stateAction];
	}
	
	/**
	 * Gets the initial 0/0 state
	 * @return the initial state
	 */
	public State getInitialState() {
		return getState(0);
	}
	
	public int getTotalStates() {
		return NUM_STATES;
	}
	
	protected void buildTables() {
		
		int curState = 0;
		// Initializing states
		// loop through all the different number of "denominators" or 
		// number of runs. (we +1 because we want to include the first one at 0 up to END_SCALE)
		for (int i = 0; i < END_SCALE + 1; i++) {
			
			// For each "level" in the tree (see figure 5 in the paper)
			// create a series of fractions with the number of wins increasing as numerator
			for (int j = 0; j <= i; j++) {
				State state = states[curState];
				
				state.setWins(j);
				state.setRuns(i);
				curState++;
				
				if (i == 0) {
					// undefined for first state (lowest)
					state.setConfidence(MINIMAL_CONFIDENCE);
					continue;
				}
				
				// compute the statistical "strength" of this proportion
				state.computeConfidence(WIN_THRESHOLD);
			}
		}
		
		Arrays.sort(states); // sort according to confidence
		
		// build the "directed graph" of win/loss state transitions
		// We also find the various jump moves at the terminal "saturated states"
		// we start at 0 and chain together the appropriate states.
		for (int stateActionIndex = 0; stateActionIndex < NUM_STATES; stateActionIndex++) {
			State state = states[stateActionIndex];
			
			// where do we go after a win?
			WIN[stateActionIndex]  = findJumpIndexForWin(state.getWins(),   state.getRuns());
			
			// where do we go after a loss?
			LOSS[stateActionIndex] = findJumpIndexForLoss(state.getWins(),  state.getRuns());
					
		}
	}

	/**
	 * Finds the appropriate jump index for the given win/run ratio for a win
	 * @return the new state action index
	 */
	
	protected int findJumpIndexForWin(int wins, int runs) {
		int existingStateIndex = findStateIndex(wins + 1, runs + 1);
		
		if (existingStateIndex == NO_STATE_EXISTS) {
			// since it doesn't exist, we've hit a "saturated" state
			// find the jump state for our wins/runs ratio
			existingStateIndex = findSaturatedJumpIndex(wins, runs, true);
		}
		
		return existingStateIndex;
	}
	/**
	 * Finds the appropriate jump index for the given win/run ratio for a loss
	 * @return the new state action index
	 */
	protected int findJumpIndexForLoss(int wins, int runs) {
		// don't record a win while incrementing the runs indicating a loss
		int existingStateIndex = findStateIndex(wins, runs + 1); 
		
		if (existingStateIndex == NO_STATE_EXISTS) {
			// since it doesn't exist, we've hit a "saturated" state
			// find the jump state for our wins/runs ratio
			existingStateIndex = findSaturatedJumpIndex(wins, runs, false);
		}
		
		
		return existingStateIndex;
	}
	
	/**
	 * Finds the index of a given state based on the specified number of wins/losses
	 * @param wins number of wins
	 * @param runs number of runs
	 * @param didWin Did we win before arriving at wins/runs?
	 * @return The index of the state or NO_STATE_EXISTS 
	 */
	public int findStateIndex(int wins, int runs) {
		for (int i = 0; i < NUM_STATES; i++) {
			if (states[i].getWins() == wins && 
				states[i].getRuns() == runs   )
				return i;
		}
		
		return NO_STATE_EXISTS;
	}

	/**
	 * Finds the index of the state in the states array
	 * @param state The state 
	 * @return the index of the state or NO_STATE_EXISTS
	 */
	public int findStateIndex(State state) {
		return findStateIndex(state.getWins(), state.getRuns());
	}
	
	/**
	 * Finds a state for the given win/loss
	 * @return A {@link State} object if the state exists, otherwise null
	 */
	public State findState(int wins, int runs) {
		int index = findStateIndex(wins, runs);
		
		return (index != NO_STATE_EXISTS ? states[index] : null);
	}
	
	/**
	 * Returns the index to jump to when we have a "saturated" proportion: n/m where m = e (end of scale).
	 * This function should not be called *unless* m = e (the proportion is saturated).
	 * We begin to lose useful information because we are literally "off the charts".
	 * 
	 * Hence, we perform a small re-adjustment which allows the proportion to jump back and "churn" back up to the current state.
	 * You should never call this function with
	 * This guarantees that any truly saturated states are extremely "rich" with information and hence become stationary.
	 * Once the number or wins *and* runs are equal to end of scale we have as much information as possible and hence
	 * we stay put.
	 * @param wins The number of wins (should be less than or equal to END_SCALE)
	 * @param runs The number of runs (should be less than or equal to END_SCALE)
	 * @param didWin Did we win in the last run?
	 * @return int The index to which we will jump to force a sort of "confirmation"
	 */
	protected int findSaturatedJumpIndex(int wins, int runs, boolean didWin) {
		// number of runs we are going to 'jump' to.
		// In effect we are changing the denominator in proportion to deviation from 1/2.
		// This performs a "jump" to an earlier state by simply looking for a new proportion with our new, target runs
		// See the original paper for the jump formula
		// TODO: we might have to change 1/2 for binary values
		// Jump in proportion to deviance from 1/2. The farther away from 1/2 the farther we jump to avoid 
		// getting "stuck" at END_SCALE/END_SCALE or 0/END_SCALE states.
		
		// Note: JUMP_CONSTANT_K is dependent upon the CONFIDENCE_LEVEL as it is emperically tuned
		int jumpRuns = (int)(runs - Math.round(JUMP_CONSTANT_K * (double) runs * Math.abs((double) wins / (double)runs - 1.0/2.0)));
		
		// find our current state's confidence (should exist since wins <= END_SCALE and runs <= END_SCALE)
		State saturatedState 	= states[findStateIndex(wins, runs)];
		int saturatedStateIndex = findStateIndex(saturatedState);
		
		if (didWin) {
			if (wins == END_SCALE) return findStateIndex(END_SCALE, END_SCALE); // fully saturated
			
			// The proportion's confidence should be better than our current proportion (with appropriate run count "jumpRuns").
			// We effectively punish by reducing the number of wins and runs but we increase our confidence.
			// TODO: but do higher confidences imply smaller proportions?
			
			// we can use the fact that the states are sorted by confidence. 
			// We start at the current index and find the next highest confidence proportion
			// we skip state 0/0 since irrelevant
			for (int i = saturatedStateIndex + 1; i < NUM_STATES; i++) {
				
				if (states[i].getRuns() != jumpRuns) continue; // break early if we don't have the appropriate number of runs
				
				if (states[i].getConfidence() > saturatedState.getConfidence())
					return i;
			}
			
		} else {
			if (wins == 0) return findStateIndex(0, END_SCALE); // fully saturated (lost as many times as possible)

			
			// We pick the "confidence" smaller than our current proportion.
			// We are effectively punishing by reducing the lose count and
			// reducing our confidence level.
			
			// TODO: but what about the rule: "must be a bigger proportion". Is that implied by a smaller confidence?
			
			// since the states are sorted by confidence, we can work our way down (from the current index) 
			// the list and we are guaranteed to find the *biggest* confidence less
			// than the current confidence
			
			// we skip state 0/0 since irrelevant
			for (int i = saturatedStateIndex - 1; i >= 1; i--) {
				
				if (states[i].getRuns() != jumpRuns) continue; // break early if doesn't have appropriate number of runs
					
				if (states[i].getConfidence() < saturatedState.getConfidence())
					return i;

			}
		} 
		
		throw new UnsupportedOperationException("Could not find jump index (target: " + jumpRuns + ") for wins: " + wins + " and runs: " + runs + " with win status: " + didWin);
	};
};
