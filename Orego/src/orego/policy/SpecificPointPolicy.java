package orego.policy;

import static orego.core.Coordinates.NO_POINT;
import orego.core.Board;
import orego.mcts.SearchNode;
import ec.util.MersenneTwisterFast;

/**
 * This policy always recommends one particular move, specified at construction
 * time. If no point is specified, NO_POINT is used.
 * 
 * This policy is useful in tests.
 */
public class SpecificPointPolicy extends Policy {

	/** The point to suggest. */
	private int point;

	/** The policy always recommends NO_POINT. */
	public SpecificPointPolicy() {
		this(NO_POINT);
	}

	/**
	 * @param point
	 *            the point to recommend.
	 */
	public SpecificPointPolicy(int point) {
		super(null);
		this.point = point;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		return point;
	}

	@Override
	public void updatePriors(SearchNode node, Board board, int weight) {
		if (point != NO_POINT) {
			node.addWins(point, weight);
		}
	}

}
