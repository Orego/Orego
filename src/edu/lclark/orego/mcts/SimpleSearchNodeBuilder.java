package edu.lclark.orego.mcts;

import edu.lclark.orego.core.CoordinateSystem;

public final class SimpleSearchNodeBuilder implements SearchNodeBuilder {

	private final CoordinateSystem coords;

	public SimpleSearchNodeBuilder(CoordinateSystem coords) {
		this.coords = coords;
	}

	@Override
	public SimpleSearchNode build() {
		return new SimpleSearchNode(coords);
	}

}
