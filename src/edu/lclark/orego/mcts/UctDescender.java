package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.move.Mover.PRIMES;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Math.log;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.Suggester;
import edu.lclark.orego.util.ShortSet;

// TODO This is almost identical to BestRateDescender; should we consolidate?
/** Uses UCT. */
public final class UctDescender implements TreeDescender {

	private final int biasDelay;

	private final Board board;

	private final TranspositionTable table;

	@Override
	public int getBiasDelay() {
		return biasDelay;
	}

	public UctDescender(Board board, TranspositionTable table, int biasDelay) {
		this.board = board;
		this.table = table;
		this.biasDelay = biasDelay;
	}

	@Override
	public short bestPlayMove() {
		double best = 1;
		short result = PASS;
		ShortSet vacantPoints = board.getVacantPoints();
		SearchNode root = getRoot();
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
				short move = vacantPoints.get(i);
				if (root.getWins(move) > best) {
					best = root.getWins(move);
					result = move;
				}
			}
		} while ((result != PASS) && !board.isLegal(result));
		// TODO Handle resignation
		// // Consider resigning
		// if (node.getWinRate(result) < RESIGN_PARAMETER) {
		// return RESIGN;
		// }
		return result;
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
	private static short bestSearchMove(SearchNode node, McRunnable runnable) {
		Board runnableBoard = runnable.getBoard();
		MersenneTwisterFast random = runnable.getRandom();
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
				// TODO Would reversing the order here (and in bestPlayMove) be
				// faster?
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

	/**
	 * Returns the UCT upper bound for node. This is the UCB1-TUNED policy,
	 * explained in the tech report by Gelly, et al, "Modification of UCT with
	 * Patterns in Monte-Carlo Go". The formula is at the bottom of p. 5 in that
	 * paper.
	 */
	private static double searchValue(SearchNode node, short move) {
		// The variable names here are chosen for consistency with the tech
		// report
		double barX = node.getWinRate(move);
		if (barX < 0) { // if the move has been excluded
			return NEGATIVE_INFINITY;
		}
		double logParentRunCount = log(node.getTotalRuns());
		// In the paper, term1 is the mean of the SQUARES of the rewards; since
		// all rewards are 0 or 1 here, this is equivalent to the mean of the
		// rewards, i.e., the win rate.
		double term1 = barX;
		double term2 = -(barX * barX);
		double term3 = sqrt(2 * logParentRunCount / node.getRuns(move));
		double v = term1 + term2 + term3; // This equation is above Eq. 1
		assert v >= 0 : "Negative variability in UCT";
		double factor1 = logParentRunCount / node.getRuns(move);
		double factor2 = min(0.25, v);
		double uncertainty = 0.4 * sqrt(factor1 * factor2);
		return uncertainty + barX;
	}

	/** Selects and plays one move in the search tree. */
	private static short selectAndPlayMove(SearchNode node, McRunnable runnable) {
		short move = bestSearchMove(node, runnable);
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
					.getFancyHash());
			if (child == null) {
				return; // No child
			}
			if (child.getTotalRuns() > biasDelay && !child.priorsUpdated()) {
				updatePriors(child, runnable);
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
		SearchNode node = getRoot();
		assert node != null : "Fancy hash code: " + board.getFancyHash();
		for (short move : moves) {
			runnable.acceptMove(move);
			SearchNode child = table.findIfPresent(runnable.getBoard().getFancyHash());
			if (child == null) {
				return; // No child
			}
			if (child.getTotalRuns() > biasDelay && !child.priorsUpdated()) {
				updatePriors(child, runnable);
			}
		}
	}

	private static void updatePriors(SearchNode node, McRunnable runnable) {
		Suggester[] suggesters = runnable.getSuggesters();
		int[] weights = runnable.getWeights();
		for (int i = 0; i < suggesters.length; i++) {
			ShortSet moves = suggesters[i].getMoves();
			for (int j = 0; j < moves.size(); j++) {
				short p = moves.get(j);
				node.update(p, weights[i], weights[i]);
			}
		}
		node.setPriorsUpdated(true);

	}

	@Override
	public String toString() {
		return getRoot().deepToString(board, table, 0);
	}

}
