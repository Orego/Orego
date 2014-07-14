package edu.lclark.orego.mcts;

import static java.lang.Float.NEGATIVE_INFINITY;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.mcts.RaveNode;
import edu.lclark.orego.core.Board;

public final class RaveDescender extends AbstractDescender{
	
	/**
	 * This corresponds to b^2/(0.5*0.5) in Silver's formula. The higher this
	 * is, the less attention is paid to RAVE.
	 */
	private float raveBias;

	public RaveDescender(Board board, TranspositionTable table, int biasDelay) {
		super(board, table, biasDelay);
		raveBias = 0.0009f;
	}
	
	/**
	 * Uses the formula from here:
	 * David Silver 
	 * Reinforcement Learning and Simulation-Based Search in Computer Go 
	 * A thesis submitted to the Faculty of Graduate Studies and Research 
	 *	in partial ful���llment of the requirements for the degree of 
	 *	Doctor of Philosophy 
	 *	Department of Computing Science 
	 * http://papersdb.cs.ualberta.ca/~papersdb/uploaded_files/1029/paper_thesis.pdf
	 * equation 8.40 page 107//Updated
	 */
	@Override
	float searchValue(SearchNode node, short move){
		if (node.getWins(move) == Integer.MIN_VALUE) {
			return NEGATIVE_INFINITY;
		}
		if (move == PASS) {
			return node.getWinRate(move);
		}
		RaveNode raveNode = (RaveNode) node;
		float c = raveNode.getRuns(move);
		float r = raveNode.getWinRate(move);
		float rc = raveNode.getRaveRuns(move);
		float rr = raveNode.getRaveWinRate(move);
		float coef = raveCoefficient(c, rc);
		return r * (1 - coef) + rr * coef;
	}
	
	/**
	 * Returns the weight given to RAVE (as opposed to direct MC data) given c
	 * runs and rc RAVE runs.
	 */
	protected float raveCoefficient(float c, float rc) {
		return rc / (rc + c + rc * c * raveBias);
	}

}
