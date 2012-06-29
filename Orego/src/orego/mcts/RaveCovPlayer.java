package orego.mcts;

import static java.lang.Double.NEGATIVE_INFINITY;
import static orego.core.Coordinates.PASS;
import orego.core.Board;

public class RaveCovPlayer extends RavePlayer {

	// private double raveBias;
	//
	// public RaveCovPlayer() {
	// raveBias = 0.0009;
	// }

	@Override
	protected SearchNode getPrototypeNode() {
		return new RaveNode();
	}
	
	/** Assuming UCT is unbiased and RAVE is biased,
     *  so the difference between the two estimates RAVE bias.
     *  This method returns the coefficient (weight beta) for the RAVE player,
     *  and it is the coefficient that minimizes the mean square error of the
     *  convex sum of RAVE and UCT.
     */
	public double raveCoefficient(double w, double rw, double c, double rc) {
		double raveBias = (rw / rc) - (w / c);  
		return (rc - c)
				* (w / c)
				* (1 - w / c)
				/ ( (rc - 2 * c) * (w / c) * (1 - w / c) + c * (rw / rc) * (1 - rw / rc)
						+ rc * c * raveBias * raveBias);
	}

	public double searchValue(SearchNode node, Board board, int move) {
		if (node.getWins(move) == Integer.MIN_VALUE) {
			return NEGATIVE_INFINITY;
		}
		if (move == PASS) {
			return ((double) node.getWins(move)) / node.getRuns(move);
		}
		RaveNode raveNode = (RaveNode) node;
		double c = raveNode.getRuns(move);
		double w = raveNode.getWins(move);
		double rc = raveNode.getRaveRuns(move);
		double rw = raveNode.getRaveWins(move);
		double coef = raveCoefficient(w, rw, c, rc);
		return (w / c) * (1 - coef) + (rw / rc) * coef;
	}

}
