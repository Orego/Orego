package edu.lclark.orego.feature;

/** True if at least one of the features provided to the constructor is true. */
public final class Disjunction implements Feature {

	private final Feature a;
	
	private final Feature b;
	
	public Disjunction(Feature a, Feature b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean at(short p) {
		return a.at(p) || b.at(p);
	}

}
