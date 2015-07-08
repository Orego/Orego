package edu.lclark.orego.genetic;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.genetic.Phenotype.*;

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
		board = new Board(19);
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
		long[] words = new long[5 + 64*19+361*361*8 + 361];
		// Replies
		words[0] = at("a1") |
				(at("b1") << 9) |
				(at("c1") << 18) |
				(BLACK.index() << 27) |
				((long) coords.getFirstPointBeyondBoard() << 32) |
				((long) at("b3") << 41) |
				((long) at("c5") << 50) |
				((long) BLACK.index() << 59);
		words[4] = at("a1") |
				(at("b1") << 9) |
				(at("d1") << 18) |
				(BLACK.index() << 27) |
				((long) coords.getFirstPointBeyondBoard() << 32) |
				((long) at("b3") << 41) |
				((long) at("d2") << 50) |
				((long) WHITE.index() << 59);
		// Convolutional layer
		// Note that all indices below are zero-based
		words[24] = 0b1101010111010111L; // Threshold for neuron 1
		words[26] = 0b101L; // 1th excitatory vector in neuron 1
		words[58] = 0b111L; // 5th inhibitory vector in neuron 2
		words[1221] = 0b10L;
		phenotype = new Phenotype(board, 10, new Genotype(words));
		assertEquals(at("d1"), phenotype.getReply(BLACK, at("a1"), at("b1")));
		assertEquals(at("c5"), phenotype.getReply(BLACK, at("e3"), at("b3")));
		assertEquals(at("d2"), phenotype.getReply(WHITE, at("e3"), at("b3")));
		ConvolutionalNeuron n1 = phenotype.getConvolutionalLayer().getNeurons()[1];
		assertEquals(0b1101010111010111L % THRESHOLD_LIMIT, n1.getThreshold());
		assertEquals(0b101L, n1.getExcitation()[1]);
		ConvolutionalNeuron n2 = phenotype.getConvolutionalLayer().getNeurons()[2];
		assertEquals(0b111L, n2.getInhibition()[5]);
		//Linear Layer
		byte testWeight = phenotype.getLinearLayer().getWeight(at("a19"), at("a19"), 0);
		System.out.println(testWeight);
		assertEquals(0b10L, testWeight);
	
	}

}
