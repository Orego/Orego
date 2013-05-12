package orego.mcts;

import orego.play.Playable;

public interface StatisticalPlayer extends Playable {
	/** Performs playouts in the manner of bestMove, but does not try to determine the move to play */
	public void runSearch();
	
	/** Get the number of wins through the point p */
	public int getWins(int p);
	
	/** Get the number of playouts through the point p */
	public int getPlayouts(int p);
	
	/** Get an array containing the wins through each point on the board */
	public long[] getBoardWins();
	
	/** Get an array containing the playouts through each point on the board */
	public long[] getBoardPlayouts();
	
	/** Get the total number of playouts that were run during this game */
	public long getTotalPlayoutCount();
	
	/** Tests whether we would win after removing our dead stones */
	public boolean secondPassWouldWinGame();
	
	/** Gets the player's opinion on final status of stones on the board */
	public String finalStatusList(String status);
	
	/** 
	 * Terminates any ongoing search activity, it should be safe to begin a new
	 * search after this has been called
	 */
	public void terminateSearch();
}
