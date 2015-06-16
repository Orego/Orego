package edu.lclark.orego.neural;

public class Network {

	private InputLayer in;

	private OutputLayer out;

	public InputLayer getInput() {
		return in;
	}

	Network(int inputNumber, int outputNumber) {
		in = new InputLayer(inputNumber);
		out = new OutputLayer(outputNumber, in);
	}

	public void test(float[][] training, float[][] correct) {
		for (int i = 0; i < correct.length; i++) {
			in.setActivations(training[i]);
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
		out.updateWeights();
	}

	public void update(float[] training) {
		in.setActivations(training);
		out.updateActivations();
	}

	public float[] getOutputActivations() {
		return out.getActivations();
	}

}
