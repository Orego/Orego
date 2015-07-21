package edu.lclark.orego.genetic;

import static edu.lclark.orego.genetic.Phenotype.IGNORE;
import static org.junit.Assert.assertEquals;
import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

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
		phenotype.installGenes(-1, new Genotype(words));
		board.play("a1");
		board.play("b1");
		assertEquals(at("d1"), phenotype.replyToTwoMoves(at("a1"), at("b1")));
	}

	@Test
	public void testClearReplies() {
		phenotype.setReply(at("a1"), at("b1"), at("c1"));
		board.clear();
		board.play("a1");
		board.play("b1");
		assertEquals(at("c1"), phenotype.replyToTwoMoves(at("a1"), at("b1")));
		int[] words = new int[5 + 5];
		words[4] = at("a2") | (at("b2") << 9) | (at("c2") << 18);
		phenotype.installGenes(-1, new Genotype(words));
		assertEquals(NO_POINT, phenotype.replyToTwoMoves(at("a1"), at("b1")));
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
		assertEquals(at("c1"), phenotype.replyToTwoMoves(at("a1"), at("b1")));
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
