package orego.cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SearchController extends Remote {
	/** Accepts search results from a given Searcher. */
	void acceptResults(TreeSearcher searcher, int[] runs, int[] wins) throws RemoteException; 
}
