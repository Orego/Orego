package edu.lclark.orego.neural;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class OutputLayerTest {

	private InputLayer in;
	
	private OutputLayer out;
	
	@Before
	public void setUp() throws Exception {
		in = new InputLayer(3);
		out = new OutputLayer(2, in);
	}

	@Test
	public void testUpdateActivations() {
		out.setWeights(new float[][] {{0.0f, 1.0f, 2.0f, 3.0f},
				{1.0f, -1.0f, 0.0f, 0.0f}});
		in.setActivations(3, 2, 1);
		out.updateNetInputs();
		assertArrayEquals(new float[] {10, -2}, out.getNetInputs(), 0.001f);
	}

}
