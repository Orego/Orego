package edu.lclark.orego.neural;

/** An output layer in a neural network. */
class OutputLayer extends ComputationLayer {

	/** @see ComputationLayer#ComputationLayer(int, Layer) */
	OutputLayer(int size, Layer previous) {
		super(size, previous);
	}

	/** Updates the activations for the goodth and badth units, ignoring others. */
	public void updateActivations(int good, int bad) {
		updateActivation(good);
		updateActivation(bad);
	}

	/**
	 * Updates the delta for the indexth unit in this layer, given the correct
	 * activation.
	 */
	void updateDelta(int index, float correct) {
		getDeltas()[index] = squashDerivative(getActivations()[index + 1])
				* (correct - getActivations()[index + 1]);
	}

	/**
	 * Updates the deltas for this layer, given an array of correct activations.
	 */
	void updateDeltas(float[] correct) {
		for (int i = 0; i < correct.length; i++) {
			updateDelta(i, correct[i]);
		}
	}

	/**
	 * Updates the deltas for two units in this layer, assuming the unit with
	 * index good should have activation 1 and the unit with index bad should
	 * have activation 0.
	 */
	void updateDeltas(int good, int bad) {
		updateDelta(good, 1);
		updateDelta(bad, 0);
	}

	/**
	 * Updates the weights for two units in this layer.
	 * 
	 * @see #updateDeltas(int, int)
	 */
	void updateWeights(int good, int bad) {
		float[][] weights = getWeights();
		Layer previous = getPrevious();
		float[] deltas = getDeltas();
		for (int j = 0; j < weights[good].length; j++) {
			weights[good][j] += LEARNING_RATE * previous.getActivations()[j]
					* deltas[good];
			weights[bad][j] += LEARNING_RATE * previous.getActivations()[j]
					* deltas[bad];
		}
	}

}
