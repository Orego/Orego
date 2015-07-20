package edu.lclark.orego.genetic;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;
import org.junit.Before;
import org.junit.Test;

public class PlayerTest {

	private Player player;
	
	@Before
	public void setUp() throws Exception {
		player = new PlayerBuilder().populationSize(20000).individualLength(2000).msecPerMove(60000).threads(5).boardWidth(5).openingBook(false).build();
	}

	@Test
	public void testCreatePopulations() {
		assertEquals(2000, player.getPopulations()[BLACK.index()].size());
	}

	@Test
	public void testBestMove() {
		String[] diagram = {
				"#.#.#",
				"#####",
				".....",
				"OOOOO",
				"O...O", };
		player.getBoard().setUpProblem(diagram, BLACK);
		assertEquals(player.getBoard().getCoordinateSystem().at("c1"), player.bestMove());
	}

}
