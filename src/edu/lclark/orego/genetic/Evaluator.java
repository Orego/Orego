package edu.lclark.orego.genetic;

import java.util.concurrent.CountDownLatch;

/** Helper for evaluating fitness. */
public class Evaluator implements Runnable {

	private int start;
	
	private int stop;
	
	private Genotype[] individuals;
	
	private CountDownLatch latch;
	
	private final int numberOfReplyLongs;
	
	public Evaluator(int start, int stop, Genotype[] individuals,
			CountDownLatch latch, int numberOfReplyLongs) {
		this.start = start;
		this.stop = stop;
		this.individuals = individuals;
		this.latch = latch;
		this.numberOfReplyLongs = numberOfReplyLongs;
	}

	@Override
	public void run() {
		for (int i = start; i < stop; i++) {
			individuals[i].evaluateFitness(numberOfReplyLongs);
		}
		latch.countDown();
	}

}
