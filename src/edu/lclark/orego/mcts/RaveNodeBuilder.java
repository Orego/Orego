package edu.lclark.orego.mcts;

import edu.lclark.orego.core.CoordinateSystem;

public final class RaveNodeBuilder implements SearchNodeBuilder {

	private final CoordinateSystem coords;

	public RaveNodeBuilder(CoordinateSystem coords) {
		this.coords = coords;
	}

	@Override
	public RaveNode build() {
		return new RaveNode(coords);
	}
}
