package edu.lclark.orego.neural;

/**Input Neuron*/
public class InputNeuron implements Neuron{
	
	private double activation; 
	
	@Override
	public double getActivation() {
		return activation;
	}

	@Override
	public void setActivation(double act) {
		activation = act;
	}

}
