package edu.lclark.orego.neural;

import java.io.Serializable;

/** A feedforward neural network, trained by backpropagation. */
@SuppressWarnings("serial")
public class Network implements Serializable {

	/** Sequence of hidden layers. */
	private HiddenLayer[] hid;

	/** Input layer. */
	private Layer in;

	/** Output layer. */
	private OutputLayer out;

	/**
	 * @param layerSizes
	 *            Sequence of layer sizes, e.g., 2, 4, 3, 1 for a network with 2
	 *            input units, 4 units in the first hidden layer, 3 in the
	 *            second, and 1 output unit. There must be at least one hidden
	 *            layer.
	 */
	public Network(int... layerSizes) {
		assert layerSizes.length >= 3;
		in = new Layer(layerSizes[0]);
		hid = new HiddenLayer[layerSizes.length - 2];
		hid[0] = new HiddenLayer(layerSizes[1], in);
		for (int i = 1; i < hid.length; i++) {
			hid[i] = new HiddenLayer(layerSizes[i + 1], hid[i - 1]);
		}
		out = new OutputLayer(layerSizes[layerSizes.length - 1],
				hid[hid.length - 1]);
	}

	/**
	 * Returns the activations of the output units, including a bias unit at
	 * index 0.
	 */
	public float[] getOutputActivations() {
		return out.getActivations();
	}

	/** Prints the output for each training case. */
	@SuppressWarnings("boxing")
	void test(float[][] training) {
		for (int i = 0; i < training.length; i++) {
			System.out.println(java.util.Arrays.toString(training[i]));
			update(training[i]);
			System.out.println(java.util.Arrays.toString(out.getActivations()));
		}
		System.out.println();
	}

	/**
	 * Trains the network on one training vector with one vector of correct
	 * outputs.
	 */
	public void train(float[] training, float[] correct) {
		update(training);
		out.updateDeltas(correct);
		hid[hid.length - 1].updateDeltas(out);
		for (int i = hid.length - 2; i >= 0; i--) {
			hid[i].updateDeltas(hid[i + 1]);
		}
		out.updateWeights();
		for (int i = 0; i < hid.length; i++) {
			hid[i].updateWeights();
		}
	}

	/**
	 * Trains the network on a given training vector, where the goodth output
	 * should be 1 and the badth should be 0.
	 */
	public void train(float[] training, int good, int bad) {
		update(training, good, bad);
		out.updateDeltas(good, bad);
		hid[hid.length - 1].updateDeltas(out, good, bad);
		for (int i = hid.length - 2; i >= 0; i--) {
			hid[i].updateDeltas(hid[i + 1]);
		}
		out.updateWeights(good, bad);
		for (int i = 0; i < hid.length; i++) {
			hid[i].updateWeights();
		}
	}

	/** Updates this network, but only the goodth and badth output units. */
	private void update(float[] training, int good, int bad) {
		in.setActivations(training);
		for (int i = 0; i < hid.length; i++) {
			hid[i].updateActivations();
		}
		out.updateActivations(good, bad);
	}

	/**
	 * Trains the network to associate input vectors with correct output
	 * vectors.
	 */
	public void train(float[][] input, float[][] correct) {
		for (int i = 0; i < correct.length; i++) {
			train(input[i], correct[i]);
		}
	}

	/**
	 * Trains the network, for the specified number of epochs, to associates
	 * input vectors with correct output vectors.
	 */
	public void train(float[][] input, float[][] correct, int epochs) {
		for (int i = 0; i < epochs; i++) {
			train(input, correct);
		}
	}

	/**
	 * Update the activations for all units in this network, given the specified
	 * input vector.
	 */
	public void update(float[] input) {
		in.setActivations(input);
		for (int i = 0; i < hid.length; i++) {
			hid[i].updateActivations();
		}
		out.updateActivations();
	}

}
