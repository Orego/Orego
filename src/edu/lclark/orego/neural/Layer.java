package edu.lclark.orego.neural;

import java.io.Serializable;

/**
 * A layer in a neural network. This class can be used for an input layer;
 * computational layers use subclasses.
 */
@SuppressWarnings("serial")
class Layer implements Serializable {

	/**
	 * Activations of the units, including a bias unit at index 0 with
	 * activation 1.
	 */
	private final float[] activations;

	/**
	 * @param size
	 *            Number of units in this layer, not including the bias unit.
	 */
	Layer(int size) {
		activations = new float[size + 1];
		activations[0] = 1;
	}

	/**
	 * Returns the activations of the units in this layer, including the bias
	 * unit at index 0.
	 */
	float[] getActivations() {
		return activations;
	}

	/**
	 * Sets the activations of the units in this layer. The bias node is not
	 * included in the argument.
	 */
	void setActivations(float... activations) {
		for (int i = 0; i < activations.length; i++) {
			this.activations[i + 1] = activations[i];
		}
	}

	/** Returns the number of units in this layer (not including any bias unit). */
	int size() {
		return activations.length - 1;
	}

}
