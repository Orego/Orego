package edu.lclark.orego.neural;

public class Network {

	private SigmoidNeuron[][] hiddenNeurons;

	private InputNeuron[] inputs;

	private SigmoidNeuron[] output;

	/**
	 * Constructs Network with a specified number of inputs excluding the bias
	 * input and a specified number of hiddenNeurons. Hidden unit-layer size is specified
	 * and it has one output In the inputs array, the 0th element is the bias
	 * neuron.
	 */
	public Network(int inputSize, int hiddenNeuronSize, int hiddenLayers) {
		inputs = new InputNeuron[inputSize + 1];
		for (int i = 0; i < inputSize + 1; i++) {
			inputs[i] = new InputNeuron();
		}
		inputs[0].setActivation(1);
		hiddenNeurons = new SigmoidNeuron[hiddenLayers][hiddenNeuronSize];
		Neuron[] biasAndHidden = new Neuron[hiddenNeuronSize + 1];
		for (int j = 0; j < hiddenLayers; j++){
			biasAndHidden[0] = inputs[0];
			for (int i = 0; i < hiddenNeuronSize; i++) {
				hiddenNeurons[j][i] = new SigmoidNeuron(inputs);
				biasAndHidden[i + 1] = hiddenNeurons[j][i];
			}
		}
		output = new SigmoidNeuron[1];
		output[0] = new SigmoidNeuron(biasAndHidden);
	}
	
	/**
	 * Constructs Network with a specified number of inputs excluding the bias
	 * input and a specified number of hiddenNeurons. Hidden unit-layer size is specified
	 * and it has a specified number of outputs. In the inputs array, the 0th element is the bias
	 * neuron.
	 */
	public Network(int inputSize, int hiddenNeuronSize, int hiddenLayers, int outputs) {
		inputs = new InputNeuron[inputSize + 1];
		for (int i = 0; i < inputSize + 1; i++) {
			inputs[i] = new InputNeuron();
		}
		inputs[0].setActivation(1);
		hiddenNeurons = new SigmoidNeuron[hiddenLayers][hiddenNeuronSize];
		Neuron[] biasAndHidden = new Neuron[hiddenNeuronSize + 1];
		for (int j = 0; j < hiddenLayers; j++){
			biasAndHidden[0] = inputs[0];
			for (int i = 0; i < hiddenNeuronSize; i++) {
				hiddenNeurons[j][i] = new SigmoidNeuron(inputs);
				biasAndHidden[i + 1] = hiddenNeurons[j][i];
			}
		}
		output = new SigmoidNeuron[outputs];
		for (int i = 0; i < outputs; i++){
			output[i] = new SigmoidNeuron(biasAndHidden);
		}
		
	}

	public SigmoidNeuron[] getOutput() {
		return output;
	}

	/** Sets the activations of the network's input units. */
	public void setInputs(double... input) {
		for (int i = 1; i < inputs.length; i++) {
			inputs[i].setActivation(input[i - 1]);
		}
	}

	/** Returns the network's output when inputs are fed in. */
	public double test(double... inputs) {
		setInputs(inputs);
		for (int j = 0; hiddenNeurons != null && j < hiddenNeurons.length; j++ ){
			for (int i = 0; hiddenNeurons != null && i < hiddenNeurons[j].length; i++) {
				hiddenNeurons[j][i].updateActivation();
			}
		}
		for (int i = 0; i < output.length; i++){
			output[i].updateActivation();
		}
		return output[0].getActivation();
		// TODO: make array
	}

	/**
	 * Trains the network to be more likely to associate the specified inputs
	 * with the specified output.
	 */
	public void train(double correct, double... inputs) {
		//TODO: Train network on one output at a time. 
		setInputs(inputs);
		for (int j = 0; hiddenNeurons != null && j < hiddenNeurons.length; j++ ){
			for (int i = 0; hiddenNeurons != null && i < hiddenNeurons[j].length; i++) {
				hiddenNeurons[j][i].updateActivation();
			}
		}
		output[0].updateActivation();
		output[0].updateDelta(correct);
		//TODO: Take output array into consideration
		for (int j = 0; hiddenNeurons != null && j < hiddenNeurons.length; j++ ){
			for (int i = 0; hiddenNeurons != null && i < hiddenNeurons[j].length; i++) {
				hiddenNeurons[j][i].updateDelta(output[0].getDelta(),
						output[0].getWeights()[i + 1]);
			}
			//TODO: FIX! D:
		}
		updateWeights();
	}

	/** Calculates and returns the delta value (double) for output neuron */
	public void updateDeltaOutput(double correct) {
		output[0].updateDelta(correct);
		//TODO: fix output
	}

	/** Updates weights based on previously determined delta from output */
	public void updateWeights() {
		output[0].updateWeights();
		//TODO: fix output
		for (int j = 0; hiddenNeurons != null && j < hiddenNeurons.length; j++ ){
			for (int i = 0; hiddenNeurons != null && i < hiddenNeurons[j].length; i++) {
				hiddenNeurons[j][i].updateWeights();
			}
		}
	}

}
