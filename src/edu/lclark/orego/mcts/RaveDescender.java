package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static java.lang.Float.NEGATIVE_INFINITY;
import edu.lclark.orego.core.Board;

/** Descender using Rapid Action Value Estimation. */
public final class RaveDescender extends AbstractDescender {

	/**
	 * This corresponds to b^2/(0.5*0.5) in Silver's formula. The higher this
	 * is, the less attention is paid to RAVE.
	 */
	private final float raveBias;

	public RaveDescender(Board board, TranspositionTable table, int biasDelay) {
		super(board, table, biasDelay);
		raveBias = 0.0009f;
	}

	/**
	 * Returns the weight given to RAVE (as opposed to direct MC data) given c
	 * runs and rc RAVE runs.
	 */
	private float raveCoefficient(float c, float rc) {
		return rc / (rc + c + rc * c * raveBias);
	}

	/**
	 * Uses the formula from here: David Silver Reinforcement Learning and
	 * Simulation-Based Search in Computer Go
	 * http://papersdb.cs.ualberta.ca/~papersdb/uploaded_files/
	 * 1029/paper_thesis.pdf equation 8.40 page 107//Updated
	 */
	@Override
	public float searchValue(SearchNode node, short move) {
		if (node.getWinRate(move) < 0.0f) {
			return NEGATIVE_INFINITY;
		}
		if (move == PASS) {
			return node.getWinRate(move);
		}
		final RaveNode raveNode = (RaveNode) node;
		final float c = raveNode.getRuns(move);
		final float r = raveNode.getWinRate(move);
		final float rc = raveNode.getRaveRuns(move);
		final float rr = raveNode.getRaveWinRate(move);
		final float coef = raveCoefficient(c, rc);
		return r * (1 - coef) + rr * coef;
	}

}
