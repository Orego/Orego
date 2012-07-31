package orego.heuristic;

import orego.core.*;
import static java.util.Arrays.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.util.*;

/**
 * Tries to save any friendly group put in atari by the last move, by capturing or extending.
 * The value of a move is number of stones saved + number of stones captured (if any).
 * A move that captures several chains adjacent to the threatened friendly group, or captures while extending, gets additional value.
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

	@Override
	public void prepare(Board board) {
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
					if ((capturePoint != NO_POINT) && !board.isSelfAtari(capturePoint, color)) {
						friends.add(chain);
						// Add value for extending here
						int size = board.getChainSize(chain);
						increaseValue(capturePoint, size);
						findCapturableEnemies(chain, size, opposite(color), board);
					}
				}
			}
		}
	}
	
	// TODO Move this up to Heuristic
	protected void increaseValue(int point, int amount) {
		getValues()[point] += amount;
		IntSet nonzeroPoints = getNonzeroPoints();
		nonzeroPoints.add(point);
		if ((getBestIndex() == -1) || (getValues()[point] > getValues()[getNonzeroPoints().get(getBestIndex())])) {
			// TODO IntSet can do this directly, faster
			for (int i = 0; i < nonzeroPoints.size(); i++) {
				if (nonzeroPoints.get(i) == point) {
					setBestIndex(i);
				}
			}
		}
	}
	
	/** Adds value for moves that capture adjacent enemies of chain. */
	protected void findCapturableEnemies(int chain, int size, int enemyColor, Board board) {
		int p = chain;
		int[] next = board.getChainNextPoints();
		do {
			for (int i = 0; i < 4; i++) {
				int target = board.getChainId(NEIGHBORS[p][i]);
				if ((board.getColor(target) == enemyColor) && !targets.contains(target)){
					int capturePoint = board.getCapturePoint(target);
					if (capturePoint != NO_POINT) {
						increaseValue(capturePoint, board.getChainSize(target) + size);
					}
				}
			}
			p = next[p];
		} while (p != chain);
	}

}
