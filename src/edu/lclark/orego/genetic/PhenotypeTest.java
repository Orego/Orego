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
		phenotype.setColorToPlay(BLACK);
	}

	private short at(String label) {
		return coords.at(label);
	}
	
	@Test
	public void testContextAt() {
		assertEquals(
				0b1010101010101010101010101010101010101010101010101010101010101010L,
				phenotype.contextAt(at("g8")));
		board.play(at("f8"));
		assertEquals(
				0b1010101010101010101010101010101010101010101010101010101010101000L,
				phenotype.contextAt(at("g8")));
		assertEquals(
				0b1011101010101010101010101010101010101010001010101010101010101010L, 
				phenotype.contextAt(at("d8")));
		board.play(at("d7"));
		board.play(at("c8"));
	}

	@Test
	public void testContextToString(){
		String test = 
					"    .\n" +
					"    .\n" +
					"  .....\n" +
					"  .....\n" +
					".... ....\n" +
					"  .....\n" +
					"  .....\n" +
					"    .\n" +
					"    .\n";
		assertEquals(test, phenotype.contextToString(phenotype.contextAt(at("e9"))));
		board.play(at("f8"));		
		board.play(at("d7"));
		board.play(at("c8"));
		test = 
				"    .\n" +
				"    .\n" +
				"  .....\n" +
				"  .....\n" +
				"?..# .#..\n" +
				"  ..O..\n" +
				"  .....\n" +
				"    .\n" +
				"    .\n";
		assertEquals(test, phenotype.contextToString(phenotype.contextAt(at("d8"))));

	}

	
	@Test
	public void testSelectAndPlayOneMove() {
		phenotype.addContext(phenotype.contextAt(at("c3")));
		phenotype.setReply(NO_POINT, at("c3"), at("d3"));
		MersenneTwisterFast random = new MersenneTwisterFast();
		assertEquals(at("c3"), phenotype.selectAndPlayOneMove(random, true));
		//TODO: This next one was specific to white
//		phenotype.setColorToPlay(WHITE);
//		assertEquals(at("d3"), phenotype.selectAndPlayOneMove(random, true));
	}

	@Test
	public void testGenotypeConstructor() {
		int[] words = new int[5 + 5];
		// Replies
		words[0] = at("a1") |
				(at("b1") << 9) |
				(at("c1") << 18);
		words[4] = at("a1") |
				(at("b1") << 9) |
				(at("d1") << 18) ;
		// Contexts
		words[7] = 1;
		words[9] = -1;
		// Build and test phenotype
		phenotype = new Phenotype(board, new Genotype(words), BLACK);
		assertEquals(at("d1"), phenotype.getReply(at("a1"), at("b1")));
		//TODO: This next one was specific to white
//		assertEquals(at("c5"), phenotype.getReply(at("e3"), at("b3")));
		assertEquals(at("d2"), phenotype.getReply( at("e3"), at("b3")));
		assertTrue(phenotype.containsContext(1L));
		assertFalse(phenotype.containsContext(2L));
	}

	@SuppressWarnings("boxing")
	@Test
	public void testHits() {
		board.play("c3");
		// This white move shouldn't be found
		phenotype.addContext(phenotype.contextAt(at("d3")));
		board.clear();
		phenotype.setReply(NO_POINT, NO_POINT, at("c3"));
		phenotype.setReply(at("e3"), at("f3"), at("g3"));
		Short[] game = {at("c3"), at("d3"), at("e3"), at("f3"), at("g3"), PASS, PASS};
		assertEquals(2, phenotype.hits(game));
	}

}
