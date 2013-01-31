package orego.mcts;

public class RaveCovPlayer extends RavePlayer{
	
	private double raveBias;

	public RaveCovPlayer() {
		raveBias = 0.0009;
	}
	
	@Override
	protected double raveCoefficient(double c, double rc) {
		return (rc - c) / (rc - c + rc * c * raveBias);
	}

}
