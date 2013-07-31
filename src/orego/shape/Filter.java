package orego.shape;

import java.io.Serializable;

/** A sloppy filter. */
public class Filter implements Serializable {

	private static final long serialVersionUID = 1L;

	/** For each table and entry, the number of stored entries. */
	private long[][] counts;
	
	/** Used to keep only the relevant number of bit in an index. */
	private int mask;
	
	/** Width, in bits, of each table's indices. */
	private int width;
	
	private final int THRESHOLD = 10000;
	
	/**
	 * @param tables number of tables.
	 * @param bits number of index bits in each table.
	 */
	public Filter(int tables, int bits) {
		counts = new long[tables][1 << bits];
		width = bits;
		mask = (1 << bits) - 1;
	}

	/** Returns the index into the specified table. */
	protected int getLocalIndex(long hash, int table) {
		return ((int) (hash >>> (width * table))) & mask;
	}
	
	/** Returns whether the hash has been seen more than THRESHOLD times. */
	public boolean isReasonable(long hash){
		for (int i=0; i<counts.length; i++){
			if (counts[i][getLocalIndex(hash,i)]<THRESHOLD){
				return false;
			}
		}
		return true;
	}
	
	/** Returns the lowest count present in a filter. */
	public long getLowestCount(long hash){
		long min = counts[0][getLocalIndex(hash,0)];
		for (int i = 1; i < counts.length; i++) {
			long temp = counts[i][getLocalIndex(hash, i)];
			if (min>temp){
				min = temp;
			}
		}
		return min;
	}

//	/** Sets the count for all entries in this table. 
//	 * @param count The value to set the run counts to.
//	 */
//	public void setCount(long count) {
//		for (int i = 0; i < counts.length; i++) {
//			Arrays.fill(counts[i], count);
//		}
//	}
	
	/**
	 * Stores a run for this hash.
	 */
	public void store(long hash) {
		long min = getLowestCount(hash)+1;
		for (int i = 0; i < counts.length; i++) {
			int index = getLocalIndex(hash, i);
			counts[i][index] = Math.max(min, counts[i][index]);
		}		
	}

}
