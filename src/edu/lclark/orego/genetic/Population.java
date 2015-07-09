package edu.lclark.orego.genetic;

public class Population {
	
	Genotype[] individuals;
	
	Population(int individualCount){
		individuals = new Genotype[individualCount];
		for (int i = 0; i < individualCount; i++){
			individuals[i] = new Genotype(5 + 64*19+361*361*8 + 361);
			individuals[i].randomize();
		}
	}
	
	public static void main(String[] args) {
		Population p = new Population(2);
		p.evaluateAllFitness();
		double [] fit = p.getAllFitnesses();
		System.out.println("Max fitness: " + p.maxFitness(p.getAllFitnesses()));
		System.out.println("Mean: " + p.meanFitness(p.getAllFitnesses()));
	}
	
	public void evaluateAllFitness(){
		for (Genotype individual : individuals){
			individual.evaluateFitness();
			System.out.println(individual.getFitness());
		}
	}
	
	public double[] getAllFitnesses(){
		double [] fitnesses = new double [individuals.length];
		for (int i = 0; i < fitnesses.length; i++){
			fitnesses[i] = individuals[i].getFitness();
		}
		return fitnesses;
	}
	
	public double maxFitness(double[] fitnesses){
		double max = -1;
		for (int i = 0; i < fitnesses.length; i++){
			if (fitnesses[i] > max){
				max = fitnesses[i];
			}
		}
		return max;
	}
	
	public double meanFitness(double[] fitnesses){
		double sum = 0;
		for (int i = 0; i < fitnesses.length; i++){
			sum += fitnesses[i];
		}
		return sum/fitnesses.length;
	}

}
