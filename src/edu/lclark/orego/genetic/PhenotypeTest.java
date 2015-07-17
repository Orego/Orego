package edu.lclark.orego.genetic;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.genetic.Phenotype.*;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.*;
import edu.lclark.orego.mcts.CopiableStructure;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class PhenotypeTest {

	// TODO Make this class compile; move tests to EvoRunnableTest as appropriate
	// TODO Test and implement Phenotype.installGenes
	
	private Phenotype phenotype;
	
	private Board board;
	
	private CopiableStructure richBoard;
	
	private CoordinateSystem coords;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		richBoard = Phenotype.makeRichBoard(board, 7.5);
		phenotype = new Phenotype(richBoard);
	}

	private short at(String label) {
		return coords.at(label);
	}
	
	@Test
	public void testSelectAndPlayOneMove() {
		phenotype.setReply(NO_POINT, NO_POINT, at("c3"));
		phenotype.setReply(NO_POINT, at("c3"), at("d3"));
		MersenneTwisterFast random = new MersenneTwisterFast();
		assertEquals(at("c3"), phenotype.selectAndPlayOneMove(random, true));
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
		// Build and test phenotype
		phenotype = new Phenotype(richBoard, new Genotype(words));
		board.play("a1");
		board.play("b1");
		assertEquals(at("d1"), phenotype.bestMove());
		board.play("d1");
		assertEquals(NO_POINT, phenotype.bestMove());
	}

	@Test
	public void testReplyTypes() {
		phenotype.setReply(at("a1"), at("b1"), at("c1"));
		phenotype.setReply(IGNORE, at("b1"), at("d1"));
		phenotype.setReply(at("a1"), IGNORE, at("e1"));
		phenotype.setReply(IGNORE, IGNORE, at("f1"));
		board.clear();
		board.play("a1");
		board.play("b1");
		assertEquals(at("c1"), phenotype.bestMove());
		board.clear();
		board.play("g1");
		board.play("b1");
		assertEquals(at("d1"), phenotype.bestMove());
		board.clear();
		board.play("a1");
		board.play("g1");
		assertEquals(at("e1"), phenotype.bestMove());
		board.clear();
		board.play("g1");
		board.play("g1");
		assertEquals(at("f1"), phenotype.bestMove());
	}

	@Test
	public void testFallback() {
		assertNotEquals(CoordinateSystem.NO_POINT, phenotype.selectAndPlayOneMove(new MersenneTwisterFast(), true));
	}

}
