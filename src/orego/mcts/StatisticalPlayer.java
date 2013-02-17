package orego.mcts;

import orego.play.Playable;

public interface StatisticalPlayer extends Playable {
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
}
