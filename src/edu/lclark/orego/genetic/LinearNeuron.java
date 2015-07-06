package edu.lclark.orego.genetic;

import edu.lclark.orego.core.CoordinateSystem;

public class LinearNeuron {

	private int threshold;
	
	private byte[][] weights;
	
	public LinearNeuron(int threshold, byte[][] weights) {
		
	}
	
	public int activityAt(short p, long[] featureMap, CoordinateSystem coords) {
		return -1;
	}

}
