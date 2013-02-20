package orego.cluster;

import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import orego.util.IntSet;


public class BalancedClusterPlayer extends ClusterPlayer {

	private Map<Integer, IntSet> searchersToPoints;
	
	public BalancedClusterPlayer() {
		super();
		
		searchersToPoints = new HashMap<Integer, IntSet>();
	}
	
	@Override
	public void addSearcher(TreeSearcher searcher) throws RemoteException {
		super.addSearcher(searcher);
		
		redistributePointAllocation();
	}
	
	@Override
	public void removeSearcher(TreeSearcher searcher) throws RemoteException {
		super.removeSearcher(searcher);
		
		redistributePointAllocation();
	}
	
	// TODO: This is repetitive, we should break this method into two parts
	@Override
	public void acceptResults(TreeSearcher searcher, long[] runs, long[] wins) throws RemoteException {
		if(resultsRemaining < 0) {
			return;
		}
		
		// Only tally the results from the points that this searcher was 
		// supposed to consider		
		int id = searcher.getSearcherId();
		IntSet consideredPoints = searchersToPoints.get(id);
		
		for(int idx = 0; idx < consideredPoints.size(); idx++) {
			int p = consideredPoints.get(idx);
			totalRuns[p] += runs[p];
			totalWins[p] += wins[p];
		}
		
		decrementResultsRemaining();
	}
	
	private void redistributePointAllocation() throws RemoteException {
		searchersToPoints.clear();
		
		// If all the searchers have left, there's nothing for us to do
		if(getRemoteSearchers().size() == 0) {
			return;
		}
		
		// Make a set of points for each searcher, and remember their ids
		List<TreeSearcher> remoteSearchers = getRemoteSearchers();
		List<Integer> searcherIds = new ArrayList<Integer>();
		for(TreeSearcher searcher : remoteSearchers) {
			int id = searcher.getSearcherId();
			searchersToPoints.put(id, new IntSet(FIRST_POINT_BEYOND_BOARD));
			searcherIds.add(id);
		}
		
		// Add points to be considered by each searcher by counting off
		int count = remoteSearchers.size();
		for(int p = 0; p < FIRST_POINT_BEYOND_BOARD; p++) {
			int id = searcherIds.get(p % count);
			searchersToPoints.get(id).add(p);
		}
		
		// Send the points to be considered to the searchers
		for(int idx = 0; idx < remoteSearchers.size(); idx++) {
			int id = searcherIds.get(idx);
			IntSet points = searchersToPoints.get(id);
			remoteSearchers.get(idx).setPointsToConsider(points);
		}
	}
	
}
