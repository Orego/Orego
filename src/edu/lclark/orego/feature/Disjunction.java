package edu.lclark.orego.feature;

/** True if at least one of the features provided to the constructor is true. */
@SuppressWarnings("serial")
public final class Disjunction implements Predicate {

	private final Predicate a;

	private final Predicate b;

	public Disjunction(Predicate a, Predicate b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean at(short p) {
		return a.at(p) || b.at(p);
	}

}
