package edu.lclark.orego.genetic;

import static org.junit.Assert.*;
import static edu.lclark.orego.genetic.Neuron.*;
import static edu.lclark.orego.core.StoneColor.*;
import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

public class NeuronTest {

	private Neuron neuron;
	
	@Before
	public void setUp() throws Exception {
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
		// For this neuron to fire, there must be 3 more bits in position 0 (rightmost) than
		// in position 1
		neuron = new Neuron(3, 0b01L, 0b10L);
		assertTrue(neuron.isActive(0b01L, 0b11L, 0b01L, 0b01L));
		assertTrue(neuron.isActive(0b01L, 0b01L, 0b01L, 0b01L));
		assertFalse(neuron.isActive(0b11L, 0b11L, 0b01L, 0b01L));
		assertFalse(neuron.isActive(0b01L, 0b11L, 0b01L, 0b00L));
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
		Board board = new Board(5);
		board.setUpProblem(diagram, BLACK);
		//n w e s nw ne sw se
		//ovef
		long[] extracted = new long[8];
		long[] blackToPlay = {0b0001, 0b1000, 0b0010, 0b1000, 0b1000, 0b0100, 0b1000, 0b1000};
		extractFeaturesAround(board.getCoordinateSystem().at("a1"), board, extracted);
		assertArrayEquals(blackToPlay, extracted);
		board.setColorToPlay(WHITE);
		long[] whiteToPlay = {0b0010, 0b1000, 0b0001, 0b1000, 0b1000, 0b0100, 0b1000, 0b1000};
		extractFeaturesAround(board.getCoordinateSystem().at("a1"), board, extracted);
		assertArrayEquals(whiteToPlay, extracted);
	}

}
