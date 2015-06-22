package edu.lclark.orego.neural;

@SuppressWarnings("serial")
class HiddenLayer extends ComputationLayer {

	/**
	 * @see ComputationLayer#ComputationLayer(int, Layer)
	 */
	HiddenLayer(int size, Layer previous) {
		super(size, previous);
	}

	/** Updates the deltas for this layer, given the next layer. */
	void updateDeltas(ComputationLayer next) {
		final float[] deltas = getDeltas();
		final float[] nextDeltas = next.getDeltas();
		final float[][] nextWeights = next.getWeights();
		for (int j = 0; j < deltas.length; j++) {
			deltas[j] = 0.0f;
			for (int i = 0; i < nextDeltas.length; i++) {
				deltas[j] += nextDeltas[i] * nextWeights[i][j + 1];
			}
			deltas[j] *= squashDerivative(getActivations()[j + 1]);
		}
	}

	/**
	 * Updates the deltas for this layer, given the next layer, paying attention
	 * to only the goodth and badth units in the next layer.
	 * 
	 * @see Network#train(float[], int, int)
	 */
	void updateDeltas(OutputLayer next, int good, int bad) {
		final float[] deltas = getDeltas();
		final float[] nextDeltas = next.getDeltas();
		final float[][] nextWeights = next.getWeights();
		for (int j = 0; j < deltas.length; j++) {
			deltas[j] = 0.0f;
			deltas[j] += nextDeltas[good] * nextWeights[good][j + 1];
			deltas[j] += nextDeltas[bad] * nextWeights[bad][j + 1];
			deltas[j] *= squashDerivative(getActivations()[j + 1]);
		}
	}

}
