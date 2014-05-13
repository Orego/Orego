package edu.lclark.orego.core;

import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static java.util.Arrays.*;

public class BoardImplementation {

	/** Colors of points on the board. */
	private final Color[] colors;
	
	/** The color to play next. */
	private StoneColor colorToPlay;
	
	/** Coordinate system based on board width. */
	private final CoordinateSystem coordinateSystem;
	
	public BoardImplementation(int width) {
		// TODO We may later move most of this work to a clear method, as in the old version
		coordinateSystem = CoordinateSystem.forWidth(width);
		colors = new Color[coordinateSystem.getFirstPointBeyondBoard()];
		fill(colors, OFF_BOARD);
		for (int p : coordinateSystem.getAllPointsOnBoard()) {
			colors[p] = VACANT;
		}
		colorToPlay = BLACK;
	}

	/** @see edu.lclark.orego.core.CoordinateSystem#at(String) */
	public short at(String label) {
		return coordinateSystem.at(label);
	}

	/** Returns the color at point p. */
	public Color getColorAt(short p) {
		return colors[p];
	}

	/**
	 * Plays a move at point p if possible. Has no side effect if the move is
	 * illegal. Returns the legality of that move.
	 */
	public Legality play(short p) {
		colors[p] = colorToPlay;
		colorToPlay = colorToPlay.opposite();
		// TODO This currently considers any move legal!
		return OK;
	}

}
