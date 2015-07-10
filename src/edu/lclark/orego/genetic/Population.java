package edu.lclark.orego.genetic;

import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class Population {
	
	private Genotype[] individuals;
	
	public Population(int individualCount){
		random = new MersenneTwisterFast();
		individuals = new Genotype[individualCount];
		for (int i = 0; i < individualCount; i++){
			individuals[i] = new Genotype(5 + 64*19+361*361*8 + 361);
			individuals[i].randomize();
		}
	}
	
	public static void main(String[] args) {
		new Population(100).evolve(50);
	}
	
	private MersenneTwisterFast random;
	
	public void evolve(int generations) {
		System.out.println("Generation\tMax\tMean");
		System.out.print("0\t");
		evaluateAllFitness();
		for (int g = 1; g <= generations; g++) {
			Genotype[] nextGeneration = new Genotype[individuals.length];
			for (int i = 0; i < nextGeneration.length; i++) {
				nextGeneration[i] = chooseParent(random).cross(chooseParent(random), random);
				nextGeneration[i].mutate(random);
			}
			individuals = nextGeneration;
			System.out.print(g + "\t");
			evaluateAllFitness();
		}
	}
	
	Genotype chooseParent(MersenneTwisterFast random) {
		Genotype best = individuals[random.nextInt(individuals.length)];
		for (int i = 0; i < 9; i++) {
			Genotype challenger = individuals[random.nextInt(individuals.length)];
			if (challenger.getFitness() > best.getFitness()) {
				best = challenger;
			}
		}
		return best;
	}
	
	public void evaluateAllFitness(){
		for (Genotype individual : individuals){
			individual.evaluateFitness();
		}
		System.out.println(maxFitness() + "\t" + meanFitness());
	}
	
	public double maxFitness(){
		double max = -1;
		for (int i = 0; i < individuals.length; i++){
			if (individuals[i].getFitness() > max){
				max = individuals[i].getFitness();
			}
		}
		return max;
	}
	
	public double meanFitness(){
		double sum = 0;
		for (int i = 0; i < individuals.length; i++){
			sum += individuals[i].getFitness();
		}
		return sum/individuals.length;
	}

}
