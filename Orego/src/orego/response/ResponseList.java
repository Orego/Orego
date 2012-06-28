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
	
	// All of these arrays are shorts so they'll fit into memory
	short[] wins;
	short[] runs;
	short[] moves;
	short[] indices;
	int totalRuns;
	
	public ResponseList(){
		wins = new short[Coordinates.FIRST_POINT_BEYOND_BOARD];
		runs = new short[Coordinates.FIRST_POINT_BEYOND_BOARD];
		moves = new short[Coordinates.FIRST_POINT_BEYOND_BOARD];
		indices = new short[Coordinates.FIRST_POINT_BEYOND_BOARD];
		//randomize the list
		ArrayList<Short> list = new ArrayList<Short>();
		for (int p : Coordinates.ALL_POINTS_ON_BOARD) {
			list.add((short) p);
		}
		Random random = new Random();
		short i = 0;
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
		indices[Coordinates.PASS] = (short) (moves.length - 1);	
	}
	
	public short[] getWins() {
		return wins;
	}

	public void setWins(short[] wins) {
		this.wins = wins;
	}

	public short[] getRuns() {
		return runs;
	}

	public void setRuns(short[] runs) {
		this.runs = runs;
	}

	public short[] getMoves() {
		return moves;
	}

	public void setMoves(short[] moves) {
		this.moves = moves;
	}

	public short[] getIndices() {
		return indices;
	}

	public void setIndices(short[] indices) {
		this.indices = indices;
	}

	public int getTotalRuns() {
		return totalRuns;
	}

	public void setTotalRuns(int totalRuns) {
		this.totalRuns = totalRuns;
	}
	
	/**
	 * @param move the move to be updated
	 * @param result 1 if adding a win, -1 if adding a loss
	 * 
	 * calls either sortWin(move) or sortLoss(move)
	 * to sort the arrays appropriately
	 * 
	 */
	public void sort(int move, int result) {
		int moveIndex = indices[move];
		if(moveIndex <= 0 && result > 0) return;
		if(moveIndex >= Coordinates.BOARD_AREA && result < 0) return;
		
		if(result > 0) sortWin(move);
		else sortLoss(move);
	}
	
	/**
	 * @param move the move to be updated
	 * 
	 * sorts the arrays appropriately if a
	 * win has been added to move
	 */
	public void sortWin(int move) {
		int moveIndex = indices[move];
		double toSort = getWinRate(move);
		int compIndex = moveIndex-1;
		double compare = getWinRate(moves[compIndex]);
		
		while(toSort >= compare && compIndex > 0) {
			swap(moveIndex, compIndex);
			compIndex--;
			moveIndex--;
			toSort = getWinRate(moves[moveIndex]);
			compare = getWinRate(moves[compIndex]);
		}
		if(compIndex == 0 && toSort >= compare) swap(moveIndex,compIndex);
	}
	
	/**
	 * @param move the move to be updated
	 * 
	 * sorts the arrays appropriately if a
	 * loss has been added to move
	 */
	public void sortLoss(int move) {
		int moveIndex = indices[move];
		double toSort = getWinRate(move);
		int compIndex = moveIndex+1;
		double compare = getWinRate(moves[compIndex]);
		
		while(toSort <= compare && compIndex < Coordinates.BOARD_AREA+1) {
			swap(moveIndex, compIndex);
			compIndex++;
			moveIndex++;
			toSort = getWinRate(moves[moveIndex]);
			compare = getWinRate(moves[compIndex]);
		}
		if(compIndex == Coordinates.BOARD_AREA && toSort <= compare) swap(moveIndex,compIndex);
	}
	
	public void swap(int i1, int i2) {
		// swap wins[]
		short hold1 = wins[i1];
		short hold2 = wins[i2];
		wins[i1] = hold2;
		wins[i2] = hold1;
		// swap moves[]
		hold1 = moves[i1];
		hold2 = moves[i2];
		moves[i1] = hold2;
		moves[i2] = hold1;
		// swap runs[]
		hold1 = runs[i1];
		hold2 = runs[i2];
		runs[i1] = hold2;
		runs[i2] = hold1;
		// swap indices[]
		hold1 = indices[moves[i1]];
		hold2 = indices[moves[i2]];
		indices[moves[i1]] = hold2;
		indices[moves[i2]] = hold1;
	}
	/**
	 * Add a win and run to this move.
	 */
	public void addWin(int p){
		wins[indices[p]]++;
		runs[indices[p]]++;
		totalRuns++;
		assert totalRuns > 0: "totalRuns two's complement";
		sort(p, 1);
	}
	
	/**
	 * Add a run to this move.
	 */
	public void addLoss(int p){
		runs[indices[p]]++;
		totalRuns++;
		assert totalRuns > 0: "totalRuns two's complement";
		sort(p,-1);
	}
	
	public short getWins(int p){
		return wins[p];
	}
	
	public short getRuns(int p){
		return runs[p];
	}
	
	public double getWinRate(int p){
		return wins[indices[p]] / (1.0 * runs[indices[p]]);
	}

}
