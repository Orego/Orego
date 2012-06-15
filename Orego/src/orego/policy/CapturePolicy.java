package orego.policy;

import static java.util.Arrays.*;
import static orego.core.Board.PLAY_OK;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.mcts.SearchNode;
import orego.util.*;
import orego.core.Board;
import ec.util.MersenneTwisterFast;

/**
 * Captures the largest ataried enemy chain on the board, with ties broken
 * randomly.
 */
public class CapturePolicy extends Policy {

	/** Number of stones captured by each move on the board. */
	private int[] captures;

	/** Moves that capture something. */
	private IntSet liberties;

	public CapturePolicy() {
		this(new RandomPolicy());
	}

	public CapturePolicy(Policy fallback) {
		super(fallback);
		captures = new int[FIRST_POINT_BEYOND_BOARD];
		liberties = new IntSet(FIRST_POINT_BEYOND_BOARD);
	}

	public Policy clone() {
		CapturePolicy result = (CapturePolicy) super.clone();
		result.captures = new int[FIRST_POINT_BEYOND_BOARD];
		result.liberties = new IntSet(FIRST_POINT_BEYOND_BOARD);
		return result;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		// Find out how many stones would be captured by each move
		fill(captures, 0);
		liberties.clear();
		int enemy = opposite(board.getColorToPlay());
		IntSet targets = board.getChainsInAtari(enemy);
		for (int i = 0; i < targets.size(); i++) {
			int c = targets.get(i);
			int liberty = board.getCapturePoint(c);
			liberties.add(liberty);
			captures[liberty] += board.getChainSize(c);
		}
		// Choose the biggest capture, with ties determined randomly
		while (!liberties.isEmpty()) {
			int result = NO_POINT;
			int biggestCapture = 0;
			int start = random.nextInt(liberties.size());
			for (int i = start; i < liberties.size(); i++) {
				int p = liberties.get(i);
				if (captures[p] > biggestCapture) {
					result = p;
					biggestCapture = captures[p];
				}
			}
			for (int i = 0; i < start; i++) {
				int p = liberties.get(i);
				if (captures[p] > biggestCapture) {
					result = p;
					biggestCapture = captures[p];
				}
			}
			// No need to check feasibility; a capture can't be an eye and must
			// be near another stone
			if (board.playFast(result) == PLAY_OK) {
				return result;
			} else {
				liberties.removeKnownPresent(result);
			}
		}
		// Fall back
		return getFallback().selectAndPlayOneMove(random, board);
	}

	@Override
	public void updatePriors(SearchNode node, Board board, int weight) {
		int enemy = opposite(board.getColorToPlay());
		for (int p : ALL_POINTS_ON_BOARD) {
			if ((board.getColor(p) == enemy) & (board.getChainId(p) == p)) {
				int liberty = board.getCapturePoint(p);
				if (liberty != NO_POINT) {
					node.addWins(liberty, board.getChainSize(p) * weight);
				}
			}
		}
		getFallback().updatePriors(node, board, weight);
	}

}
