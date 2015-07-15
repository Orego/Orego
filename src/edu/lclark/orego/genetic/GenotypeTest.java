package edu.lclark.orego.genetic;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.lclark.orego.thirdparty.MersenneTwisterFast;

@SuppressWarnings("static-method")
public class GenotypeTest {

	@Test
	public void testCross() {
		Genotype a = new Genotype(new int[] {0, 0, 0});
		Genotype b = new Genotype(new int[] {1, 1, 1});
		int trials = 1000000;
		int[] counts = new int[a.getGenes().length];
		MersenneTwisterFast random = new MersenneTwisterFast();
		for(int i = 0; i < trials; i++){
			Genotype c = a.cross(b, random);
			for (int j = 0; j < counts.length; j++) {
				if (c.getGenes()[j] == 0) {
					counts[j]++;
				}
			}
		}
		assertEquals(2.0/3, 1.0*counts[0]/trials, .05);
		assertEquals(1.0/3, 1.0*counts[1]/trials, .05);
		assertEquals(0.0/3, 1.0*counts[2]/trials, .00000001);
	}
	
	@Test
	public void testMutate() {
		Genotype a = new Genotype(new int[] {0, 0, 0});
		MersenneTwisterFast random = new MersenneTwisterFast();
		int[] points = {1, 2};		
		a.mutate(random, points);
		int count = 0;
		for (int gene : a.getGenes()) {
			if (gene != 0) {
				int p0 = gene & ((1 << 9) - 1);
				assertTrue(1 == p0 || 2 == p0);
				int p1 = (gene >>> 9) & ((1 << 9) - 1);
				assertTrue(1 == p1 || 2 == p1);
				int p2 = (gene >>> 18) & ((1 << 9) - 1);
				assertTrue(1 == p2 || 2 == p2);
				count++;
			}
		}
		assertEquals(1, count);
	}

}
