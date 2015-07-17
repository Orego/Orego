package edu.lclark.orego.genetic;

import static edu.lclark.orego.experiment.GameBatch.timeStamp;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;
import static java.io.File.separator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class Population {

	public static final int NUMBER_OF_THREADS = 32;
	
	public static void main(String[] args) {
	    new Population(10000, 2000, CoordinateSystem.forWidth(19)).evolve(1000);
	}

	private Genotype[] individuals;

	private final MersenneTwisterFast random;

	private final short[] possiblePoints;
	
	public Population(int individualCount, int numberOfReplies, CoordinateSystem coords) {
		random = new MersenneTwisterFast();
		possiblePoints = new short[coords.getArea() + 2];
		int i = 0;
		for (short p : coords.getAllPointsOnBoard()) {
			possiblePoints[i] = p;
			i++;
		}
		possiblePoints[i] = NO_POINT;
		i++;
		possiblePoints[i] = RESIGN;
		individuals = new Genotype[individualCount];
		for (i = 0; i < individualCount; i++) {
			individuals[i] = new Genotype(numberOfReplies);
			individuals[i].randomize(random, possiblePoints);
		}
	}

	Genotype chooseParent() {
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

	public void evaluateAllFitness(PrintWriter stats, ObjectOutputStream champions) throws java.io.IOException, InterruptedException {
		// Indices where each thread should start
		int[] starts = new int[NUMBER_OF_THREADS];
		// Indices where each thread should stop
		int[] stops = new int[NUMBER_OF_THREADS];
		for (int i = 0; i < starts.length; i++) {
			starts[i] = individuals.length * i / NUMBER_OF_THREADS;
			stops[i] = individuals.length * (i + 1) / NUMBER_OF_THREADS;
		}
		int step = (int)Math.ceil(((double)individuals.length) / NUMBER_OF_THREADS);
		CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS);
		Evaluator[] evaluators = new Evaluator[NUMBER_OF_THREADS];
		for (int i = 0; i < evaluators.length; i++) {
			evaluators[i] = new Evaluator(step * i, Math.min(individuals.length, step * (i + 1)), individuals, latch);
		}
		ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		long before = System.nanoTime();
		for (int i = 0; i < NUMBER_OF_THREADS; i++) {
			executor.execute(evaluators[i]);
		}
		executor.shutdown();
		latch.await();
		long after = System.nanoTime();
		System.out.println("Time taken: " + ((after - before) / (60 * 1000000000.0)) + " minutes");
		Genotype champion = findChampion();
		stats.println(champion.getFitness() + "\t" + meanFitness());
		stats.flush();
		champions.writeObject(champion.getGenes());
	}

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
					nextGeneration[i] = chooseParent().cross(
							chooseParent(), random);
					nextGeneration[i].mutate(random, possiblePoints);
				}
				individuals = nextGeneration;
				stats.print(g + "\t");
				evaluateAllFitness(stats, champions);
			}
			try (ObjectOutputStream finalPopulation = new ObjectOutputStream(new FileOutputStream(resultsDirectory+ "finalPopulation-" + timeStamp(false) + ".data"))) {
				for (Genotype g : individuals) {
					finalPopulation.writeObject(g.getGenes());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
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
	
	public int size() {
		return individuals.length;
	}

}
