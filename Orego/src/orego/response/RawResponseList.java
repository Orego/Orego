package orego.response;

/**
 * This object stores information about this move, and potentially stores
 * information about moves made with a relevant history.
 */

import ec.util.MersenneTwisterFast;

import orego.core.Board;
import orego.core.Coordinates;
import orego.util.IntSet;

public class RawResponseList extends AbstractResponseList {

	final static int NORMAL_WINS_BIAS = 1;
	final static int NORMAL_RUNS_BIAS = 2;
	final static int PASS_WINS_BIAS = 1;
	final static int PASS_RUNS_BIAS = 10;

	int[] wins;
	int[] runs;
	long totalRuns;

	public RawResponseList() {
		wins = new int[Coordinates.FIRST_POINT_BEYOND_BOARD];
		runs = new int[Coordinates.FIRST_POINT_BEYOND_BOARD];
		for (int p: Coordinates.ALL_POINTS_ON_BOARD) {
			wins[p] = NORMAL_WINS_BIAS;
			runs[p] = NORMAL_RUNS_BIAS;
		}
		wins[Coordinates.PASS] = PASS_WINS_BIAS;
		runs[Coordinates.PASS] = PASS_RUNS_BIAS;
	}

	// TODO: these array getters are only used in tests
	// could probably remove and just use move-specific getters
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

	public long getTotalRuns() {
		return totalRuns;
	}

	public void setTotalRuns(int totalRuns) {
		this.totalRuns = totalRuns;
	}

	/**
	 * Add a win and run to this move.
	 */
	public void addWin(int p) {
		wins[p]++;
		runs[p]++;
		totalRuns++;
		assert totalRuns > 0 : "totalRuns overflowed";
	}

	/**
	 * Add a run to this move.
	 */
	public void addLoss(int p) {
		runs[p]++;
		totalRuns++;
		assert totalRuns > 0 : "totalRuns overflowed";
	}

	public int getWins(int p) {
		return wins[p];
	}

	public int getRuns(int p) {
		return runs[p];
	}

	public double getWinRate(int p) {
		return wins[p] / (1.0 * runs[p]);
	}

	public int bestMove(Board board) {
		MersenneTwisterFast random = new MersenneTwisterFast();
		IntSet vacantPoints = board.getVacantPoints();
		int start = random.nextInt(vacantPoints.size());
		int i = start;
		double compare = 0.1;
		int bestMove = Coordinates.PASS;
		double searchValue;
		int move;
		do {
			move = vacantPoints.get(i);
			searchValue = getWinRate(move);
			if (searchValue > compare) {
				if (board.isFeasible(move) && board.isLegal(move)) {
					compare = searchValue;
					bestMove = move;
				}
			}
			i = (i + 457) % vacantPoints.size();
		} while (i != start);
		return bestMove;
	}
}
