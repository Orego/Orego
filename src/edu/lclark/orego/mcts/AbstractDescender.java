package edu.lclark.orego.mcts;

import static edu.lclark.orego.experiment.Logging.*;
import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.CoordinateSystem.RESIGN;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortList;
import edu.lclark.orego.util.ShortSet;

/** Always chooses the move with the best win rate, with no exploration. */
public abstract class AbstractDescender implements TreeDescender {

	/** If our win rate falls below this, resign. */
	public static final float RESIGN_PARAMETER = 0.1f;

	/**
	 * Bias is not updated unless there have been this many runs through a node.
	 */
	private final int biasDelay;

	private final Board board;

	private final TranspositionTable table;

	public AbstractDescender(Board board, TranspositionTable table,
			int biasDelay) {
		this.board = board;
		this.table = table;
		this.biasDelay = biasDelay;
	}

	@Override
	public short bestPlayMove() {
		double mostWins = 1;
		short result = PASS;
		final ShortSet vacantPoints = board.getVacantPoints();
		final SearchNode root = getRoot();
		do {
			mostWins = root.getWins(PASS);
			// If the move chosen on the previous pass through this loop was
			// illegal (e.g., because it was never actually tried in a playout),
			// throw it out
			if (result != PASS) {
				log("Rejected " + board.getCoordinateSystem().toString(result) + " as illegal");
				root.exclude(result);
				result = PASS;
			}
			for (int i = 0; i < vacantPoints.size(); i++) {
				final short move = vacantPoints.get(i);
				if (root.getWins(move) > mostWins) {
					mostWins = root.getWins(move);
					result = move;
				}
			}
		} while (result != PASS && !board.isLegal(result));
		// Consider resigning
		if (root.getWinRate(result) < RESIGN_PARAMETER) {
			return RESIGN;
		}
		log("Selected " + board.getCoordinateSystem().toString(result) + " with " + root.getWins(result) + " wins in " + root.getRuns(result) + " runs");
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
		float bestSearchValue = searchValue(node, PASS);
		result = PASS;
		final ShortList candidates = runnable.getCandidates();
		candidates.clear();
		candidates.addAll(runnableBoard.getVacantPoints());
		while (candidates.size() > 0) {
			final short p = candidates.removeRandom(random);
			final float searchValue = searchValue(node, p);
			if (searchValue > bestSearchValue) {
				if (runnable.isFeasible(p) && runnableBoard.isLegal(p)) {
					bestSearchValue = searchValue;
					result = p;
				} else {
					node.exclude(p);
				}
			}
		} 
		
		
//		
//		
//		final ShortSet vacantPoints = runnableBoard.getVacantPoints();
//		
//		
//		int start;
//		start = random.nextInt(vacantPoints.size());
//		int i = start;
//		final int skip = PRIMES[random.nextInt(PRIMES.length)];
//		do {
//			final short move = vacantPoints.get(i);
//			final float searchValue = searchValue(node, move);
//			if (searchValue > bestSearchValue) {
//				if (runnable.isFeasible(move) && runnableBoard.isLegal(move)) {
//					bestSearchValue = searchValue;
//					result = move;
//				} else {
//					node.exclude(move);
//				}
//			}
//			// Advancing by a random prime skips through the array
//			// in a manner analogous to double hashing.
//			i = (i + skip) % vacantPoints.size();
//		} while (i != start);
		return result;
	}

	@Override
	public void clear() {
		// Nothing to do; the TreeUpdater clears the table
	}

	/** Some nodes may have their biases updated. */
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
			if (child.getTotalRuns() > biasDelay && !child.biasUpdated()) {
				child.updateBias(runnable);
			}
			node = child;
		}
	}

	@Override
	public void fakeDescend(McRunnable runnable, short... moves) {
		runnable.copyDataFrom(board);
		final SearchNode node = getRoot();
		assert node != null : "Fancy hash code: " + board.getFancyHash();
		for (final short move : moves) {
			runnable.acceptMove(move);
			final SearchNode child = table.findIfPresent(runnable.getBoard()
					.getFancyHash());
			if (child == null) {
				return; // No child
			}
			if (child.getTotalRuns() > biasDelay && !child.biasUpdated()) {
				child.updateBias(runnable);
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
