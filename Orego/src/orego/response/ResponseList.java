package orego.response;

/**
 * This object stores information about this move, and potentially stores
 * information about moves made with a relevant history.
 */

import java.util.ArrayList;
import java.util.Random;

import orego.core.Coordinates;

public class ResponseList {
	
	final static int NORMAL_WINS_BIAS = 1;
	final static int NORMAL_RUNS_BIAS = 2;
	final static int PASS_WINS_BIAS = 1;
	final static int PASS_RUNS_BIAS = 10;
	
	int[] wins;
	int[] runs;
	int[] moves;
	int[] indices;
	int totalRuns;
	
	public ResponseList(){
		wins = new int[Coordinates.FIRST_POINT_BEYOND_BOARD];
		runs = new int[Coordinates.FIRST_POINT_BEYOND_BOARD];
		moves = new int[Coordinates.FIRST_POINT_BEYOND_BOARD];
		indices = new int[Coordinates.FIRST_POINT_BEYOND_BOARD];
		//randomize the list
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int p : Coordinates.ALL_POINTS_ON_BOARD) {
			list.add(p);
		}
		Random random = new Random();
		int i = 0;
		while (list.size() > 0){
			moves[i] = list.remove(random.nextInt(list.size()));
			indices[moves[i]] = i;
			wins[i] = NORMAL_WINS_BIAS;
			runs[i] = NORMAL_RUNS_BIAS;
			i++;
		}
		moves[moves.length - 1] = Coordinates.PASS;
		wins[wins.length - 1] = PASS_WINS_BIAS;
		runs[runs.length - 1] = PASS_RUNS_BIAS;
		indices[Coordinates.PASS] = moves.length - 1;	
	}
	
	public int[] getWins() {
		return wins;
	}

	public void setWins(int[] wins) {
		this.wins = wins;
	}

	public int[] getRuns() {
		return runs;
	}

	public void setRuns(int[] runs) {
		this.runs = runs;
	}

	public int[] getMoves() {
		return moves;
	}

	public void setMoves(int[] moves) {
		this.moves = moves;
	}

	public int[] getIndices() {
		return indices;
	}

	public void setIndices(int[] indices) {
		this.indices = indices;
	}

	public int getTotalRuns() {
		return totalRuns;
	}

	public void setTotalRuns(int totalRuns) {
		this.totalRuns = totalRuns;
	}
	
	/**
	 * Add a win and run to this move.
	 */
	public void addWin(int p){
		wins[indices[p]]++;
		runs[indices[p]]++;
		totalRuns++;
		/*Sort nodes to the left...
		 * 
		 */
		
		
	}
	
	/**
	 * Add a run to this move.
	 */
	public void addLoss(int p){
		runs[indices[p]]++;
		totalRuns++;
		/*Sort nodes to the right...
		 * 
		 */
		
		
	}
	
	public int getWins(int p){
		return wins[p];
	}
	
	public int getRuns(int p){
		return runs[p];
	}
	
	public double getWinRate(int p){
		return wins[indices[p]] / (1.0 * runs[indices[p]]);
	}

}
