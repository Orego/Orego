package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.genetic.Phenotype.IGNORE;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.CopiableStructure;
import edu.lclark.orego.mcts.CopiableStructureFactory;

public class EvoRunnableTest {

	private EvoRunnable runnable;
	
	private Board board;
	
	private CoordinateSystem coords;
	
	@Before
	public void setUp() throws Exception {
		CopiableStructure stuff = CopiableStructureFactory.escapePatternCapture(5, 7.5);
		runnable = new EvoRunnable(stuff);
		board = runnable.getBoard();
		coords = board.getCoordinateSystem();
	}

	private short at(String label) {
		return coords.at(label);
	}

	@Test
	public void testPerformPlayout() {
		for (int i = 0; i < 10; i++) {
			Phenotype black = runnable.getPhenotype(BLACK, 0);
			black.setReply(IGNORE, IGNORE, at("a3"));
			Phenotype white = runnable.getPhenotype(WHITE, 0);
			white.setReply(IGNORE, IGNORE, at("c1"));
			String[] diagram = {
					"#.#.#",
					"#####",
					".....",
					"OOOOO",
					"O...O",
			};
			board.setUpProblem(diagram, BLACK);
			assertEquals(WHITE, runnable.performPlayout(black, white, true));
		}
	}

}
