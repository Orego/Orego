package orego.response;

/**
 * This object stores information about this move, and potentially stores
 * information about moves made with a relevant history.
 */

import orego.core.Coordinates;

public class HistoryInfo {
	
	int move;
	int wins;
	int runs;
	HistoryInfo[] history;
	
	public HistoryInfo(int newmove){
		move = newmove;
		//Initialize to size of the board, plus PASS, plus NO_POINT
		history = new HistoryInfo[Coordinates.FIRST_POINT_BEYOND_BOARD];
	}
	
	/**
	 * Add a win and run to this move.
	 */
	public void addWin(){
		wins++;
		runs++;
	}
	
	/**
	 * Add a run to this move.
	 */
	public void addLoss(){
		runs++;
	}
	
	public int getMove(){
		return move;
	}
	
	public int getWins(){
		return wins;
	}
	
	public int getRuns(){
		return runs;
	}
	
	/**
	 * Returns the HistoryInfo object which contains the wins and runs for a move
	 * relative to previously made moves.
	 * @param move The move you want the history of.
	 * @return The HistoryInfo which has wins and losses relative to previous moves.
	 */
	public HistoryInfo getHistoryInfo(int move) {
		return history[move];
	}
	
	/**
	 * This is called to setup a level of history. It uses the moves on the board,
	 * Pass, and NO_POINT.
	 */
	public void setupHistory(){
		for (int p : Coordinates.ALL_POINTS_ON_BOARD) {
			history[p] = new HistoryInfo(p);
		}
		history[Coordinates.PASS] = new HistoryInfo(Coordinates.PASS);
		history[Coordinates.NO_POINT] = new HistoryInfo(Coordinates.NO_POINT);
	}

}
