package edu.lclark.orego.neural;

public class OutputLayer extends Layer {
	
	final static private float LEARNING_RATE = .01f;

	private float[][] weights;
	
	private float[] netInputs;
	
	private Layer previous;

	private float[] delta;
	
	public float[] getDelta(){
		return delta;
	}
	
	public float[][] getWeights(){
		return weights;
	}
	
	public OutputLayer(int size, Layer previous) {
		super(size);
		netInputs = new float[size];
		this.previous = previous;
		delta = new float[size];
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
	
	void updateDelta(int index, float correct){
			delta[index] = squashDeritive(getActivations()[index]) * (correct - getActivations()[index]);
	}

	private float squashDeritive(float function) {
		return function * (1 - function);
	}
	
	void updateWeights(){
		for(int i = 0; i < delta.length; i++){
			for(int j = 0; j < weights.length; j++){
				weights[i][j] += LEARNING_RATE * getActivations()[j] * delta[i];
			}
		}
	}
}
