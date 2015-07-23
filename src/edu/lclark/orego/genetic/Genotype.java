package edu.lclark.orego.genetic;

import java.util.concurrent.locks.ReentrantLock;

import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

/** A set of genes. */
public class Genotype {

	private int[] genes;
	
	private final ReentrantLock lock;
	
	/**
	 * @param length
	 *            The number of genes in this genotype.
	 */
	public Genotype(int length) {
		this(new int[length]);
	}

	public ReentrantLock getLock() {
		return lock;
	}

	public Genotype(int[] genes) {
		this.genes = genes;
		lock = new ReentrantLock();
	}

	/** Crosses this and that, overwriting the genes of child with the result. */
	public void cross(Genotype that, Genotype child, MersenneTwisterFast random) {
		final int crossoverPoint = random.nextInt(genes.length);
		int[] childGenes = child.getGenes();
		System.arraycopy(genes, 0, childGenes, 0, crossoverPoint);
		System.arraycopy(that.genes, crossoverPoint, childGenes, crossoverPoint, childGenes.length - crossoverPoint);
	}
	
	public int[] getGenes() {
		return genes;
	}

	/** Randomly replaces one of the genes in this Genotype. */
	public void mutate(MersenneTwisterFast random, EvoRunnable runnable, StoneColor color) {
		genes[random.nextInt(genes.length)] = runnable.nextGene(color);
	}

	@SuppressWarnings("static-method")
	/** Generates a random gene, containing three moves from possiblePoints. */
	public int randomGene(MersenneTwisterFast random, short[] possiblePoints) {
		return possiblePoints[random.nextInt(possiblePoints.length)]
				| (possiblePoints[random.nextInt(possiblePoints.length)] << 9)
				| (possiblePoints[random.nextInt(possiblePoints.length)] << 18);
	}

	/** Returns a gene where reply is the reply to penultimate followed by ultimate. */
	public static int makeGene(short penultimate, short ultimate, short reply) {
		return penultimate | (ultimate << 9) | (reply << 18);
	}

	public void setGenes(int[] genes) {
		this.genes = genes;
	}

	public void initialize(EvoRunnable runnable, StoneColor color) {
		for (int i = 0; i < genes.length; i++) {
			genes[i] = runnable.nextGene(color);
		}
	}

}
