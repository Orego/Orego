package orego.policy;

import ec.util.MersenneTwisterFast;
import orego.core.Board;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static orego.core.Board.*;
import orego.mcts.SearchNode;
import orego.util.*;

/** Tries to escape when in atari. */
public class EscapePolicy extends Policy {

	/** Last liberty (if any) of each of the neighbors of the last move. */
	private int[] capturePoints;

	/** Set of chains already considered. */
	private IntSet testedChains;

	/** Defaults to random fallback if none is specified. */
	public EscapePolicy() {
		this(new RandomPolicy());
	}

	public EscapePolicy(Policy fallback) {
		super(fallback);
		testedChains = new IntSet(FIRST_POINT_BEYOND_BOARD);
		capturePoints = new int[4];
	}

	/**
	 * Returns the location of a move, if there is one, that would capture a
	 * neighbor of the chain including point n.
	 */
	protected int capture(Board board, int n) {
		int next = n;
		do {
			for (int j = 0; j < 4; j++) {
				int m = NEIGHBORS[next][j];
				if (board.getColor(m) == opposite(board.getColorToPlay())) {
					int capturePoint = board.getCapturePoint(m);
					if (capturePoint != NO_POINT) {
						int legality = board.playFast(capturePoint);
						if (legality == PLAY_OK) {
							return capturePoint;
						}
					}
				}
			}
			next = board.getChainNextPoints()[next];
		} while (next != n);
		return NO_POINT;
	}

	public Policy clone() {
		EscapePolicy result = (EscapePolicy) super.clone();
		result.testedChains = new IntSet(FIRST_POINT_BEYOND_BOARD);
		result.capturePoints = new int[4];
		return result;
	}

	/**
	 * Returns liberty unless it is a self-atari or is illegal according to
	 * board.playFast(). Otherwise returns NO_POINT.
	 */
	protected int escape(Board board, int liberty) {
		if (board.isSelfAtari(liberty, board.getColorToPlay())
				|| board.playFast(liberty) != PLAY_OK) {
			return NO_POINT;
		}
		return liberty;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int lastPlay = board.getMove(board.getTurn() - 1);
		for (int i = 0; i < 4; i++) {
			int n = NEIGHBORS[lastPlay][i];
			if (board.getColor(n) == board.getColorToPlay()) {
				capturePoints[i] = board.getCapturePoint(n);
				if (capturePoints[i] != NO_POINT) {
					// Try to escape by capturing
					int capturePlay = capture(board, n);
					if (capturePlay != NO_POINT) {
						return capturePlay;
					}
				}
			} else {
				capturePoints[i] = NO_POINT;
			}
		}
		for (int i = 0; i < 4; i++) {
			if (capturePoints[i] != NO_POINT) {
				// Try to escape by extending
				int result = escape(board, capturePoints[i]);
				if (result != NO_POINT) {
					return result;
				}
			}
		}
		return getFallback().selectAndPlayOneMove(random, board);
	}

	@Override
	public void updatePriors(SearchNode node, Board board, int weight) {
		int lastPlay = board.getMove(board.getTurn() - 1);
		testedChains.clear();
		for (int i = 0; i < 4; i++) {
			int n = NEIGHBORS[lastPlay][i];
			if (board.getColor(n) == board.getColorToPlay()) {
				if (!testedChains.contains(board.getChainId(n))) {
					testedChains.add(board.getChainId(n));
					int liberty = board.getCapturePoint(n);
					if (liberty != NO_POINT) {
						// This friendly group is in atari. Try to run...
						node.addWins(liberty, weight);
						// ... or capture one of its neighbors
						int next = n;
						do {
							for (int j = 0; j < 4; j++) {
								int m = NEIGHBORS[next][j];
								if (board.getColor(m) == opposite(board.getColorToPlay())
										&& !testedChains.contains(board.getChainId(m))) {
									testedChains.add(board.getChainId(m));
									int capturePoint = board.getCapturePoint(m);
									if (capturePoint != NO_POINT) {
										node.addWins(capturePoint, weight);
									}
								}
							}
							next = board.getChainNextPoints()[next];
						} while (next != n);
					}
				}
			}
		}
		getFallback().updatePriors(node, board, weight);
	}

}
