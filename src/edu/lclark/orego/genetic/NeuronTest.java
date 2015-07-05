package edu.lclark.orego.genetic;

import static org.junit.Assert.*;
import static edu.lclark.orego.genetic.Neuron.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
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
	public void testCountBits() {
		assertEquals(0, countBits(0L));
		assertEquals(3, countBits(0b00101001L));
		assertEquals(1, countBits(Long.MIN_VALUE));
		assertEquals(64, countBits(-1));
		assertEquals(36, countBits(0b0100110001101111011011100110011101001010011011110110100001101110L));
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
	
	@Test
	public void testExtractFeatures() {
		String[] diagram = {
				".....",
				".....",
				".....",
				"#....",
				".O...",
		};
		board.setUpProblem(diagram, BLACK);
		//n w e s nw ne sw se
		//ovef
		long[] features = new long[coords.getFirstPointBeyondExtendedBoard()];
		extractFeatures(board, features);
		assertEquals(0b0001, features[at("a2")]); // Friendly
		assertEquals(0b0010, features[at("b1")]); // Enemy
		assertEquals(0b0100, features[at("a1")]); // Vacant
		assertEquals(0b1000, features[coords.getNeighbors(at("a1"))[WEST_NEIGHBOR]]); // Off board
		board.setColorToPlay(WHITE);
		extractFeatures(board, features);
		assertEquals(0b0010, features[at("a2")]); // Enemy
		assertEquals(0b0001, features[at("b1")]); // Friendly
		assertEquals(0b0100, features[at("a1")]); // Vacant
		assertEquals(0b1000, features[coords.getNeighbors(at("a1"))[WEST_NEIGHBOR]]); // Off board
	}
	
	@Test
	public void testExtractFeaturesAround() {
		String[] diagram = {
				".....",
				".....",
				".....",
				"#....",
				".O...",
		};
		board.setUpProblem(diagram, BLACK);
		//n w e s nw ne sw se
		//ovef
		long[] extracted = new long[8];
		long[] blackToPlay = {0b0001, 0b1000, 0b0010, 0b1000, 0b1000, 0b0100, 0b1000, 0b1000};
		extractFeaturesAround(at("a1"), board, extracted);
		assertArrayEquals(blackToPlay, extracted);
		board.setColorToPlay(WHITE);
		long[] whiteToPlay = {0b0010, 0b1000, 0b0001, 0b1000, 0b1000, 0b0100, 0b1000, 0b1000};
		extractFeaturesAround(at("a1"), board, extracted);
		assertArrayEquals(whiteToPlay, extracted);
	}

}
