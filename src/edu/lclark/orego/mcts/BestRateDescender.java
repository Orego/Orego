package edu.lclark.orego.mcts;

import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.util.*;
import static edu.lclark.orego.move.Mover.*;
import static edu.lclark.orego.core.CoordinateSystem.*;

/** Always chooses the move with the best win rate, with no exploration. */
public final class BestRateDescender implements TreeDescender {

	private final Board board;
	
	private final TranspositionTable table;
	
	public BestRateDescender(Board board, TranspositionTable table) {
		this.board = board;
		this.table = table;
	}

	@Override
	public void clear() {
		// Nothing to do; the TreeUpdater clears the table
	}

	// TODO Should this method live in table?
	/** Returns the root node (creating it if necessary). */
	private SearchNode getRoot() {
		return table.findOrAllocate(board.getFancyHash());
	}

	/** Returns the best move to make from here during a playout. */
	private static short bestSearchMove(SearchNode node, Board runnableBoard,
			MersenneTwisterFast random) {
		short result = node.getWinningMove();
		if ((result != NO_POINT) && runnableBoard.isLegal(result)) {
			// The isLegal() check is necessary to avoid superko violations
			return result;
		}
		double best = searchValue(node, PASS);
		result = PASS;
		ShortSet vacantPoints = runnableBoard.getVacantPoints();
		int start;
		start = random.nextInt(vacantPoints.size());
		int i = start;
		int skip = PRIMES[random.nextInt(PRIMES.length)];
		do {
			short move = vacantPoints.get(i);
			double searchValue = searchValue(node, move);
			if (searchValue > best) {
				if (runnableBoard.isLegal(move)) {
					best = searchValue;
					result = move;
				} else {
					node.exclude(move);
				}
			}
			// Advancing by a random prime skips through the array
			// in a manner analogous to double hashing.
			i = (i + skip) % vacantPoints.size();
		} while (i != start);
		return result;
	}

	private static double searchValue(SearchNode node, short move) {
		return node.getWins(move);
	}

	/** Selects and plays one move in the search tree. */
	private static short selectAndPlayMove(SearchNode node, McRunnable runnable) {
		short move = bestSearchMove(node, runnable.getBoard(),
				runnable.getRandom());
		runnable.acceptMove(move);
		return move;
	}

	@Override
	public void descend(McRunnable runnable) {
		SearchNode node = getRoot();
		assert node != null : "Fancy hash code: " + board.getFancyHash();
		while (runnable.getBoard().getPasses() < 2) {
			selectAndPlayMove(node, runnable);
			SearchNode child = table.findIfPresent(runnable.getBoard()
					.getHash());
			if (child == null) {
				return; // No child
			}
			node = child;
		}
	}

}
