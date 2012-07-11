package orego.response;

import orego.core.Board;
import orego.core.Coordinates;
import orego.util.IntSet;
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
		// we give each move a starting win rate of 50%
		for (int move : Coordinates.ALL_POINTS_ON_BOARD) {
			moveStates[move] = (byte) WinLossStates.getWLS().findStateIndex(AbstractResponseList.NORMAL_WINS_PRIOR, 
																			AbstractResponseList.NORMAL_RUNS_PRIOR);
			
		}
		
		// we make the pass move worse (.10 win rate) than the other moves so we don't pass too often
		moveStates[Coordinates.PASS] = (byte) WinLossStates.getWLS().findStateIndex(AbstractResponseList.PASS_WINS_PRIOR, 
																					AbstractResponseList.PASS_RUNS_PRIOR);
		
		
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
		IntSet vacantPoints = board.getVacantPoints();
		int start = random.nextInt(vacantPoints.size());
		
		int i = start;
		double bestValue = PASS_WINS_PRIOR / PASS_RUNS_PRIOR;
		
		int bestMove = Coordinates.PASS;
		do {
			int move = vacantPoints.get(i);
			double searchValue = getWinRate(move);
			
			if (searchValue > bestValue) {
				if (board.isFeasible(move) && board.isLegal(move)) {
					bestValue = searchValue;
					bestMove = move;
				}
			}
			
			// make sure we hit all the elements
			i = (i + 457) % vacantPoints.size();
		} while (i != start);
		
		return bestMove;
	}

	@Override
	public long getTotalRuns() {
		return totalRuns;
	}

	@Override
	public double getWinRate(int p) {
		// get the confidence rating for the given move based on it's move index
		// TODO: this might be problematic.....
		return WinLossStates.getWLS().getState(moveStates[p]).getWinRunsProportion();
	}

}
