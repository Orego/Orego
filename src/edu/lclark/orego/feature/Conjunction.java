package edu.lclark.orego.feature;

/** True if both of the features provided to the constructor are true. */
public class Conjunction implements Feature {

	private Feature a;
	
	private Feature b;
	
	public Conjunction(Feature a, Feature b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean at(short p) {
		return a.at(p) && b.at(p);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
	}

}
