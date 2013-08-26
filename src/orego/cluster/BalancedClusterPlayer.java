package orego.cluster;

import static orego.core.Coordinates.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import orego.util.IntSet;


public class BalancedClusterPlayer extends ClusterPlayer {

	private Map<Integer, IntSet> searchersToPoints;
	
	private static double _95_Z_VALUE = 1.96;
	
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
	
	@Override
	public void tallyResults(TreeSearcher searcher, long[] runs, long[] wins) {		
		// Only tally the results from the points that this searcher was 
		// supposed to consider
		try {
			int id = searcher.getSearcherId();
			IntSet consideredPoints = searchersToPoints.get(id);
			
			for(int idx = 0; idx < consideredPoints.size(); idx++) {
				int p = consideredPoints.get(idx);
				totalRuns[p] += runs[p];
				totalWins[p] += wins[p];
			}
		} catch (RemoteException e) {
			getLogWriter().println("Could not get ID from searcher: " + searcher + " to record results.");
		}
	}
	
	private int[] bestPointsPerSearcher() {
		int[] bestPoints = new int[searchersToPoints.size()];
		for(Map.Entry<Integer, IntSet> entry : searchersToPoints.entrySet()) {
			long maxVisits = 0;
			int bestPoint = PASS;
			IntSet points = entry.getValue();
			for(int idx = 0; idx < points.size(); idx++) {
				int p = points.get(idx);
				long visits = totalRuns[p];
				if(visits > maxVisits) {
					maxVisits = visits;
					bestPoint = p;
				}
			}
			bestPoints[entry.getKey()] = bestPoint;
		}
		return bestPoints;
	}
	
	@Override
	protected int bestSearchMove() {
		double maxLowerBound = Double.NEGATIVE_INFINITY;
		int bestPoint = PASS;
		for(int point : bestPointsPerSearcher()) {
			long wins = totalWins[point];
			if(wins < 0) continue;
			double lowerConfidenceBound = lowerConfidenceBound(wins, totalRuns[point]);
			if(lowerConfidenceBound > maxLowerBound) {
				maxLowerBound = lowerConfidenceBound;
				bestPoint = point;
			}
			getLogWriter().println(String.format("%s: %d/%d => %f", pointToString(point), totalWins[point], totalRuns[point], lowerConfidenceBound));
		}
		if(bestPoint != PASS) {
			return bestPoint;
		}
		else {
			return super.bestSearchMove();
		}
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
			searchersToPoints.put(id, new IntSet(getFirstPointBeyondBoard()));
			searcherIds.add(id);
		}
		
		// Add points to be considered by each searcher by counting off
		int count = remoteSearchers.size();
		for(int p = 0; p < getFirstPointBeyondBoard(); p++) {
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
	
	private static double lowerConfidenceBound(long x, long n) {
		double zsq = Math.pow(_95_Z_VALUE, 2);
		double nhat = n + zsq;
		double phat = 1/nhat * (x + 0.5 * zsq);
		
		return phat - _95_Z_VALUE * Math.sqrt(1/nhat * phat * (1 - phat)); 
	}
	
}
