package edu.lclark.orego.neural;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class NetworkTest {

	private Network net;
	
	@Before
	public void setUp() throws Exception {
		net = new Network(2, 1);	
	}
	
	@Test
	public void testTrain() {
		float[][] training = new float[][] {{0, 0}, {0, 1}, {1, 0}, {1, 0}};
		float[][] correct = new float[][] {{0}, {1}, {1}, {1}};
		net.train(1, training[1], correct[1]);
	}

}
