package edu.lclark.orego.neural;

/** Sigmoid Neuron */
public class SigmoidNeuron implements Neuron{
	

	/** Learning rate used in updating weights. */
	public static final double LEARNING_RATE = 1.0;

	public static final double MAX_INITIAL_WEIGHT = 1.0;

	public static final double MIN_INITIAL_WEIGHT = -1.0;

	/**
	 * Returns a random double in the range [MIN_INITIAL_WEIGHT,
	 * MAX_INITIAL_WEIGHT).
	 */
	public static double rand() {
		return MIN_INITIAL_WEIGHT + (MAX_INITIAL_WEIGHT - MIN_INITIAL_WEIGHT)
				* Math.random();
	}

	/** Returns f(x) where f(x) = 1/(1+(e^-x)) */
	private static double sigmoid(double x) {
		return 1 / (1 + Math.exp(-x));
	}

	/** Output of this neuron. */
	private double activation;

	/** Delta value for backpropagation. */
	private double delta;

	/** Neurons providing input to this neuron. */
	private final Neuron[] inputs;

	/** Weights from inputs to this neuron. */
	private double[] weights;

	/** Connects this SigmoidNeuron to the specified inputs with random weights. */
	public SigmoidNeuron(Neuron... inputs) {
		this.inputs = inputs;
		weights = new double[inputs.length];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = rand();
		}
	}
	
	/** Constructor that initializes the values of inputs and weights */
	public SigmoidNeuron(Neuron[] inputs, double[] weights) {
		this.inputs = inputs;
		this.weights = weights;
	}

	@Override
	public double getActivation() {
		return activation;
	}

	/** returns double Delta value for this Neuron */
	public double getDelta() {
		return delta;
	}

	public double[] getWeights(){
		return weights;
	}

	@Override
	public void setActivation(double act) {
		activation = act;
	}

	/** Sets the double array of weights. For testing only. */
	public void setWeights(double w[]) {
		weights = w;
	}
	public String toString(){
		String print = "Activation of Sigmoid Neuron = " + activation + "\n";
		for(int i = 0; i < inputs.length; i++){
			print += inputs[i].toString();
		}
		return print;
	}

	/**
	 * Computes and sets activation from inputs & weights using sigmoid function
	 */
	public void updateActivation() {
		double x = 0;
		for (int i = 0; i < inputs.length; i++) {
			x += (weights[i]) * (inputs[i].getActivation());
		}
		activation = sigmoid(x);
	}

	/** Updates delta value given the correct output for an output neuron. */
	public void updateDelta(double correct) {
		delta = activation * (1 - activation) * (correct - activation);
//		System.out.println("output delta: " + delta);
	}

	/**
	 * Updates delta value given the output's delta and weight from output to
	 * this neuron for a hidden Neuron.
	 */
	public void updateDelta(double outputDelta, double weightToOutput) {
		delta = activation * (1 - activation) * weightToOutput * outputDelta;
//		System.out.println("hidden delta: " + delta);
	}

	/**
	 * Updates the weights into this neuron, assuming delta has already been
	 * set.
	 */
	public void updateWeights() {
		for (int i = 0; i < inputs.length; i++) {
			weights[i] += LEARNING_RATE * (inputs[i].getActivation()) * delta;
		}
	}


}
