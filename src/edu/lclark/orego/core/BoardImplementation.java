package edu.lclark.orego.core;

import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import orego.util.BitVector;
import edu.lclark.orego.util.ShortSet;
import edu.lclark.orego.util.ShortList;

// TODO Should this implement the same interface as CoordinateSystem and provide delegate methods?
// Alternately, should we rip out the delegates and ask others to call getCoordinateSystem?
public final class BoardImplementation {

	/**
	 * Used (for different purposes) by hashAfterRemovingCapturedStones() and
	 * isSelfAtari().
	 */
	private final BitVector adjacentChains;

	/** The color to play next. */
	private StoneColor colorToPlay;

	/** Coordinate system based on board width. */
	private final CoordinateSystem coords;

	/**
	 * Identifiers of enemy chains adjacent to the move just played. Used by
	 * isSuicidal() and isSelfAtari().
	 */
	private final ShortList enemyNeighboringChainIds;

	/** Identifiers of friendly chains adjacent to the move just played. */
	private final ShortList friendlyNeighboringChainIds;

	/**
	 * Zobrist hash of the current board position.
	 * 
	 * @see #getHash()
	 */
	private long hash;
	
	/** The point, if any, where the simple ko rule prohibits play. */
	private short koPoint;

	/** Liberties of the stone just played. */
	private final ShortSet lastPlayLiberties;

	/** Neighbors of a stone just captured. Used by removeStone(). */
	private final ShortList neighborsOfCapturedStone;

	/** Number of consecutive passes just played. */
	private short passes;
	
	/** Point on the board (and surrounding sentinels). */
	private final Point[] points;

	/**
	 * A hash table of all previous board positions for ko verification. The
	 * hash codes stored here do NOT include the simple ko point.
	 */
	private final SuperKoTable superKoTable;

	/** @see #getTurn() */
	private short turn;
	
	/** The set of vacant points. */
	private final ShortSet vacantPoints;

	public BoardImplementation(int width) {
		coords = CoordinateSystem.forWidth(width);
		points = new Point[coords.getFirstPointBeyondExtendedBoard()];
		friendlyNeighboringChainIds = new ShortList(4);
		enemyNeighboringChainIds = new ShortList(4);
		int n = coords.getFirstPointBeyondBoard();
		adjacentChains = new BitVector(n);
		lastPlayLiberties = new ShortSet(n);
		superKoTable = new SuperKoTable();
		vacantPoints = new ShortSet(n);
		for (short p = 0; p < points.length; p++) {
			points[p] = new Point(coords, p);
		}
		neighborsOfCapturedStone = new ShortList(4);
		clear();
	}

	/**
	 * Deals with enemy chains adjacent to the move just played at p, either
	 * capturing them or decrementing their liberty counts.
	 * 
	 * @param color
	 *            The color of the stone just played.
	 */
	private void adjustEnemyNeighbors(StoneColor color, short p) {
//		// TODO Should the caller find the opposite color?
//		StoneColor enemyColor = color.opposite();
		for (int i = 0; i < enemyNeighboringChainIds.size(); i++) {
			short enemy = enemyNeighboringChainIds.get(i);
			if (points[enemy].liberties.size() == 1) {
				// chainsInAtari[enemyColor].remove(enemy);
				short s = enemy;
				do {
					removeStone(s);
					s = points[s].chainNextPoint;
				} while (s != enemy);
			} else {
				points[enemy].liberties.removeKnownPresent(p);
				// if (points[enemy].liberties.size() == 1) {
				// chainsInAtari[enemyColor].addKnownAbsent(enemy);
				// }
			}
		}
	}

	/**
	 * Deals with friendly neighbors of the move p just played, merging chains
	 * as necessary.
	 */
	private void adjustFriendlyNeighbors(StoneColor color, short p) {
		if (friendlyNeighboringChainIds.size() == 0) {
			// If there are no friendly neighbors, create a new, one-stone chain
			points[p].becomeOneStoneChain(lastPlayLiberties);
			// if (points[p].liberties.size() == 1) {
			// chainsInAtari[color].addKnownAbsent(p);
			// }
		} else if (friendlyNeighboringChainIds.size() == 1) {
			// If there is only one friendly neighbor, add this stone to that
			// chain
			short c = friendlyNeighboringChainIds.get(0);
			points[c].liberties.addAll(lastPlayLiberties);
			points[c].liberties.removeKnownPresent(p);
			points[p].addToChain(points[c]);
			// if (points[c].liberties.size() == 1) {
			// chainsInAtari[color].add(c);
			// } else {
			// chainsInAtari[color].remove(c);
			// }
		} else {
			// If there are several friendly neighbors, merge them
			short c = friendlyNeighboringChainIds.get(0);
			points[p].addToChain(points[c]);
			for (int i = 1; i < friendlyNeighboringChainIds.size(); i++) {
				short ally = friendlyNeighboringChainIds.get(i);
				if (points[ally].liberties.size() <= points[c].liberties.size()) {
					mergeChains(c, ally);
				} else {
					mergeChains(ally, c);
					c = ally;
				}
			}
			points[c].liberties.addAll(lastPlayLiberties);
			points[c].liberties.removeKnownPresent(p);
			// if (points[c].liberties.size() == 1) {
			// chainsInAtari[color].add(c);
			// } else {
			// chainsInAtari[color].remove(c);
			// }
		}
	}

	/**
	 * @see edu.lclark.orego.core.CoordinateSystem#at(int, int)
	 */
	public short at(int r, int c) {
		return coords.at(r, c);
	}

	/** @see edu.lclark.orego.core.CoordinateSystem#at(String) */
	public short at(String label) {
		return coords.at(label);
	}

	/**
	 * Returns this board to its blank state. Any initial stones are removed and
	 * the komi is reset to a default value. This is roughly equivalent to
	 * creating a new instance, but (a) it is faster, and (b) references to the
	 * board do not have to change.
	 */
	public void clear() {
		colorToPlay = BLACK;
		hash = SuperKoTable.EMPTY;
		koPoint = NO_POINT;
		passes = 0;
		superKoTable.clear();
		turn = 0;
		vacantPoints.clear();
		for (short p : coords.getAllPointsOnBoard()) {
			points[p].clear();
			vacantPoints.addKnownAbsent(p);
		}
	}

	/**
	 * Updates data structures at the end of a play.
	 * 
	 * @param color
	 *            The color of the stone played.
	 * @param p
	 *            The location where the stone was played.
	 */
	private void finalizePlay(StoneColor color, short p) {
		int lastVacantPointCount = vacantPoints.size();
		points[p].color = color;
		vacantPoints.remove(p);
		// TODO Update stone counts, maybe neighbor counts
		hash ^= coords.getHash(color, p);
		boolean surrounded = hasMaxNeighborsForColor(color.opposite(), p);
		adjustFriendlyNeighbors(color, p);
		adjustEnemyNeighbors(color, p);
		// if (liberties[points[p].chainId].size() == 1) {
		// chainsInAtari[color].add(points[p].chainId);
		// }
		if ((lastVacantPointCount == vacantPoints.size()) & surrounded) {
			koPoint = vacantPoints.get((short) (vacantPoints.size() - 1));
		} else {
			koPoint = NO_POINT;
		}
	}

	/**
	 * @see edu.lclark.orego.core.CoordinateSystem#getAllPointsOnBoard()
	 */
	public short[] getAllPointsOnBoard() {
		return coords.getAllPointsOnBoard();
	}

	/**
	 * @see edu.lclark.orego.core.CoordinateSystem#getArea()
	 */
	public int getArea() {
		return coords.getArea();
	}

	/** Returns the color at point p. */
	public Color getColorAt(short p) {
		return points[p].color;
	}

	/** Returns the color to play next. */
	public StoneColor getColorToPlay() {
		return colorToPlay;
	}

	/**
	 * Returns the Zobrist hash of the current board position.
	 */
	public long getHash() {
		return hash;
	}

	/**
	 * Returns the liberties of p.
	 */
	public ShortSet getLiberties(short p) {
		return points[points[p].chainId].liberties;
	}
	
	/**
	 * @see edu.lclark.orego.core.CoordinateSystem#getMaxMovesPerGame()
	 */
	public short getMaxMovesPerGame() {
		return coords.getMaxMovesPerGame();
	}

	/**
	 * Returns an array of the neighbors of p.
	 * 
	 * @see CoordinateSystem#getNeighbors(short)
	 */
	public short[] getNeighbors(short p) {
		return coords.getNeighbors(p);
	}

	/**
	 * Returns the number of consecutive passes ending the move sequence so far.
	 */
	public int getPasses() {
		return passes;
	}

	/**
	 * Returns the current turn number (0 at the beginning of the game).
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * Returns the set of vacant points on this board.
	 */
	public ShortSet getVacantPoints() {
		return vacantPoints;
	}

	/**
	 * @return
	 * @see edu.lclark.orego.core.CoordinateSystem#getWidth()
	 */
	public int getWidth() {
		return coords.getWidth();
	}

	/**
	 * Returns the hash value that would result if the captured stones were
	 * removed. Used by play() to detect superko.
	 * 
	 * @param color Color of the stone to be played.
	 * @param p Location of the stone to be played.
	 */
	private long hashAfterRemovingCapturedStones(StoneColor color, short p) {
		long result = hash;
		result ^= coords.getHash(color, p);
		adjacentChains.clear(); // Chains to be captured
		StoneColor enemy = color.opposite();
		short[] neighbors = coords.getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
			if (points[n].color == enemy) {
				short c = points[n].chainId;
				if (points[c].isInAtari() & !adjacentChains.get(c)) {
					adjacentChains.set(c, true);
					short active = c;
					do {
						result ^= coords.getHash(enemy, active);
						active = points[active].chainNextPoint;
					} while (active != c);
				}
			}
		}
		return result;
	}

	/**
	 * Returns true if the stone at p has the maximum possible number of
	 * neighbors of color p.
	 */
	private boolean hasMaxNeighborsForColor(StoneColor color, short p) {
		short[] neighbors = getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			Color c = points[neighbors[i]].color;
			if (c != color && c != OFF_BOARD) {
				return false;
			}
		}
		return true;
	}

//	/**
//	 * @see edu.lclark.orego.core.CoordinateSystem#isEdgeOrCorner(short)
//	 */
//	public boolean isEdgeOrCorner(short p) {
//		return coords.isEdgeOrCorner(p);
//	}

	/**
	 * Visits neighbors of p, looking for potential captures and chains to merge
	 * with the new stone. As a side effect, loads the fields
	 * friendlyNeighboringChainIds, enemyNeighboringChainIds, and
	 * lastPlayLiberties.
	 * 
	 * @return true if playing at p would be suicidal.
	 */
	private boolean isSuicidal(StoneColor color, short p) {
		friendlyNeighboringChainIds.clear();
		enemyNeighboringChainIds.clear();
		lastPlayLiberties.clear();
		boolean suicide = true;
		short[] neighbors = getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
			Color neighborColor = points[n].color;
			if (neighborColor == VACANT) { // Vacant point
				lastPlayLiberties.add(n);
				suicide = false;
			} else if (neighborColor == color) { // Friendly neighbor
				short chainId = points[n].chainId;
				friendlyNeighboringChainIds.addIfNotPresent(chainId);
				suicide &= (points[chainId].isInAtari());
			} else if (neighborColor != OFF_BOARD) { // Enemy neighbor
				short chainId = points[n].chainId;
				enemyNeighboringChainIds.addIfNotPresent(chainId);
				suicide &= !(points[chainId].isInAtari());
			}
		}
		return suicide;
	}

	/** Returns the legality of playing at p. */
	public Legality legality(StoneColor color, short p) {
		// TODO Game too long
		assert coords.isOnBoard(p);
		if (points[p].color != VACANT) {
			return OCCUPIED;
		}
		if (p == koPoint) {
			return KO_VIOLATION;
		}
		if (isSuicidal(color, p)) {
			return SUICIDE;
		}
		long proposed = hashAfterRemovingCapturedStones(color, p);
		if (superKoTable.contains(proposed)) {
			return KO_VIOLATION;
		}
		return OK;
	}

	/**
	 * Merges the stones in appendage into the chain at base. Each parameter is
	 * a stone in one of the chains to be merged.
	 * 
	 * @param base
	 *            If not too expensive to compute, base should be the larger of
	 *            the two chains.
	 */
	private void mergeChains(short base, short appendage) {
		points[base].liberties.addAll(points[appendage].liberties);
		// chainsInAtari[colors[appendage]].remove(appendage);
		int active = appendage;
		do {
			points[active].chainId = points[base].chainId;
			active = points[active].chainNextPoint;
		} while (active != appendage);
		short temp = points[base].chainNextPoint;
		points[base].chainNextPoint = points[appendage].chainNextPoint;
		points[appendage].chainNextPoint = temp;
	}

	/** Plays a pass move. */
	public void pass() {
		if (koPoint != NO_POINT) {
			koPoint = NO_POINT;
		}
		colorToPlay = colorToPlay.opposite();
		passes++;
		// moves[turn] = PASS;
		turn++;
	}

	/** Places a stone of color at point p. */
	private void placeInitialStone(StoneColor color, short p) {
		// Initial stones will always be legal, but the legality method
		// also sets up some fields called by finalizePlay.
		legality(color, p);
		finalizePlay(color, p);
		superKoTable.add(hash);
	}

	/**
	 * Plays a move at point p if possible. Has no side effect if the move is
	 * illegal. Returns the legality of that move.
	 */
	public Legality play(short p) {
		if (p == PASS) {
			pass();
			return OK;
		}
		if (turn >= coords.getMaxMovesPerGame() - 2) {
			return GAME_TOO_LONG;
		}
		Legality result = legality(colorToPlay, p);
		if (result != OK) {
			return result;
		}
		finalizePlay(colorToPlay, p);
		colorToPlay = colorToPlay.opposite();
		passes = 0;
		// moves[turn] = p;
		turn++;
		superKoTable.add(hash);
		return OK;
	}

	/**
	 * @see edu.lclark.orego.core.CoordinateSystem#toString(short)
	 */
	public String toString(short p) {
		return coords.toString(p);
	}

	/** Removes the stone at p. */
	public void removeStone(short p) {
		StoneColor color = (StoneColor) (points[p].color);
		// stoneCounts[color]--;
		hash ^= coords.getHash(color, p);
		points[p].color = VACANT;
		vacantPoints.addKnownAbsent(p);
		neighborsOfCapturedStone.clear();
		short[] neighbors = getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
			// This seems to be a rare appropriate use of instanceof
			if (points[n].color instanceof StoneColor) {
				neighborsOfCapturedStone.addIfNotPresent(points[n].chainId);
			}
		}
//		StoneColor enemyColor = color.opposite();
		for (int k = 0; k < neighborsOfCapturedStone.size(); k++) {
			int c = neighborsOfCapturedStone.get(k);
			points[c].liberties.addKnownAbsent(p);
			// if (points[c].liberties.size() > 1) {
			// chainsInAtari[enemyColor].remove(c);
			// }
		}
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

	@Override
	public String toString() {
		String result = "";
		for (int r = 0; r < getWidth(); r++) {
			for (int c = 0; c < getWidth(); c++) {
				result += points[at(r, c)].color.toChar();
			}
			result += "\n";
		}
		return result;
	}

}
