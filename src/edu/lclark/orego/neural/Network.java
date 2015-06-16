package edu.lclark.orego.neural;

public class Network {

	private InputLayer in;

	private HiddenLayer hid;

	private OutputLayer out;

	public InputLayer getInput() {
		return in;
	}

	Network(int inputNumber, int outputNumber) {
		in = new InputLayer(inputNumber);
		out = new OutputLayer(outputNumber, in);
	}

	public Network(int input, int hidden, int output) {
		in = new InputLayer(input);
		hid = new HiddenLayer(hidden, in);
		out = new OutputLayer(output, hid);
	}

	public void test(float[][] training, float[][] correct) {
		for (int i = 0; i < correct.length; i++) {
			in.setActivations(training[i]);
			if (hid != null) {
				hid.updateActivations();
			}
			out.updateActivations();
			for (int j = 0; j < correct[i].length; j++) {
				System.out.printf("%1.3f, should be %1.3f\n",
						out.getActivations()[j + 1], correct[i][j]);
			}
			System.out.println();
		}
		System.out.println(java.util.Arrays.toString(out.getDeltas()));
		System.out.println(java.util.Arrays.toString(out.getActivations()));
		System.out.println(java.util.Arrays.toString(in.getActivations()));
		System.out.println();
	}

	public void train(float[][] training, float[][] correct, int epochs) {
		for (int i = 0; i < epochs; i++) {
			train(training, correct);
		}
		test(training, correct);
	}

	public void train(float[][] training, float[][] correct) {
		for (int i = 0; i < correct.length; i++) {
			train(training[i], correct[i]);
		}
	}

	public void train(float[] training, float[] correct) {
		update(training);
		out.updateDeltas(correct);
		if (hid != null) {
			hid.updateDeltas(out);
		}
		out.updateWeights();
		if (hid != null) {
			hid.updateWeights();
		}
	}

	public void update(float[] training) {
		in.setActivations(training);
		if (hid != null) {
			hid.updateActivations();
		}
		out.updateActivations();
	}

	public float[] getOutputActivations() {
		return out.getActivations();
	}

}
