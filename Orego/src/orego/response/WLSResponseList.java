package orego.response;

import orego.core.Board;
import orego.core.Coordinates;
import orego.wls.WinLossStates;
import ec.util.MersenneTwisterFast;

/**
 * Uses the Win/Loss state graph for compressing the number of wins/runs we need to represent.
 * Since we use a byte array, the max end of scale for the WLS is 21.
 * @author sstewart
 *
 */
public class WLSResponseList extends AbstractResponseList {
	
	private long totalRuns;
	
	/** State indices into WLS table for all 361 moves. Indexed by move*/
	private byte[] moveStates;
	 
	
	public WLSResponseList() {
		moveStates = new byte[Coordinates.FIRST_POINT_BEYOND_BOARD];
		totalRuns = 0;
	}
	
	@Override
	public void addWin(int p) {
		// transition to the next state 
		moveStates[p] = (byte)WinLossStates.getWLS().addWin(moveStates[p]);
		totalRuns++;
	}
	
	@Override
	public void addLoss(int p) {
		// transition to next loss state
		moveStates[p] = (byte)WinLossStates.getWLS().addLoss(moveStates[p]);
		totalRuns++;

	}

	@Override
	public int bestMove(Board board, MersenneTwisterFast random) {
		// pick the legal move with the highest confidence
		// TODO: a different measure might be appropriate
		
		//for (int i = 0; i < )
		return 0;
	}

	@Override
	public long getTotalRuns() {
		return totalRuns;
	}

	@Override
	public double getWinRate(int p) {
		// get the confidence rating for the given move based on it's move index
		return WinLossStates.getWLS().getState(moveStates[p]).getConfidence();
	}

}
