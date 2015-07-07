package edu.lclark.orego.genetic;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class GenotypeTest {

	@Test
	public void testCross() {
		long bit0[] = {0L, 0L};
		long bit1[] = {-1L, -1L};
		Genotype a = new Genotype(bit0);
		Genotype b = new Genotype(bit1);
		int count0 = 0;
		int count64 = 0;
		int count127 = 0;
		int trials = 1000000;
		MersenneTwisterFast random = new MersenneTwisterFast();
		for(int i = 0; i < trials; i++){
			Genotype c = a.cross(b, random);
			if(((c.getBits()[0] >>> 63) & 1L) == 1){
				count0++;
			}
			if(((c.getBits()[1] >>> 63) & 1L) == 1){
				count64++;
			}
			if((c.getBits()[1] & 1L ) == 1){
				count127++;
			}
		}
		assertEquals(1.0/129, 1.0*count0/trials, .05);
		assertEquals(65.0/129, 1.0*count64/trials, .05);
		assertEquals(128.0/129, 1.0*count127/trials, .05);
		assertTrue(1.0*count0/trials > 0);
		assertTrue(1.0*count127/trials < 1);
	}
	
	@Test
	public void testMutate(){
		long bit0[] = {0L, 0L};
		long bit1[] = {-1L, -1L};
		Genotype a = new Genotype(bit0);
		Genotype b = new Genotype(bit1);
		MersenneTwisterFast random = new MersenneTwisterFast();
		a.mutate(random);
		b.mutate(random);
		int bitCountA = 0;
		int bitCountB = 0;
		for (int i = 0; i < a.getBits().length; i++){
			bitCountA += Long.bitCount(a.getBits()[i]);
			bitCountB += Long.bitCount(b.getBits()[i]);
		}
		assertTrue(bitCountA == 1);
		assertTrue(bitCountB == 127);
	}


}
