package edu.lclark.orego.neural;

public class OutputLayer extends ComputationLayer {

	public OutputLayer(int size, Layer previous) {
		super(size, previous);
	}

	void updateDelta(int index, float correct) {
		// TODO Do we really want to call getDeltas each time?
		getDeltas()[index] = squashDerivative(getActivations()[index + 1])
				* (correct - getActivations()[index + 1]);
	}

	public void updateDeltas(float[] correct) {
		for (int i = 0; i < correct.length; i++) {
			updateDelta(i, correct[i]);
		}
	}

	public void updateDeltas(int good, int bad) {
		updateDelta(good, 1);
		updateDelta(bad, 0);
	}

	public void updateWeights(int good, int bad) {
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
