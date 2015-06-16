package edu.lclark.orego.neural;

public class OutputLayer extends ComputationLayer {

	public OutputLayer(int size, Layer previous) {
		super(size, previous);
	}

	void updateDelta(int index, float correct){
		// TODO Do we really want to call getDeltas each time?
		getDeltas()[index] = squashDerivative(getActivations()[index + 1]) * (correct - getActivations()[index + 1]);
	}
	
	public void updateDeltas(float[] correct) {
		for (int i = 0; i < correct.length; i++) {
			updateDelta(i, correct[i]);
		}
	}

}
