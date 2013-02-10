package orego.cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TreeSearcher extends Remote {
	public enum SearchType {PLAYOUT_COUNT, TIME}
	
	public static final String SEARCHER_NAME = "TreeSearcher";
	
	/** Set the Controller that this Searcher will report results to. */
	void setController(SearchController c) throws RemoteException;
	
	/** Resets all the state contained in this searcher. */
	void reset() throws RemoteException;
	
	/** Sets the komi for the search. */
	void setKomi(double komi) throws RemoteException;
	
	/** Sets the specified property on the represented player. */
	void setProperty(String key, String value) throws RemoteException;
	
	/** Accepts the given move into the state of the searcher. */
	void acceptMove(int player, int location) throws RemoteException;
	
	/** 
	 * Begins searching for the amount of time/playouts specified by properties set. 
	 * Results will be reported to the registered controller at the end of the search.
	 * */
	void beginSearch() throws RemoteException;
}
