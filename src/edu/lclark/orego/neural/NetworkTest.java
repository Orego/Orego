package edu.lclark.orego.neural;

import static org.junit.Assert.*;

import org.junit.Test;

public class NetworkTest {

	private Network net;
	
	private void testAfterTraining(float[][] training, float[][] correct) {
		for (int i = 0; i < correct.length; i++) {
			net.update(training[i]);
			for (int j = 0; j < correct[i].length; j++) {
				assertEquals(correct[i][j], net.getOutputActivations()[j + 1], 0.1f);
			}
		}
	}
	
	@Test
	public void testAndAndOr() {
		net = new Network(2, 2);	
		float[][] training = new float[][] {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
		float[][] correct = new float[][] {{0, 0}, {0, 1}, {0, 1}, {1, 1}};
		net.train(training, correct, 100000);
		testAfterTraining(training, correct);
	}

	@Test
	public void testAndOrAndXor() {
		net = new Network(2, 2, 3);	
		float[][] training = new float[][] {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
		float[][] correct = new float[][] {{0, 0, 0}, {0, 1, 1}, {0, 1, 1}, {1, 1, 0}};
		net.train(training, correct, 1000000);
		testAfterTraining(training, correct);
	}

}
