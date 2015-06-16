package edu.lclark.orego.neural;

public abstract class ComputationLayer extends Layer {
	
	final static private float LEARNING_RATE = 0.1f;

	private float[][] weights;
	
	private float[] netInputs;
	
	private Layer previous;

	private float[] deltas;
	
	public float[] getDeltas(){
		return deltas;
	}
	
	public float[][] getWeights(){
		return weights;
	}
	
	public ComputationLayer(int size, Layer previous) {
		super(size);
		netInputs = new float[size];
		this.previous = previous;
		deltas = new float[size];
		weights = new float[size][previous.size() + 1];
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				weights[i][j] = (float)(Math.random() * 0.01 - 0.005);
			}
		}
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
	
	float squashDerivative(float x) {
		return x * (1 - x);
	}
	
	void updateWeights(){
		for(int i = 0; i < weights.length; i++){
			for(int j = 0; j < weights[i].length; j++){
				weights[i][j] += LEARNING_RATE * previous.getActivations()[j] * deltas[i];
			}
		}
	}

}
