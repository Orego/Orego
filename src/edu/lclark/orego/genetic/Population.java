package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.genetic.Phenotype.IGNORE;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

/** A collection of Genotypes. */
public class Population {

	public static final int NUMBER_OF_THREADS = 32;

	private Genotype[] individuals;

	/** Points that can appear in genes: on-board points, NO_POINT, and IGNORE. */
	private final short[] possiblePoints;
	
	public Population(int individualCount, int numberOfReplies, CoordinateSystem coords) {
		final MersenneTwisterFast random = new MersenneTwisterFast();
		possiblePoints = new short[coords.getArea() + 2];
		int i = 0;
		for (short p : coords.getAllPointsOnBoard()) {
			possiblePoints[i] = p;
			i++;
		}
		possiblePoints[i] = NO_POINT;
		i++;
		possiblePoints[i] = IGNORE;
		individuals = new Genotype[individualCount];
		for (i = 0; i < individualCount; i++) {
			individuals[i] = new Genotype(numberOfReplies);
			individuals[i].randomize(random, possiblePoints);
		}
	}
	
	public int size() {
		return individuals.length;
	}

	/** Returns a random individual from this Population. */
	public Genotype randomGenotype(MersenneTwisterFast random) {
		return individuals[random.nextInt(individuals.length)];
	}

	public Genotype[] getIndividuals() {
		return individuals;
	}

}
