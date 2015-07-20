package edu.lclark.orego.genetic;

import static edu.lclark.orego.genetic.Genotype.*;
import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.genetic.Phenotype.IGNORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.CopiableStructure;
import edu.lclark.orego.mcts.CopiableStructureFactory;

public class EvoRunnableTest {

	private Board board;

	private CoordinateSystem coords;

	private EvoRunnable runnable;

	private Player player;
	
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		CopiableStructure stuff = CopiableStructureFactory
				.escapePatternCapture(5, 7.5);
		player = new Player(2, stuff);
		runnable = new EvoRunnable(player, stuff);
		board = runnable.getBoard();
		coords = board.getCoordinateSystem();
	}

	@Test
	public void testFallback() {
		Phenotype black = runnable.getPhenotype(BLACK, 0);
		assertNotEquals(CoordinateSystem.NO_POINT,
				runnable.selectAndPlayOneMove(black, true));
	}

	@Test
	public void testPerformPlayout() {
		final Population[] populations = {new Population(5, 10, coords), new Population(5, 10, coords)};
		for (int i = 0; i < populations[0].size(); i++) {
			populations[0].getIndividuals()[i].setGenes(new int[] {makeGene(IGNORE, IGNORE, at("a3")), 0, 0, 0, 0});
			populations[1].getIndividuals()[i].setGenes(new int[] {makeGene(IGNORE, IGNORE, at("c1")), 0, 0, 0, 0});
		}
		runnable.setPopulations(populations);
		for (int i = 0; i < 10; i++) {
			String[] diagram = {
					"#.#.#",
					"#####",
					".....",
					"OOOOO",
					"O...O", };
			player.getBoard().setUpProblem(diagram, BLACK);
			assertEquals(WHITE, runnable.performPlayout(true));
		}
	}

	@Test
	public void testPerformSpecificPlayout() {
		Phenotype black = runnable.getPhenotype(BLACK, 0);
		black.setReply(IGNORE, IGNORE, at("a3"));
		Phenotype white = runnable.getPhenotype(WHITE, 0);
		white.setReply(IGNORE, IGNORE, at("c1"));
		for (int i = 0; i < 10; i++) {
			String[] diagram = {
					"#.#.#",
					"#####",
					".....",
					"OOOOO",
					"O...O", };
			player.getBoard().setUpProblem(diagram, BLACK);
			assertEquals(WHITE, runnable.playAgainst(black, white, true));
		}
	}

	@Test
	public void testReplyTypes() {
		Phenotype black = runnable.getPhenotype(BLACK, 0);
		black.setReply(at("a1"), at("b1"), at("c1"));
		black.setReply(IGNORE, at("b1"), at("d1"));
		black.setReply(at("a1"), IGNORE, at("d1"));
		black.setReply(IGNORE, IGNORE, at("b2"));
		board.clear();
		board.play("a1");
		board.play("b1");
		assertEquals(at("c1"), runnable.bestMove(black));
		board.clear();
		board.play("c2");
		board.play("b1");
		assertEquals(at("d1"), runnable.bestMove(black));
		board.clear();
		board.play("a1");
		board.play("c2");
		assertEquals(at("d1"), runnable.bestMove(black));
		board.clear();
		board.play("a2");
		board.play("a3");
		assertEquals(at("b2"), runnable.bestMove(black));
	}

	@Test
	public void testSelectAndPlayOneMove() {
		Phenotype black = runnable.getPhenotype(BLACK, 0);
		Phenotype white = runnable.getPhenotype(WHITE, 0);
		black.setReply(NO_POINT, NO_POINT, at("c3"));
		white.setReply(NO_POINT, at("c3"), at("d3"));
		assertEquals(at("c3"), runnable.selectAndPlayOneMove(black, true));
		assertEquals(at("d3"), runnable.selectAndPlayOneMove(white, true));
	}

}
