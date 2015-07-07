package edu.lclark.orego.genetic;

import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class Genotype {
	
	private long[] bits;
	
	public Genotype(long[] bits){
		this.bits = bits;
	}
	
	/**
	 * @param length The number of 64-bit longs in this genotype.
	 */
	public Genotype(int length){
		bits = new long[length];
	}
	
	public void setBits(long[] bits){
		this.bits = bits;
	}
	
	public void mutate(MersenneTwisterFast random){
		int r = random.nextInt(64 * bits.length + 1);
		int word = r / 64;
		bits[word] ^= (1L << r); 
	}
	
	public void randomize(){
		MersenneTwisterFast random = new MersenneTwisterFast();
		for (int i = 0; i < bits.length; i++) {
			bits[i] = random.nextLong();
		}
	}
	
	public Genotype cross(Genotype that, MersenneTwisterFast random){
		int k = random.nextInt(64*bits.length + 1);
		int word = k / 64;
		int i = k % 64;
		long[] result = new long[bits.length];
		for (int j = 0; j < result.length; j++) {
			if(j < word){
				result[j] = bits[j];
			}else if (j == word){
				if(i == 0){
					result[j] = that.bits[j];
				}else{
					result[j] = (bits[j] & (((1L << i) - 1L) << (64 - i))) | (that.bits[j] & ((1L << (64 - i)) - 1L));
				}
				
			}else{
				result[j] = that.bits[j];
			}
		}
		return new Genotype(result);
	}
	
	public long[] getBits(){
		return bits;
	}
	
	public static void main(String[] args) {
		System.out.println(Long.toBinaryString(-1L));
	}
}
