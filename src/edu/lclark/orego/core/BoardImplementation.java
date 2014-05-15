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
		// TODO We may later move most of this work to a clear method, as in the
		// old version
		coordinateSystem = CoordinateSystem.forWidth(width);
		colors = new Color[coordinateSystem.getFirstPointBeyondBoard()];
		clear();
	}

	/**
	 * Returns this board to its initial, blank state. Any initial stones are
	 * removed and the komi is reset to a default value. This is roughly
	 * equivalent to creating a new instance, but (a) it is faster, and (b)
	 * references to the board do not have to change.
	 */
	public void clear() {
		fill(colors, OFF_BOARD);
		for (int p : coordinateSystem.getAllPointsOnBoard()) {
			colors[p] = VACANT;
		}
		colorToPlay = BLACK;
	}

	/** @see CoordinateSystem#at(String) */
	public short at(String label) {
		return coordinateSystem.at(label);
	}

	/** Returns the color at point p. */
	public Color getColorAt(short p) {
		return colors[p];
	}

	/**
	 * Returns an array of the neighbors of p.
	 * 
	 * @see CoordinateSystem#getNeighbors(short)
	 */
	public short[] getNeighbors(short p) {
		return coordinateSystem.getNeighbors(p);
	}

	/**
	 * Plays a move at point p if possible. Has no side effect if the move is
	 * illegal. Returns the legality of that move.
	 */
	public Legality play(short p) {
		assert coordinateSystem.isOnBoard(p);
		if (colors[p] != VACANT) {
			return OCCUPIED;
		}
		colors[p] = colorToPlay;
		colorToPlay = colorToPlay.opposite();
		// TODO This currently considers any move legal!
		return OK;
	}

	/**
	 * Places all of the stones indicated in diagram. These are set as initial
	 * stones, not moves recorded in the board's history. The color to play next
	 * is set as indicated.
	 */
	public void setUpProblem(String[] diagram, StoneColor colorToPlay) {
		assert diagram.length == getWidth();
		clear();
		for (int r = 0; r < getWidth(); r++) {
			assert diagram[r].length() == getWidth();
			for (int c = 0; c < getWidth(); c++) {
				StoneColor color = StoneColor.forChar(diagram[r].charAt(c));
				if (color != null) {
					placeInitialStone(color, at(r, c));
				}
			}
		}
		this.colorToPlay = colorToPlay;
		// TODO Put this new hash code in the superko table
	}

	/** Places a stone of color at point p. */
	private void placeInitialStone(StoneColor color, short p) {
		colors[p] = color;
		// TODO Update hash code
	}

	@Override
	public String toString() {
		String result = "";
		for (int r = 0; r < getWidth(); r++) {
			for (int c = 0; c < getWidth(); c++) {
				result += colors[at(r, c)].toChar();
			}
			result += "\n";
		}
		return result;
	}

	/**
	 * @return
	 * @see edu.lclark.orego.core.CoordinateSystem#getWidth()
	 */
	public int getWidth() {
		return coordinateSystem.getWidth();
	}

	/**
	 * @see edu.lclark.orego.core.CoordinateSystem#at(int, int)
	 */
	public short at(int r, int c) {
		return coordinateSystem.at(r, c);
	}

}
