package edu.lclark.orego.genetic;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;

public class PlayerTest {

	private Player player;
	
	private CoordinateSystem coords;

	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		player = new PlayerBuilder().populationSize(0).individualLength(0).msecPerMove(0).threads(5).boardWidth(5).contestants(6).openingBook(false).build();
		coords = player.getBoard().getCoordinateSystem();
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
		player.getEvoRunnable(0).printFirstMoveCounts();
		System.out.println("Creating populations");
		player.createPopulations(2000, 2000);
		player.getEvoRunnable(0).printFirstMoveCounts();
		player.getPopulations()[BLACK.index()].printGeneFrequency(NO_POINT, NO_POINT, at("c1"), coords);
		player.getPopulations()[BLACK.index()].printGeneFrequency(NO_POINT, NO_POINT, at("d1"), coords);
//		assertEquals(player.getBoard().getCoordinateSystem().at("c1"), player.bestMove());
	}
	
	@Test
	public void testBestMove2() {
		player = new PlayerBuilder().populationSize(0).individualLength(0).msecPerMove(0).threads(5).boardWidth(9).contestants(6).openingBook(false).build();
		coords = player.getBoard().getCoordinateSystem();
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
		player.getPopulations()[BLACK.index()].printGeneFrequency(NO_POINT, NO_POINT, at("e2"), coords);
		player.getPopulations()[WHITE.index()].printGeneFrequency(NO_POINT, at("e2"), at("e3"), coords);
		player.getPopulations()[WHITE.index()].printGeneFrequency(NO_POINT, at("e2"), at("d2"), coords);
		player.getPopulations()[BLACK.index()].printGeneFrequency(at("e2"), at("e3"), at("d2"), coords);
		player.getPopulations()[BLACK.index()].printGeneFrequency(at("e2"), at("d2"), at("e3"), coords);
		assertEquals(player.getBoard().getCoordinateSystem().at("e2"), player.bestMove());
	}

}
