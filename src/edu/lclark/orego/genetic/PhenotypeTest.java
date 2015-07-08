package edu.lclark.orego.genetic;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class PhenotypeTest {

	private Phenotype phenotype;
	
	private Board board;
	
	private CoordinateSystem coords;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		phenotype = new Phenotype(board);
	}

	private short at(String label) {
		return coords.at(label);
	}

	@Test
	public void testSelectAndPlayOneMove() {
		phenotype.setReply(WHITE, NO_POINT, at("c3"), at("d3"));
		phenotype.setBias(at("c3"), (byte)1);
		MersenneTwisterFast random = new MersenneTwisterFast();
		assertEquals(at("c3"), phenotype.selectAndPlayOneMove(random, true));
		assertEquals(at("d3"), phenotype.selectAndPlayOneMove(random, true));
	}

	@Test
	public void testSelectAndPlayOneMoveInvolvingConvolutionalLayer() {
		// This neuron only fires if there are off-board points west, southwest, and south
		long[] excitatory = new long[9];
		excitatory[3] = 0b1000L;
		excitatory[6] = 0b1000L;
		excitatory[7] = 0b1000L;
		long[] inhibitory = new long[9];
		phenotype.getConvolutionalLayer().setNeurons(new ConvolutionalNeuron[] {new ConvolutionalNeuron(3, excitatory, inhibitory)});
		for (short p : coords.getAllPointsOnBoard()) {
			phenotype.getLinearLayer().setWeight(p, p, 0, (byte)127);
		}
		MersenneTwisterFast random = new MersenneTwisterFast();
		assertEquals(at("a1"), phenotype.selectAndPlayOneMove(random, true));		
	}

	@Test
	public void testGenotypeConstructor() {
		long[] words = new long[1];
		words[0] = at("a1") |
				(at("b1") << 9) |
				(at("c1") << 18) |
				(BLACK.index() << 27) |
				((long) coords.getFirstPointBeyondBoard() << 32) |
				((long) at("b3") << 41) |
				((long) at("c5") << 50) |
				((long) BLACK.index() << 59);	
		phenotype = new Phenotype(board, 2, new Genotype(words));
		assertEquals(at("c1"), phenotype.getReply(BLACK, at("a1"), at("b1")));
		assertEquals(at("c5"), phenotype.getReply(BLACK, RESIGN, at("b3")));
	}

}
