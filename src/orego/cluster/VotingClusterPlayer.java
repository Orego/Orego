package orego.cluster;

import static orego.core.Coordinates.PASS;

import orego.util.IntSet;
import orego.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A ClusterPlayer in which points to play are chosen via a voting
 * procedure. Each searcher votes for for the point it found with the largest
 * number of wins. Ties are broken by number of wins.
 */
public class VotingClusterPlayer extends ClusterPlayer {
	
	/** For each best candidate point store (number of votes, largest number of wins) */
	private Map<Integer, Pair<Integer, Long>> pointsToBallots;
	
	public VotingClusterPlayer() {
		this.pointsToBallots = new HashMap<Integer, Pair<Integer, Long>>();
	}
	
	@Override
	public void tallyResults(TreeSearcher searcher, long[] runs, long[] wins) {
		super.tallyResults(searcher, runs, wins);
		
		// Find the best point from this searcher
		int bestPoint = PASS;
		long maxWins = wins[PASS];
		IntSet vacantPoints = getBoard().getVacantPoints();
		for(int idx = 0; idx < vacantPoints.size(); idx++) {
			int p = vacantPoints.get(idx);
			long winsCount = wins[p];
			if(winsCount > maxWins) {
				bestPoint = p;
				maxWins = winsCount;
			}
		}
		
		// Vote for the best point (and update its win count if necessary)
		Pair<Integer, Long> ballot = pointsToBallots.get(bestPoint);
		if(ballot == null) {
			ballot = Pair.fromValues(1, maxWins); 
		}
		else {
			ballot = Pair.fromValues(ballot.fst + 1, Math.max(ballot.snd, maxWins));
		}
		pointsToBallots.put(bestPoint, ballot);
	}
	
	@Override
	protected int bestSearchMove() {
		getLogWriter().println("Ballots:" + pointsToBallots.toString());
		
		// Find the best point, first sorting by number of votes, then breaking ties with win counts
		int bestPoint = PASS;
		Pair<Integer, Long> bestBallot = Pair.fromValues(0, 0L);
		for(Entry<Integer, Pair<Integer, Long>> voteEntry : pointsToBallots.entrySet()) {
			Pair<Integer, Long> ballot = voteEntry.getValue();
			if(ballot.fst > bestBallot.fst || (ballot.fst == bestBallot.fst && ballot.snd > bestBallot.snd)) {
				bestPoint = voteEntry.getKey();
				bestBallot = ballot;
			}
		}
		
		// We're done with the ballots for this turn
		pointsToBallots.clear();
		
		// If our choice was legal and not a pass, play it, otherwise get the super's opinion
		if(bestPoint != PASS && getBoard().isFeasible(bestPoint) && getBoard().isLegal(bestPoint)) {
			return bestPoint;
		}
		
		getLogWriter().println("Falling back to standard implementation of bestSearchMove.");
		return super.bestSearchMove();
	}
}
