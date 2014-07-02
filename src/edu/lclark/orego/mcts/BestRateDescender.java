package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.move.Mover.PRIMES;
import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.util.ShortSet;

/** Always chooses the move with the best win rate, with no exploration. */
public class BestRateDescender implements TreeDescender {

	/** Priors are not updated unless there have been this many runs through a node. */
	private final int biasDelay;

	private final Board board;

	private final TranspositionTable table;

	public BestRateDescender(Board board, TranspositionTable table, int biasDelay) {
		this.board = board;
		this.table = table;
		this.biasDelay = biasDelay;
	}

	@Override
	public short bestPlayMove() {
		double best = 1;
		short result = PASS;
		final ShortSet vacantPoints = board.getVacantPoints();
		final SearchNode root = getRoot();
		do {
			best = root.getWins(PASS);
			// If the move chosen on the last pass was illegal (e.g., a superko
			// violation that was never actually tried in a playout),
			// throw it out
			if (result != PASS) {
				root.exclude(result);
				result = PASS;
			}
			for (int i = 0; i < vacantPoints.size(); i++) {
				final short move = vacantPoints.get(i);
				if (root.getWins(move) > best) {
					best = root.getWins(move);
					result = move;
				}
			}
		} while (result != PASS && !board.isLegal(result));
		// TODO Handle resignation
//		// Consider resigning
//		if (node.getWinRate(result) < RESIGN_PARAMETER) {
//			return RESIGN;
//		}
		return result;
	}

	/** Returns the best move to make from here during a playout. */
	short bestSearchMove(SearchNode node, McRunnable runnable) {
		final Board runnableBoard = runnable.getBoard();
		final MersenneTwisterFast random = runnable.getRandom();
		short result = node.getWinningMove();
		if (result != NO_POINT && runnableBoard.isLegal(result)) {
			// The isLegal() check is necessary to avoid superko violations
			return result;
		}
		float best = searchValue(node, PASS);
		result = PASS;
		final ShortSet vacantPoints = runnableBoard.getVacantPoints();
		int start;
		start = random.nextInt(vacantPoints.size());
		int i = start;
		final int skip = PRIMES[random.nextInt(PRIMES.length)];
		do {
			final short move = vacantPoints.get(i);
			final float searchValue = searchValue(node, move);
			if (searchValue > best) {
				if (runnable.isFeasible(move) && runnableBoard.isLegal(move)) {
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

	@Override
	public void clear() {
		// Nothing to do; the TreeUpdater clears the table
	}

	/** Some nodes may have their priors updated. */
	@Override
	public void descend(McRunnable runnable) {
		SearchNode node = getRoot();
		assert node != null : "Fancy hash code: " + board.getFancyHash();
		while (runnable.getBoard().getPasses() < 2) {
			selectAndPlayMove(node, runnable);
			final SearchNode child = table.findIfPresent(runnable.getBoard()
					.getFancyHash());
			if (child == null) {
				return; // No child
			}
			if (child.getTotalRuns() > biasDelay && !child.priorsUpdated()) {
				child.updatePriors(runnable);
			}
			node = child;
		}
	}

	/**
	 * A descend method for testing that takes a runnable partway through a
	 * playout.
	 */
	void fakeDescend(McRunnable runnable, short... moves) {
		runnable.copyDataFrom(board);
		final SearchNode node = getRoot();
		assert node != null : "Fancy hash code: " + board.getFancyHash();
		for (final short move : moves) {
			runnable.acceptMove(move);
			final SearchNode child = table.findIfPresent(runnable.getBoard().getFancyHash());
			if (child == null) {
				return; // No child
			}
			if (child.getTotalRuns() > biasDelay && !child.priorsUpdated()) {
				child.updatePriors(runnable);
			}
		}
	}

	@Override
	public int getBiasDelay() {
		return biasDelay;
	}

	Board getBoard() {
		return board;
	}

	/** Returns the root node (creating it if necessary). */
	SearchNode getRoot() {
		return table.findOrAllocate(board.getFancyHash());
	}

	TranspositionTable getTable() {
		return table;
	}

	/** Returns the win rate for this move. */
	@SuppressWarnings("static-method")
	float searchValue(SearchNode node, short move) {
		return node.getWinRate(move);
	}

	/** Selects and plays one move in the search tree. */
	short selectAndPlayMove(SearchNode node, McRunnable runnable) {
		final short move = bestSearchMove(node, runnable);
		runnable.acceptMove(move);
		return move;
	}

	@Override
	public String toString() {
		return getRoot().deepToString(board, table, 0);
	}

}
