package edu.lclark.orego.neural;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SigmoidNeuronTest {
	
	public SigmoidNeuron neuron;

	@Test
	public void testComputeActivation() {
		InputNeuron a = new InputNeuron();
		InputNeuron b = new InputNeuron();
		a.setActivation(2);
		b.setActivation(1);
		neuron = new SigmoidNeuron(new Neuron[] {a, b}, new double[] {1, 2});
		neuron.updateActivation();
		assertEquals(0.98201379003, neuron.getActivation(), .0001);
	}

	@Test
	public void testUpdateDelta() {
		InputNeuron a = new InputNeuron();
		neuron = new SigmoidNeuron(new Neuron[] {a, a}, new double[] {2, 3});
		neuron.setActivation(0.1);
		neuron.updateDelta(1);
		assertEquals(0.081, neuron.getDelta(), .0001);
	}
	
	@SuppressWarnings("static-method")
	@Test
	public void testRand() {
		for (int i=0; i<100; i++){
			double x = SigmoidNeuron.rand();
			assertTrue((SigmoidNeuron.MIN_INITIAL_WEIGHT <= x) && (x < SigmoidNeuron.MAX_INITIAL_WEIGHT));
		}
	}

	@Test
	public void testCalculateDeltaOutput() {
		InputNeuron a = new InputNeuron();
		InputNeuron b = new InputNeuron();
		InputNeuron bias = new InputNeuron();
		bias.setActivation(1);
		neuron = new SigmoidNeuron(new Neuron[] {a, b, bias}, new double[] {1, 1, 1});
		a.setActivation(1);
		b.setActivation(0);
		neuron.updateActivation();
		neuron.updateDelta(1);
		assertEquals(neuron.getDelta(), 0.0125155, .001);
	}


}
