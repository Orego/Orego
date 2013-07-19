package orego.shape;

import java.io.Serializable;

/** A sloppy multihash table. */
public class Table implements Serializable {

	private static final long serialVersionUID = 1L;

	/** For each table and entry, the number of stored entries. */
	private long[][] counts;
	
	/** Used to keep only the relevant number of bit in an index. */
	private int mask;
	
	/** Width, in bits, of each table's indices. */
	private int width;
	
	/** For each table and entry, the proportion of stored entries that were wins. */
	private float[][] winRates;
	
	/**
	 * @param tables number of tables.
	 * @param bits number of index bits in each table.
	 */
	public Table(int tables, int bits) {
		winRates = new float[tables][1 << bits];
		for (int i = 0; i < tables; i++) {
			java.util.Arrays.fill(winRates[i], 0.5f);
		}
		counts = new long[tables][1 << bits];
		width = bits;
		mask = (1 << bits) - 1;
	}

	/** Returns the index into the specified table. */
	protected int getLocalIndex(long hash, int table) {
		return ((int) (hash >>> (width * table))) & mask;
	}
	
	/**
	 * Return the win rate for all entries stored at hash.
	 */
	public float getWinRate(long hash) {
		float sum = 0.0f;
		for (int i = 0; i < winRates.length; i++) {
			sum += winRates[i][getLocalIndex(hash, i)];
		}
		return sum / winRates.length;
	}
	
	/**
	 * Return the lowest count for all entries stored at hash.
	 */
	public long getLowCount(long hash) {
		long min = counts[0][getLocalIndex(hash, 0)];
		for (int i = 1; i < counts.length; i++) {
			if (min > counts[i][getLocalIndex(hash, i)])
				min = counts[i][getLocalIndex(hash, i)];
		}
		return min;
	}

	/**
	 * Stores a win (if win == 1) or a loss (0) at hash.
	 */
	public void store(long hash, int win) {
		for (int i = 0; i < winRates.length; i++) {
			int index = getLocalIndex(hash, i);
			long runs = counts[i][index];
			winRates[i][index] = ((winRates[i][index] * runs) + win) / (runs + 1);
			counts[i][index]++;
		}		
	}

}
