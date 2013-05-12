package orego.cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;

import orego.util.IntSet;

public interface TreeSearcher extends Remote {

	/** Sets an ID that is used internally by the server. */
	void setSearcherId(int id) throws RemoteException;
	
	/** Retrieves the ID that was set with setSearcherId, return -1 if no ID was set */
	int getSearcherId() throws RemoteException;
	
	/** Resets all the state contained in this searcher. */
	void reset() throws RemoteException;
	
	/** Sets the komi for the search. */
	void setKomi(double komi) throws RemoteException;
	
	/** Sets the points that this searcher will consider playing at. */
	void setPointsToConsider(IntSet pts) throws RemoteException;
	
	/** Sets the specified property on the represented player. */
	void setProperty(String key, String value) throws RemoteException;
	
	/** Accepts the given move into the state of the searcher. */
	void acceptMove(int player, int location) throws RemoteException;
	
	/** Undoes the last move. */
	boolean undo() throws RemoteException;
	
	/** Restarts this searcher and idles for restart */
	void kill() throws RemoteException;
	
	/** Sets the player to the Tree Searcher should use.
	 * @param player the fully qualified player class name.
	 * @return true if the player exists, false otherwise. 
	 */
	boolean setPlayer(String player) throws RemoteException;
	
	/** 
	 * Begins searching for the amount of time/playouts specified by properties set. 
	 * Results will be reported to the registered controller at the end of the search.
	 * */
	void beginSearch() throws RemoteException;
	
	/**
	 * Forces the searcher to stop searching, must be called before any more interaction
	 * if the search did not end normally (with a callback)
	 */
	void terminateSearch() throws RemoteException;
	
	/**
	 * Gets the total number of playouts completed by this searcher during the current game.
	 */
	long getTotalPlayoutCount() throws RemoteException;
	
	/**
	 * Gets the searcher's recommendation of whether Orego should pass.
	 */
	boolean shouldPassToWin() throws RemoteException;
	
	/**
	 * Gets the searchers view of the final status list for the game.
	 */
	String finalStatusList(String status) throws RemoteException;
}
