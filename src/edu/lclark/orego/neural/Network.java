package edu.lclark.orego.neural;

public class Network {
	
	private InputLayer in;
	
	private OutputLayer out;
	
	public InputLayer getInput(){
		return in;
	}
	
	Network(int inputNumber, int outputNumber){
		in = new InputLayer(inputNumber);
		out = new OutputLayer(outputNumber, in);
	}

	public void train(int index, float[] training, float[] correct) {
		for(int i = 0; i < correct.length; i++){
			out.updateActivations();
			out.updateDelta(i, correct[i]);
		}
		out.updateWeights();
	}
}
