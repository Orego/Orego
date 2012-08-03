package orego.heuristic;

import ec.util.MersenneTwisterFast;
import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.util.*;

/**
 * Tries to save any friendly group put in atari by the last move, by capturing
 * or extending.
 */
public class EscapeHeuristic extends Heuristic {

	/** Friendly groups put in atari by the last move. */
	private IntSet friends;

	/** Enemy chains in atari adjacent to friends. */
	private IntSet targets;

	public EscapeHeuristic(int weight) {
		super(weight);
		friends = new IntSet(FIRST_POINT_BEYOND_BOARD);
		targets = new IntSet(FIRST_POINT_BEYOND_BOARD);
	}

	/** Recommends moves that capture adjacent enemies of chain. */
	protected void escapeByCapturing(int chain, int enemyColor,
			Board board) {
		int p = chain;
		int[] next = board.getChainNextPoints();
		do {
			for (int i = 0; i < 4; i++) {
				int neighbor = NEIGHBORS[p][i];
				if (board.getColor(neighbor) == enemyColor) {
					int target = board.getChainId(neighbor);
					if (!targets.contains(target)) {
						targets.add(target);
						int capturePoint = board.getCapturePoint(target);
						if (capturePoint != NO_POINT) {
							recommend(capturePoint);
						}
					}
				}
			}
			p = next[p];
		} while (p != chain);
	}

	@Override
	public void prepare(Board board) {
		super.prepare(board);
		int lastMove = board.getMove(board.getTurn() - 1);
		int color = board.getColorToPlay();
		// Find friendly groups in danger
		friends.clear();
		targets.clear();
		for (int i = 0; i < 4; i++) {
			int neighbor = NEIGHBORS[lastMove][i];
			if (board.getColor(neighbor) == color) {
				int chain = board.getChainId(neighbor);
				if (!friends.contains(chain)) {
					int capturePoint = board.getCapturePoint(chain);
					if (capturePoint != NO_POINT) {
						friends.add(chain);
						// Consider escaping by capturing
						escapeByCapturing(chain, opposite(color), board);
						// Consider escaping by extending
						if (!board.isSelfAtari(capturePoint, color)) {
							recommend(capturePoint);
						}
					}
				}
			}
		}
	}

}
