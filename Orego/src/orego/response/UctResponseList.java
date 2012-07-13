package orego.response;

import static java.lang.Math.log;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import orego.core.Board;
import orego.core.Coordinates;
import ec.util.MersenneTwisterFast;

public class UctResponseList extends RawResponseList {
	
	@Override
	public int bestMove(Board board, MersenneTwisterFast random) {
		double bestValue = 0;
		int bestMove = Coordinates.PASS;
		for (int p: Coordinates.ALL_POINTS_ON_BOARD) {
			if(!(board.isLegal(p) && board.isFeasible(p)))
				continue;
			double value = searchValue(wins[p], runs[p]);
			if (value >= bestValue) {
				bestValue = value;
				bestMove = p;
			}
		}
		return bestMove;
	}
	
	public double searchValue(int wins, int runs) {
		// The variable names here are chosen for consistency with the tech
		// report
		double barX = wins / (double) runs;
		double logParentRunCount = log(getTotalRuns());
		// In the paper, term1 is the mean of the SQUARES of the rewards; since
		// all rewards are 0 or 1 here, this is equivalent to the mean of the
		// rewards, i.e., the win rate.
		double term1 = barX;
		double term2 = -(barX * barX);
		double term3 = sqrt(2 * logParentRunCount / runs);
		double v = term1 + term2 + term3; // This equation is above Eq. 1
		double factor1 = logParentRunCount / runs;
		double factor2 = min(0.25, v);
		double uncertainty = 0.4 * sqrt(factor1 * factor2);
		return uncertainty + barX;
	}
}
