package edu.lclark.orego.neural;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class InputNeuronTest {
	
	private InputNeuron neuron;

	@Before
	public void setUp() {
		neuron = new InputNeuron();
	}

	@Test
	/**Tests both set and get*/
	public void testGetActivation() {
		neuron.setActivation(5.0);
		assertEquals(5.0, neuron.getActivation(), .001);
		neuron.setActivation(-1.1);
		assertEquals(-1.1, neuron.getActivation(), .001);
	}

}
