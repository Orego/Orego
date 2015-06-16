package edu.lclark.orego.neural;

public class Network {

	private InputLayer in;

	private HiddenLayer[] hid;

	private OutputLayer out;

	public InputLayer getInput() {
		return in;
	}

	public Network(int... layerSizes) {
		in = new InputLayer(layerSizes[0]);
		hid = new HiddenLayer[layerSizes.length - 2];
		if (hid.length > 0) {
			hid[0] = new HiddenLayer(layerSizes[1], in);
		}
		for (int i = 1; i < hid.length; i++) {
			hid[i] = new HiddenLayer(layerSizes[i + 1], hid[i - 1]);
		}
		if (hid.length > 0) {
			out = new OutputLayer(layerSizes[layerSizes.length - 1], hid[hid.length - 1]);
		} else {
			out = new OutputLayer(layerSizes[layerSizes.length - 1], in);
		}
	}

	public void test(float[][] training, float[][] correct) {
		for (int i = 0; i < correct.length; i++) {
			update(training[i]);
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
		if (hid.length > 0) {
			hid[hid.length - 1].updateDeltas(out);
		}
		for (int i = hid.length - 2; i >= 0; i--) {
			hid[i].updateDeltas(hid[i + 1]);
		}
		out.updateWeights();
		for (int i = 0; i < hid.length; i++) {
			hid[i].updateWeights();
		}
	}

	public void update(float[] training) {
		in.setActivations(training);
		for (int i = 0; i < hid.length; i++) {
			hid[i].updateActivations();
		}
		out.updateActivations();
	}

	public float[] getOutputActivations() {
		return out.getActivations();
	}

}
