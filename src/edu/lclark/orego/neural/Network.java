package edu.lclark.orego.neural;

public class Network {

	private InputLayer in;

	private HiddenLayer[] hid;

	private OutputLayer out;

	public InputLayer getInput() {
		return in;
	}

	public Network(int... layerSizes) {
		assert layerSizes.length >= 3;
		in = new InputLayer(layerSizes[0]);
		hid = new HiddenLayer[layerSizes.length - 2];
		hid[0] = new HiddenLayer(layerSizes[1], in);
		for (int i = 1; i < hid.length; i++) {
			hid[i] = new HiddenLayer(layerSizes[i + 1], hid[i - 1]);
		}
		out = new OutputLayer(layerSizes[layerSizes.length - 1], hid[hid.length - 1]);
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
		update(training[training.length - 1]);
		out.updateDeltas(correct[correct.length - 1]);
		System.out.println(java.util.Arrays.toString(out.getDeltas()));
		System.out.println(java.util.Arrays.toString(out.getActivations()));
		System.out.println(java.util.Arrays.toString(correct[correct.length - 1]));
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
		hid[hid.length - 1].updateDeltas(out);
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

	public void train(float[] training, int good, int bad) {
		update(training); // TODO This is a bit wasteful
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

}
