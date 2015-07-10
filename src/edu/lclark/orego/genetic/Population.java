package edu.lclark.orego.genetic;

import static edu.lclark.orego.experiment.GameBatch.timeStamp;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;
import static java.io.File.separator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class Population {

	private Genotype[] individuals;

	public Population(int individualCount) {
		random = new MersenneTwisterFast();
		individuals = new Genotype[individualCount];
		for (int i = 0; i < individualCount; i++) {
			individuals[i] = new Genotype(5 + 64 * 19 + 361 * 361 * 8 + 361);
			individuals[i].randomize();
		}
	}

	public static void main(String[] args) {
		new Population(2).evolve(3);
	}

	private MersenneTwisterFast random;

	public void evolve(int generations) {
		String resultsDirectory = SYSTEM.resultsDirectory + timeStamp(true)
				+ separator;
		new File(resultsDirectory).mkdirs();
		try (
			PrintWriter stats = new PrintWriter(resultsDirectory + "stats-" + timeStamp(false)
					+ ".txt");
			ObjectOutputStream champions = new ObjectOutputStream(new FileOutputStream(resultsDirectory + "champions-" + timeStamp(false)
					+ ".data"))) {
			stats.println("Generation\tMax\tMean");
			stats.print("0\t");
			evaluateAllFitness(stats, champions);
			for (int g = 1; g <= generations; g++) {
				Genotype[] nextGeneration = new Genotype[individuals.length];
				for (int i = 0; i < nextGeneration.length; i++) {
					nextGeneration[i] = chooseParent(random).cross(
							chooseParent(random), random);
					nextGeneration[i].mutate(random);
				}
				individuals = nextGeneration;
				stats.print(g + "\t");
				evaluateAllFitness(stats, champions);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	Genotype chooseParent(MersenneTwisterFast random) {
		Genotype best = individuals[random.nextInt(individuals.length)];
		for (int i = 0; i < 9; i++) {
			Genotype challenger = individuals[random
					.nextInt(individuals.length)];
			if (challenger.getFitness() > best.getFitness()) {
				best = challenger;
			}
		}
		return best;
	}

	public void evaluateAllFitness(PrintWriter stats, ObjectOutputStream champions) throws java.io.IOException {
		for (Genotype individual : individuals) {
			individual.evaluateFitness();
		}
		Genotype champion = findChampion();
		stats.println(champion.getFitness() + "\t" + meanFitness());
		champions.writeObject(champion.getWords());
	}

	public Genotype findChampion() {
		double max = Double.NEGATIVE_INFINITY;
		int bestIndex = -1;
		for (int i = 0; i < individuals.length; i++) {
			if (individuals[i].getFitness() > max) {
				max = individuals[i].getFitness();
				bestIndex = i;
			}
		}
		return individuals[bestIndex];
	}

	public double meanFitness() {
		double sum = 0;
		for (int i = 0; i < individuals.length; i++) {
			sum += individuals[i].getFitness();
		}
		return sum / individuals.length;
	}

}
