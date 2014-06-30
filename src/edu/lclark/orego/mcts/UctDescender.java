package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.move.Mover.PRIMES;
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Math.log;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.Suggester;
import edu.lclark.orego.util.ShortSet;

/** Uses UCT. */
public final class UctDescender extends BestRateDescender {

	/**
	 * Returns the UCT upper bound for node. This is the UCB1-TUNED policy,
	 * explained in the tech report by Gelly, et al, "Modification of UCT with
	 * Patterns in Monte-Carlo Go". The formula is at the bottom of p. 5 in that
	 * paper.
	 */
	@Override
	float searchValue(SearchNode node, short move) {
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
		return (float)(uncertainty + barX);
	}

	/** Adds some extra wins from node, based on the suggesters in the runnable. */
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

	/** Priors are not updated unless there have been this many runs through a node. */
	private final int biasDelay;

	public UctDescender(Board board, TranspositionTable table, int biasDelay) {
		super(board, table);
		this.biasDelay = biasDelay;
	}

	@Override
	public void descend(McRunnable runnable) {
		SearchNode node = getRoot();
		assert node != null : "Fancy hash code: " + getBoard().getFancyHash();
		while (runnable.getBoard().getPasses() < 2) {
			selectAndPlayMove(node, runnable);
			SearchNode child = getTable().findIfPresent(runnable.getBoard()
					.getFancyHash());
			if (child == null) {
				return; // No child
			}
			// TODO This is the only difference from the superclass method
			if (child.getTotalRuns() > biasDelay && !child.priorsUpdated()) {
				updatePriors(child, runnable);
			}
			node = child;
		}
	}

	// TODO This might also be moved up to BestRateDescender
	/**
	 * A descend method for testing that takes a runnable partway through a
	 * playout.
	 */
	void fakeDescend(McRunnable runnable, short... moves) {
		runnable.copyDataFrom(getBoard());
		SearchNode node = getRoot();
		assert node != null : "Fancy hash code: " + getBoard().getFancyHash();
		for (short move : moves) {
			runnable.acceptMove(move);
			SearchNode child = getTable().findIfPresent(runnable.getBoard().getFancyHash());
			if (child == null) {
				return; // No child
			}
			if (child.getTotalRuns() > biasDelay && !child.priorsUpdated()) {
				updatePriors(child, runnable);
			}
		}
	}

	@Override
	public int getBiasDelay() {
		return biasDelay;
	}


}
