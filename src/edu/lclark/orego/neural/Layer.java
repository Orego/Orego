package edu.lclark.orego.neural;

public abstract class Layer {

	private float[] activations;
	
	public Layer(int size) {
		activations = new float[size + 1];
		activations[0] = 1;
	}

	public void setActivations(float... activations) {
		for (int i = 0; i < activations.length; i++) {
			this.activations[i + 1] = activations[i];
		}
	}
	
	/** Returns the activations of the units in this layer, including the bias unit at index 0. */
	public float[] getActivations() {
		return activations;
	}
	
	/** Returns the number of units in this layer (not including any bias unit). */
	public int size() {
		return activations.length - 1;
	}

}
