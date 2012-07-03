package orego.policy;

import orego.core.Board;
import ec.util.MersenneTwisterFast;
import orego.mcts.SearchNode;
import orego.response.ResponsePlayer;

/**
 * Generates moves for playouts (beyond the search tree). Many subclasses will
 * override reset() and updatePriors().
 */
public abstract class Policy implements Cloneable {

	/** The Policy to use in case no moves are generated. */
	private Policy fallback;

	/**
	 * @param fallback
	 *            the policy to use if this one generates no move.
	 */
	public Policy(Policy fallback) {
		this.fallback = fallback;
	}

	public Policy clone() {
		try {
			Policy result = (Policy) super.clone();
			if (fallback != null) {
				result.fallback = (Policy) fallback.clone();
			}
			return result;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	/**
	 * Returns the fallback policy (used when this policy doesn't suggest a
	 * move).
	 */
	public Policy getFallback() {
		return fallback;
	}

	/**
	 * Generates and plays a move. Implementations of this method should, unless
	 * they return some other move, return
	 * getFallBack().selectAndPlayOneMove(random, board).
	 */
	public abstract int selectAndPlayOneMove(MersenneTwisterFast random,
			Board board);

	/**
	 * Updates the priors so that recommended moves will be tried sooner from
	 * search tree nodes.
	 */
	public void updatePriors(SearchNode node, Board board, int weight) {
		getFallback().updatePriors(node, board, weight);
	}
	
	/**
	 * updatePriors for ResponsePlayer
	 */
	public void updateResponses(ResponsePlayer player, Board board, int weight) {
		getFallback().updateResponses(player, board, weight);
	}
}