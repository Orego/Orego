package edu.lclark.orego.genetic;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class PopulationTest {

	private Population population;
	
	@Before
	public void setUp() throws Exception {
		population = new Population(100, 10, CoordinateSystem.forWidth(5));
	}

	@Test
	public void testReplaceLoser() {
		Genotype[] individuals = population.getIndividuals();
		int[] zeroes = new int[10];
		java.util.Arrays.fill(zeroes, 0);
		int[] ones = new int[10];
		java.util.Arrays.fill(ones, 1);
		individuals[0].setGenes(zeroes);
		for (int i = 1; i < individuals.length; i++) {
			individuals[i].setGenes(ones);
		}
		population.replaceLoser(0, 1, 2, new MersenneTwisterFast());
		assertFalse(java.util.Arrays.equals(new int[10], individuals[0].getGenes()));
	}

}
