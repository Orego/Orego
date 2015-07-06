package edu.lclark.orego.genetic;

import static org.junit.Assert.*;
import static edu.lclark.orego.genetic.Neuron.*;
import static edu.lclark.orego.core.StoneColor.*;
import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class NeuronTest {

	private Neuron neuron;
	
	private Board board;
	
	private CoordinateSystem coords;
	
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
	}

	@Test
	public void testIsActive() {
		// For this neuron to fire, there must be 3 more friendly stones than enemy stones in the receptive field
		neuron = new Neuron(3, 0b01L, 0b10L);
		String[] diagram = {
				"..O..",
				"#O.O.",
				".#O..",
				"##...",
				".#...",
		};
		board.setUpProblem(diagram, BLACK);
		long[] features = new long[coords.getFirstPointBeyondExtendedBoard()];
		extractFeatures(board, features);
		assertTrue(neuron.isActiveAt(at("a1"), features, coords));
		assertTrue(neuron.isActiveAt(at("a3"), features, coords));
		assertFalse(neuron.isActiveAt(at("c2"), features, coords));
		board.setUpProblem(diagram, WHITE);
		extractFeatures(board, features);
		assertTrue(neuron.isActiveAt(at("c4"), features, coords));
		assertFalse(neuron.isActiveAt(at("d3"), features, coords));
	}

}
