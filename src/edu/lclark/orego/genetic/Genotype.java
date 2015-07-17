package edu.lclark.orego.genetic;

import edu.lclark.orego.thirdparty.MersenneTwisterFast;

/** A set of genes. */
public class Genotype {

	private int[] genes;
	
	/**
	 * @param length
	 *            The number of genes in this genotype.
	 */
	public Genotype(int length) {
		genes = new int[length];
	}

	public Genotype(int[] genes) {
		this.genes = genes;
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
	public void mutate(MersenneTwisterFast random, short[] possiblePoints) {
		genes[random.nextInt(genes.length)] = randomGene(random, possiblePoints);
	}

	@SuppressWarnings("static-method")
	/** Generates a random gene, containing three moves from possiblePoints. */
	public int randomGene(MersenneTwisterFast random, short[] possiblePoints) {
		return possiblePoints[random.nextInt(possiblePoints.length)]
				| (possiblePoints[random.nextInt(possiblePoints.length)] << 9)
				| (possiblePoints[random.nextInt(possiblePoints.length)] << 18);
	}

	/** Fills this Genotype with random genes. */
	public void randomize(MersenneTwisterFast random, short[] possiblePoints) {
		for (int i = 0; i < genes.length; i++) {
			genes[i] = randomGene(random, possiblePoints);
		}
	}

	public void setGenes(int[] genes) {
		this.genes = genes;
	}

}
