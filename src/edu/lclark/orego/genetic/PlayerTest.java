package edu.lclark.orego.genetic;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;

import org.junit.Before;
import org.junit.Test;

public class PlayerTest {

	private Player player;
	
	@Before
	public void setUp() throws Exception {
		player = new PlayerBuilder().populationSize(0).individualLength(0).msecPerMove(1000).threads(5).boardWidth(5).contestants(6).openingBook(false).build();
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
		player.createPopulations(2000, 2000);
		assertEquals(player.getBoard().getCoordinateSystem().at("c1"), player.bestMove());
	}
	
	@Test
	public void testBestMove2() {
		player = new PlayerBuilder().populationSize(0).individualLength(0).msecPerMove(2000).threads(5).boardWidth(9).contestants(6).openingBook(false).build();
		String[] diagram = {
				".#######.",
				"#########",
				"#########",
				"#########",
				"OOOOOOOOO",
				"OOOOOOOOO",
				"OOO..OOOO",
				"OOO...OOO",
				"OOOO.OOOO",
				};
		player.getBoard().setUpProblem(diagram, BLACK);
		player.createPopulations(2000, 2000);
		assertEquals(player.getBoard().getCoordinateSystem().at("e2"), player.bestMove());
	}

}
