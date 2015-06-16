package edu.lclark.orego.neural;

/** A layer that performs computation, e.g., a hidden or output layer. */
abstract class ComputationLayer extends Layer {

	/** Learning rate for backpropagation. */
	static final float LEARNING_RATE = 0.1f;

	/** Sigmoid squashing function. */
	static float squash(float x) {
		return (float) (1 / (1 + Math.exp(-x)));
	}

	/**
	 * Derivative of the squashing function, as a function of the output of
	 * squash.
	 */
	static float squashDerivative(float x) {
		return x * (1 - x);
	}

	/** Delta values for backpropagation. */
	private float[] deltas;

	/** Weighted sums of inputs for each unit. */
	private float[] netInputs;

	/** Previous layers. */
	private Layer previous;

	/**
	 * weight[i][j] is the weight into (non-bias) unit i from unit j of the
	 * previous layer.
	 */
	private float[][] weights;

	/**
	 * @param size
	 *            Number of units in this layer, not including a bias unit.
	 * @param previous
	 *            Previous layer.
	 */
	ComputationLayer(int size, Layer previous) {
		super(size);
		netInputs = new float[size];
		this.previous = previous;
		deltas = new float[size];
		weights = new float[size][previous.size() + 1];
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				weights[i][j] = (float) (Math.random() * 0.01 - 0.005);
			}
		}
	}

	/** Returns the deltas of the units in this layer. */
	float[] getDeltas() {
		return deltas;
	}

	/** Returns the net inputs of the units in this layer. */
	float[] getNetInputs() {
		return netInputs;
	}

	/** Returns the previous layer. */
	Layer getPrevious() {
		return previous;
	}

	/** Returns the weight matrix for this layer. */
	float[][] getWeights() {
		return weights;
	}

	/** For testing only. Sets the weights into this layer. */
	void setWeights(float[][] weights) {
		this.weights = weights;
	}

	/** Updates the activation of the indexth unit. */
	void updateActivation(int index) {
		updateNetInput(index);
		getActivations()[index + 1] = squash(getNetInputs()[index]);
	}

	/** Updates the activations of the units in this layer. */
	void updateActivations() {
		updateNetInputs();
		final float[] activations = getActivations();
		for (int i = 0; i < activations.length - 1; i++) {
			activations[i + 1] = squash(netInputs[i]);
		}
	}

	/** Updates the net input of the indexth unit. */
	void updateNetInput(int index) {
		netInputs[index] = 0.0f;
		for (int j = 0; j < previous.size() + 1; j++) {
			netInputs[index] += previous.getActivations()[j]
					* weights[index][j];
		}
	}

	/** Updates the net inputs of the units in this layer. */
	void updateNetInputs() {
		final float[] previousActivations = previous.getActivations();
		for (int i = 0; i < netInputs.length; i++) {
			netInputs[i] = 0.0f;
			for (int j = 0; j < previous.size() + 1; j++) {
				netInputs[i] += previousActivations[j] * weights[i][j];
			}
		}
	}

	/**
	 * Updates the weights in this layer, assuming deltas have already been set.
	 */
	void updateWeights() {
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				weights[i][j] += LEARNING_RATE * previous.getActivations()[j]
						* deltas[i];
			}
		}
	}

}
