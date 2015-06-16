package edu.lclark.orego.neural;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class OutputLayerTest {

	private Layer in;

	private OutputLayer out;

	private static float f(float x) {
		return (float) (1 / (1 + Math.exp(-x)));
	}

	@Before
	public void setUp() throws Exception {
		in = new Layer(3);
		out = new OutputLayer(2, in);
	}

	@Test
	public void testUpdateNetInputs() {
		out.setWeights(new float[][] { { 0.0f, 1.0f, 2.0f, 3.0f },
				{ 1.0f, -1.0f, 0.0f, 0.0f } });
		in.setActivations(3, 2, 1);
		out.updateNetInputs();
		assertArrayEquals(new float[] { 10, -2 }, out.getNetInputs(), 0.001f);
	}

	@Test
	public void testUpdateActivations() {
		out.setWeights(new float[][] { { 0.0f, 1.0f, 2.0f, 3.0f },
				{ 1.0f, -1.0f, 0.0f, 0.0f } });
		in.setActivations(3, 2, 1);
		out.updateActivations();
		assertArrayEquals(new float[] { 1.0f, f(10), f(-2) },
				out.getActivations(), 0.001f);
	}

	@Test
	public void testUpdateDelta() {
		out.setWeights(new float[][] { { 0.0f, 1.0f, 2.0f, 3.0f },
				{ 1.0f, -1.0f, 0.0f, 0.0f } });
		in.setActivations(3, 2, 1);
		out.updateActivations();
		out.updateDelta(0, 1);
		assertEquals(f(10) * (1 - f(10)) * (1 - f(10)), out.getDeltas()[0],
				.001);
	}

	@Test
	public void testUpdateWeight() {
		float[][] originalWeights = new float[][] { { 0.0f, 1.0f, 2.0f, 3.0f },
				{ 1.0f, -1.0f, 0.0f, 0.0f } };
		float[][] weights = new float[][] { { 0.0f, 1.0f, 2.0f, 3.0f },
				{ 1.0f, -1.0f, 0.0f, 0.0f } };
		out.setWeights(weights);
		in.setActivations(3, 2, 1);
		out.updateActivations();
		out.updateDelta(0, 1);
		out.updateDelta(1, 1);
		out.updateWeights();
		for (int i = 0; i < out.getWeights().length; i++) {
			for (int j = 0; j < out.getWeights()[i].length; j++) {
				assertEquals(originalWeights[i][j]
						+ ComputationLayer.LEARNING_RATE
						* in.getActivations()[j] * out.getDeltas()[i],
						out.getWeights()[i][j], .001);
			}
		}
	}
}
