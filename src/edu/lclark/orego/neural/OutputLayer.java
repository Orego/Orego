package edu.lclark.orego.neural;

public class OutputLayer extends Layer {

	private float[][] weights;
	
	private float[] netInputs;
	
	private Layer previous;
	
	public OutputLayer(int size, Layer previous) {
		super(size);
		netInputs = new float[size];
		this.previous = previous;
	}

	void updateActivations() {
		updateNetInputs();
		final float[] activations = getActivations();
		for(int i = 0; i < activations.length - 1; i++){
			activations[i + 1] = squash(netInputs[i]);
		}
	}
	
	private float squash(float x) {
		return (float) (1 / (1 + Math.exp(-x)));
	}

	void setWeights(float[][] weights) {
		this.weights = weights;
	}

	void updateNetInputs() {
		final float[] previousActivations = previous.getActivations();
		for (int i = 0; i < netInputs.length; i++) {
			netInputs[i] = 0.0f;
			for (int j = 0; j < previous.size() + 1; j++) {
				netInputs[i] += previousActivations[j] * weights[i][j];
			}
		}
	}

	float[] getNetInputs() {
		return netInputs;
	}
}
