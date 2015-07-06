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

}
