package edu.lclark.orego.feature;

/** True if both of the features provided to the constructor are true. */
@SuppressWarnings("serial")
public final class Conjunction implements Predicate {

	private final Predicate a;

	private final Predicate b;

	public Conjunction(Predicate a, Predicate b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean at(short p) {
		return a.at(p) && b.at(p);
	}

}
