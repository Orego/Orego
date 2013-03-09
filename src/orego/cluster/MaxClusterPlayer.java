package orego.cluster;

import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.ALL_POINTS_ON_BOARD;
import static orego.core.Coordinates.pointToString;

import orego.util.Pair;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/** 
 * A ClusterPlayer which chooses the point with the 
 * largest number of wins out of all the searchers. 
 */
public class MaxClusterPlayer extends ClusterPlayer {
	
	/** For each searcher store (best point, number of wins) */
	private Map<Integer, Pair<Integer, Long>> searchersToResults;
	
	public MaxClusterPlayer() {
		super();
				
		searchersToResults = new HashMap<Integer, Pair<Integer, Long>>();
	}

	@Override
	public void addSearcher(TreeSearcher searcher) throws RemoteException {
		super.addSearcher(searcher);
		
		// Add a new empty entry to the results map
		searchersToResults.put(searcher.getSearcherId(), Pair.fromValues(0, 0L));
	}
	
	@Override
	public void removeSearcher(TreeSearcher searcher) throws RemoteException {
		super.removeSearcher(searcher);
		
		// Remove the entry in the results map associated with this searcher
		searchersToResults.remove(searcher.getSearcherId());
	}
	
	@Override
	public void tallyResults(TreeSearcher searcher, long[] runs, long[] wins) {
		// Also tally results the standard way in case we need to fall back
		super.tallyResults(searcher, runs, wins);
		
		try {
			int id = searcher.getSearcherId();
			
			Pair<Integer, Long> resultsData = searchersToResults.get(id);
			
			// Find the point on the board with the most wins for this searcher
			long maxWins = wins[PASS];
			int bestPoint = PASS;
			for(int p : ALL_POINTS_ON_BOARD) {
				long winCount = wins[p];
				if(winCount > maxWins) {
					bestPoint = p;
					maxWins = winCount;
				}
			}
			
			// Store it in the results map
			resultsData = Pair.fromValues(bestPoint, maxWins);
			searchersToResults.put(id, resultsData);
			
		} catch (RemoteException e) {
			getLogWriter().println("Could not get ID from searcher: " + searcher + " to record results.");
		}
	}
	
	@Override
	protected int bestSearchMove() {
		long maxWins = 0;
		int bestPoint = PASS;
		
		// Find the point with the most wins out of all the searchers
		for(Map.Entry<Integer, Pair<Integer, Long>> resultEntry : searchersToResults.entrySet()) {
			
			Pair<Integer, Long> resultData = resultEntry.getValue();
			long searcherMaxWins = resultData.snd;
			
			getLogWriter().println(pointToString(resultData.fst) + " : " + searcherMaxWins);
			
			if(searcherMaxWins > maxWins) {
				bestPoint = resultData.fst;
				maxWins = searcherMaxWins;
			}
			
			// Clear the search results for this searcher
			searchersToResults.put(resultEntry.getKey(), Pair.fromValues(0, 0L));
		}
		
		if(bestPoint != PASS && getBoard().isFeasible(bestPoint) && getBoard().isLegal(bestPoint)) {
			return bestPoint;
		}
		
		getLogWriter().println("Falling back to super impl of bestSearchMove.");
		return super.bestSearchMove();
	}

}
