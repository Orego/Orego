package edu.lclark.orego.neural;

public class Network {
	
	private SigmoidNeuron[] hiddenNeurons;

	private InputNeuron[] inputs;

	private SigmoidNeuron output;

	/**
	 * Constructs a network that can be used for and, or, etc. (but not xor). It
	 * has two input nodes, 1 bias, and no hidden units.
	 */
	public Network() {
		inputs = new InputNeuron[3];
		inputs[1] = new InputNeuron();
		inputs[2] = new InputNeuron();
		inputs[0] = new InputNeuron();
		inputs[0].setActivation(1);
		output = new SigmoidNeuron(inputs);
		hiddenNeurons = null;
	}

	/**
	 * Constructs Network with a specified number of inputs including the bias
	 * input and a specified number of hiddenNeurons. For now, it's layer-size 2
	 * and has one output In the inputs array, the 0th element is the bias
	 * neuron.
	 */
	public Network(int inputSize, int hiddenNeuronSize) {
		inputs = new InputNeuron[inputSize];
		for (int i = 0; i < inputSize; i++) {
			inputs[i] = new InputNeuron();
		}
		inputs[0].setActivation(1);
		hiddenNeurons = new SigmoidNeuron[hiddenNeuronSize];
		Neuron[] biasAndHidden = new Neuron[hiddenNeuronSize + 1];
		// TODO get rid of biasAndHidden through some clever trick
		biasAndHidden[0] = inputs[0];
		for (int i = 0; i < hiddenNeuronSize; i++) {
			hiddenNeurons[i] = new SigmoidNeuron(inputs);
			biasAndHidden[i + 1] = hiddenNeurons[i];
		}
		output = new SigmoidNeuron(biasAndHidden);
	}

	public SigmoidNeuron getOutput() {
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
		for (int i = 0; hiddenNeurons != null && i < hiddenNeurons.length; i++) {
			hiddenNeurons[i].updateActivation();
		}
		output.updateActivation();
		return output.getActivation();
	}

	/**
	 * Trains the network to be more likely to associate the specified inputs
	 * with the specified output.
	 */
	public void train(double correct, double... inputs) {
		setInputs(inputs);
		for (int i = 0; hiddenNeurons != null && i < hiddenNeurons.length; i++) {
			hiddenNeurons[i].updateActivation();
		}
		output.updateActivation();
		output.updateDelta(correct);
		for (int i = 0; hiddenNeurons != null && i < hiddenNeurons.length; i++) {
			hiddenNeurons[i].updateDelta(output.getDelta(),
					output.getWeights()[i+1]);
		}
		updateWeights();
	}

	/** Calculates and returns the delta value (double) for output neuron */
	public void updateDeltaOutput(double correct) {
		output.updateDelta(correct);
	}

	/** Updates weights based on previously determined delta from output */
	public void updateWeights() {
		output.updateWeights();
		for (int i = 0; hiddenNeurons != null && i < hiddenNeurons.length; i++) {
			hiddenNeurons[i].updateWeights();
		}
	}


}
