package edu.lclark.orego.neural;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class NetworkTest {

	private Network net;
	
	@Before
	public void setUp() throws Exception {
		net = new Network(2, 2);	
	}
	
	@Test
	public void testTrain() {
		float[][] training = new float[][] {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
		float[][] correct = new float[][] {{0, 0}, {0, 1}, {0, 1}, {1, 1}};
		net.train(training, correct, 10000);
		for (int i = 0; i < correct.length; i++) {
			net.update(training[i]);
			for (int j = 0; j < correct[i].length; j++) {
				assertEquals(correct[i][j], net.getOutputActivations()[j + 1], 0.1f);
			}
		}
	}

}
