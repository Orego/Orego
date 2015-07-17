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

	private Board board;

	private CoordinateSystem coords;

	private Phenotype phenotype;

	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		phenotype = new Phenotype(coords);
	}

	@Test
	public void testInstallGenes() {
		int[] words = new int[5 + 5];
		words[0] = at("a1") | (at("b1") << 9) | (at("c1") << 18);
		words[4] = at("a1") | (at("b1") << 9) | (at("d1") << 18);
		phenotype.installGenes(new Genotype(words));
		board.play("a1");
		board.play("b1");
		assertEquals(at("d1"), phenotype.getRawReply(at("a1"), at("b1")));
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
		assertEquals(at("c1"), phenotype.getRawReply(at("a1"), at("b1")));
		board.clear();
		board.play("g1");
		board.play("b1");
		assertEquals(at("d1"), phenotype.replyToOneMove(at("b1")));
		board.clear();
		board.play("a1");
		board.play("g1");
		assertEquals(at("e1"), phenotype.followUp(at("a1")));
		board.clear();
		board.play("g1");
		board.play("g2");
		assertEquals(at("f1"), phenotype.playBigPoint());
	}

}
