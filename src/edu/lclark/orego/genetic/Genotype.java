package edu.lclark.orego.genetic;

import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class Genotype {
	
	private long[] words;
	
	public Genotype(long[] words){
		this.words = words;
	}
	
	/**
	 * @param length The number of 64-bit longs in this genotype.
	 */
	public Genotype(int length){
		words = new long[length];
	}
	
	public void setWords(long[] words){
		this.words = words;
	}
	
	public void mutate(MersenneTwisterFast random){
		int r = random.nextInt(64 * words.length);
		int w = r / 64;
		words[w] ^= (1L << r); 
	}
	
	public void randomize(){
		MersenneTwisterFast random = new MersenneTwisterFast();
		for (int i = 0; i < words.length; i++) {
			words[i] = random.nextLong();
		}
	}
	
	public Genotype cross(Genotype that, MersenneTwisterFast random){
		int k = random.nextInt(64*words.length + 1);
		int w = k / 64;
		int i = k % 64;
		long[] result = new long[words.length];
		for (int j = 0; j < result.length; j++) {
			if(j < w){
				result[j] = words[j];
			}else if (j == w){
				if(i == 0){
					result[j] = that.words[j];
				}else{
					result[j] = (words[j] & (((1L << i) - 1L) << (64 - i))) | (that.words[j] & ((1L << (64 - i)) - 1L));
				}
				
			}else{
				result[j] = that.words[j];
			}
		}
		return new Genotype(result);
	}
	
	public long[] getWords(){
		return words;
	}
	
	public static void main(String[] args) {
		System.out.println(Long.toBinaryString(-1L));
	}
}
