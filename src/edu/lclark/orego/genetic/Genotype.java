package edu.lclark.orego.genetic;

import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class Genotype {
	
	private long[] genotype;
	
	public Genotype(long [] genotype){
		this.genotype = genotype;
	}
	
	public Genotype(int numberOfGenotypes){
		genotype = new long[numberOfGenotypes];
	}
	
	public void setRules(long [] genotype){
		this.genotype = genotype;
	}
	
	public void generateRules(){
		MersenneTwisterFast random = new MersenneTwisterFast();
		for(long i: genotype){
		}
	}
	
	public Genotype performCrossover(Genotype A, Genotype B){
		return null;
	}
	
	public long[] getGenotype(){
		return genotype;
	}
	
	
}
