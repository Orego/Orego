package bandit;

/** Stores, at each position, a win rate in [0.0, 1.0] and a number of runs. */
public class Table {

	private double[] winRates;
	
	private long[] runCounts;
	
	public Table() {
		winRates = new double[1 << 16];
		runCounts = new long[1 << 16];
	}

	/**
	 * Store a win or loss at the indicated hash.
	 * @param hash The hash entry to update.
	 * @param win True for a win, false for a loss.
	 */
	public void store(int hash, boolean win) {
		if (win) {
			winRates[hash] = (winRates[hash] * runCounts[hash] + 1) / (runCounts[hash] + 1);
		} else {
			winRates[hash] = (winRates[hash] * runCounts[hash]) / (runCounts[hash] + 1);
		}
		runCounts[hash]++;
	}
	
	/**
	 * Returns the win rate at the specified hash.
	 */
	public double getWinRate(int hash) {
		if (runCounts[hash] == 0) {
			return 0.5;
		}
		return winRates[hash];
	}

	/**
	 * Returns the run count at the specified hash.
	 */
	public long getRunCount(int hash) {
		return runCounts[hash];
	}

}
