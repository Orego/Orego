package edu.lclark.orego.neural;

public class HiddenLayer extends ComputationLayer {

	public HiddenLayer(int size, Layer previous) {
		super(size, previous);
	}

	void updateDelta(int index, float correct){
		// TODO Do we really want to call getDeltas each time?
		getDeltas()[index] = squashDerivative(getActivations()[index + 1]) * (correct - getActivations()[index + 1]);
	}
	
	public void updateDeltas(ComputationLayer next) {
		float[] deltas = getDeltas();
		float[] nextDeltas = next.getDeltas();
		float[][] nextWeights = next.getWeights();
		for (int j = 0; j < deltas.length; j++) {
			deltas[j] = 0.0f;
			for (int i = 0; i < nextDeltas.length; i++) {
				deltas[j] += nextDeltas[i] * nextWeights[i][j + 1];
			}
			deltas[j] *= squashDerivative(getActivations()[j + 1]); 
		}
	}

}
