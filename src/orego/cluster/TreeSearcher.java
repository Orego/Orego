package orego.cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TreeSearcher extends Remote {
	public enum SearchType {PLAYOUT_COUNT, TIME}
	
	/** Set the Controller that this Searcher will report results to. */
	void setController(SearchController c) throws RemoteException;
	
	/** Resets all the state contained in this searcher. */
	void reset() throws RemoteException;
	
	/** Sets the komi for the search. */
	void setKomi(double komi) throws RemoteException;
	
	/** Accepts the given move into the state of the searcher. */
	void acceptMove(int player, int location) throws RemoteException;
	
	/** 
	 * Begins searching for either the number of playouts or milliseconds requested. 
	 * Results will be reported to the registered controller at the end of the search.
	 * */
	void beginSearch(long duration, SearchType type) throws RemoteException;
}
