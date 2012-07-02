package orego.wls;

/**
 LICENSE:
 ========

 Copyright (c) 2011 Jacques Basaldúa.
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

import java.util.Arrays;

public class WinLossStates {


	public static double CONFIDENCE_LEVEL = .95; // confidence level for interval estimation
	
	// our sliding "window" for the "resolution" of our encoding
	public static int END_SCALE = 21;
	
	// compute the total number of states which is sum(1...[e+1]).
	// we write it in closed form. Includes an extra +1 for 0/0 entry
	public static int NUM_STATES = Integer.MAX_VALUE;
	
	public static double WIN_THRESHOLD = 0.5000; // TODO: probably shouldn't be static and changable...
	
	public static final int NO_STATE_EXISTS = Integer.MAX_VALUE;
	
	public Visualizer visualizer = new Visualizer();
	
	// constant used when jumping. Tuned empirically for END_SCALE = 21
	// Can we do this automatically in the future?
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
			states[i] = new State(Integer.MIN_VALUE, Integer.MIN_VALUE);
		}
		
		buildTables();
	}

	public WinLossStates() {
 		this(.95, 21);
	}
	
	/** A class for doing data visualizations of the WLS algorithm */
	public class Visualizer {
		/** Creates an R csv file for plotting the various proportions with labels
		 * @return
		 */
		public String visualizeStates() {
			StringBuilder builder = new StringBuilder(200);
			builder.append("confidences, wrproportions, labels\n");
			for (int i = 0; i < NUM_STATES; i++) {
				builder.append(states[i].getConfidence() + ", " + states[i].getWinRunsProportion() + ",  \"WR: " + states[i].getWins() + "/" + states[i].getRuns() + "\"\n"); 
			}
	
			return builder.toString();
		}
	}
	
	/** Gets the singleton instance of our WinLossState class*/
	public static WinLossStates getWLS() {
		// TODO: make constructor private and make a new instance if it doesn't exist
		return null;
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
	 * @return State the state for the given index
	 */
	public State getState(int stateAction) {
		return states[stateAction];
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
				state.setStateIndex(curState);
				curState++;
				
				if (i == 0) {
					// undefined for first state (lowest)
					state.setConfidence(Double.MIN_VALUE);
					continue;
				}
				
				// compute the statistically "strength" of this proportion
				state.computeConfidence(WIN_THRESHOLD);
			}
		}
		
		Arrays.sort(states); // sort according to confidence
		
		
		// build the "directed graph" of win/loss state transitions
		// We also find the various jump moves at the terminal "saturated states"
		// we start at 0 and chain together the appropriate states.
		for (int stateActionIndex = 0; stateActionIndex < NUM_STATES; stateActionIndex++) {
			// where do we go after a win?
			WIN[stateActionIndex] = findJumpIndexForWin(states[stateActionIndex].getWins(),   states[stateActionIndex].getRuns());
			
			// where do we go after a loss?
			LOSS[stateActionIndex] = findJumpIndexForLoss(states[stateActionIndex].getWins(), states[stateActionIndex].getRuns());
					
		}
		
	}

	/**
	 * Finds the appropriate jump index for the given win/run ratio for a win
	 * @return int the new state action index
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
	 * @return int the new state action index
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
	 * @return
	 */
	protected int findStateIndex(int wins, int runs) {
		for (int i = 0; i < NUM_STATES; i++) {
			if (states[i].getWins() == wins && 
				states[i].getRuns() == runs   )
				return i;
		}
		
		
		return NO_STATE_EXISTS;
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
		
		int jumpRuns = (int)(runs - Math.round(JUMP_CONSTANT_K * (double) runs * Math.abs((double) wins / (double)runs - 1.0/2.0)));
		// find our current state's confidence (should exist since wins <= END_SCALE and runs <= END_SCALE)
		State saturatedState = states[findStateIndex(wins, runs)];
		
		if (didWin) {
			
			
			if (wins == END_SCALE) return findStateIndex(END_SCALE, END_SCALE); // fully saturated
			
			// The proportion's confidence should be better than our current proportion (with appropriate run count "jumpRuns").
			// We effectively punish by reducing the number of wins and runs but we increase our confidence.
			// TODO: but do higher confidences imply smaller proportions?
			
			// we can use the fact that the states are sorted by confidence. 
			// We start at the current index and find the next highest confidence proportion
			// we skip state 0/0 since irrelevant
			for (int i = saturatedState.getStateIndex() + 1; i < NUM_STATES; i++) {
				
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
			for (int i = saturatedState.getStateIndex() - 1; i >= 1; i--) {
				
				if (states[i].getRuns() != jumpRuns) continue; // break early if doesn't have appropriate number of runs
					
				if (states[i].getConfidence() < saturatedState.getConfidence())
					return i;

			}
			
			
		} 
		return Integer.MAX_VALUE;
	};
};
