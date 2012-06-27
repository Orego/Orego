package orego.core;

import static java.lang.String.format;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static orego.core.NeighborCounts.*;
import java.util.*;
import orego.util.*;
import ec.util.MersenneTwisterFast;

/** Holds the current state of the board, allows moves to be played, etc. */
public class Board {

	/**
	 * Maximum number of moves per game. It should be extremely rare to actually
	 * play this many moves, but a playout (which doesn't check superko) might
	 * run this long. It's faster to just cut off such unusual runs (by forcing
	 * passes) than to check for superko in playouts.
	 */
	public static final int MAX_MOVES_PER_GAME = BOARD_AREA * 3;

	/**
	 * The result returned by play() when the playout has run too long; only
	 * passes are legal near the end, to ensure that the playout ends by
	 * MAX_MOVES_PER_GAME.
	 */
	public static final int PLAY_GAME_TOO_LONG = 4;

	/**
	 * The result returned by play() when the move violates ko.
	 */
	public static final int PLAY_KO_VIOLATION = 3;

	/**
	 * The result returned by play() when the point is occupied.
	 */
	public static final int PLAY_OCCUPIED = 2;

	/**
	 * The result returned by play() when the move is legal.
	 */
	public static final int PLAY_OK = 0;

	/**
	 * The result returned by play() when the move is suicide.
	 */
	public static final int PLAY_SUICIDE = 1;

	/**
	 * Maps to the bits which need updating in the ith neighbors' pattern. (If
	 * point n is my ith neighbor, I am the opposite neighbor, and each color
	 * takes up two bits.)
	 */
	public static final int[] UPDATE_NBR_MAP = new int[] { 6, 4, 2, 0, 14, 12,
			10, 8 };

	/**
	 * Random numbers for Zobrist hashes, indexed by color and point. The last
	 * row is for the simple ko point.
	 */
	public static final long[][] ZOBRIST_HASHES = new long[3][FIRST_POINT_BEYOND_BOARD];

	static { // Initialize ZOBRIST_HASHES
		MersenneTwisterFast random = new MersenneTwisterFast(0L);
		for (int color = 0; color < ZOBRIST_HASHES.length; color++) {
			for (int p : ALL_POINTS_ON_BOARD) {
				ZOBRIST_HASHES[color][p] = random.nextLong();
			}
		}
		// Set the element below to zero, so that xoring in the ko point when
		// there isn't one has no effect.
		ZOBRIST_HASHES[VACANT][NO_POINT] = 0L;
	}

	/**
	 * Used (for different purposes) by hashAfterRemovingCapturedStones() and
	 * isSelfAtari().
	 */
	private BitVector adjacentChains;

	/**
	 * Id of chain for each occupied point.
	 */
	private int[] chainIds;

	/**
	 * Next "pointers" for each occupied point, linking points into chains.
	 */
	private int[] chainNextPoints;

	/** Sets of chains that are in atari, one for each color. */
	private IntSet[] chainsInAtari;

	/**
	 * Color of each point.
	 * 
	 * @see orego.core.Colors
	 */
	private int[] colors;

	/** The color to play next, BLACK or WHITE. */
	private int colorToPlay;

	/** Used by isEyelike(). */
	private int[] diagonalColorCount;

	/**
	 * Ids of enemy chains adjacent to the move just played. Used by
	 * isSuicidal() and isSelfAtari().
	 */
	private IntList enemyNeighboringChainIds;

	/** Ids of friendly chains adjacent to the move just played. */
	private IntList friendlyNeighboringChainIds;
	
	/** Keeps track of the number of handicap stones at the start of the game */
	private int handicap;

	/**
	 * Zobrist hash of the current board position, including the simple ko
	 * point.
	 * 
	 * @see #getHash()
	 */
	private long hash;

	/** Komi, stored in a form that speeds score counting. */
	private int komi;

	/** The point, if any, where the simple ko rule prohibits play. */
	private int koPoint;

	/** Liberties of the stone just played. */
	private IntSet lastPlayLiberties;

	/** The liberties of each chain. */
	private IntSet[] liberties;

	/** moves[t] is the move played at turn t. */
	private int[] moves;

	/**
	 * Neighbor counts of each point.
	 * 
	 * @see orego.core.NeighborCounts
	 */
	private int[] neighborCounts;

	/**
	 * The 3x3 neighborhood of each point. Undefined for occupied points.
	 * 
	 * @see orego.patterns.Patterns
	 */
	private char[] neighborhoods;

	/** Neighbors of a stone just captured. Used by removeStone(). */
	private IntList neighborsOfCapturedStone;

	/** Number of consecutive passes. */
	private int passes;

	/** Used by isSelfAtari() */
	private IntSet selfAtariLiberties;

	/** Numbers of stones of each color on the board. */
	private int stoneCounts[];

	/**
	 * A hash table of all previous board positions for ko verification. The
	 * hash codes stored here do NOT include the simple ko point.
	 */
	private SuperKoTable superKoTable;

	/**
	 * The number of moves played so far. At the beginning of the game, turn is
	 * 0.
	 */
	private int turn;

	/** The set of vacant points. */
	private IntSet vacantPoints;

	public Board() {
		clear();
	}

	/** Adds stone p to chain c. */
	protected void addStone(int p, int c) {
		chainNextPoints[p] = chainNextPoints[c];
		chainNextPoints[c] = p;
		chainIds[p] = c;
	}

	/**
	 * Deals with enemy chains adjacent to the move just played at p, either
	 * capturing them or decrementing their liberty counts.
	 */
	protected void adjustEnemyNeighbors(int p) {
		int enemyColor = opposite(colorToPlay);
		for (int i = 0; i < enemyNeighboringChainIds.size(); i++) {
			int enemy = enemyNeighboringChainIds.get(i);
			if (liberties[enemy].size() == 1) {
				chainsInAtari[enemyColor].remove(enemy);
				int s = enemy;
				do {
					removeStone(s);
					s = chainNextPoints[s];
				} while (s != enemy);
			} else {
				liberties[enemy].removeKnownPresent(p);
				if (liberties[enemy].size() == 1) {
					chainsInAtari[enemyColor].addKnownAbsent(enemy);
				}
			}
		}
	}

	/**
	 * Deals with friendly neighbors of the move p just played, merging chains
	 * as necessary.
	 */
	protected void adjustFriendlyNeighbors(int p) {
		if (friendlyNeighboringChainIds.size() == 0) {
			// If there are no friendly neighbors, create a new, one-stone chain
			chainNextPoints[p] = p;
			liberties[p].copyDataFrom(lastPlayLiberties);
			if (liberties[p].size() == 1) {
				chainsInAtari[colorToPlay].addKnownAbsent(p);
			}
		} else if (friendlyNeighboringChainIds.size() == 1) {
			// If there is only one friendly neighbor, add this stone to that
			// chain
			int c = friendlyNeighboringChainIds.get(0);
			liberties[c].union(lastPlayLiberties);
			liberties[c].removeKnownPresent(p);
			addStone(p, c);
			if (liberties[c].size() == 1) {
				chainsInAtari[colorToPlay].add(c);
			} else {
				chainsInAtari[colorToPlay].remove(c);
			}
		} else {
			// If there are several friendly neighbors, merge them
			int c = friendlyNeighboringChainIds.get(0);
			addStone(p, c);
			for (int i = 1; i < friendlyNeighboringChainIds.size(); i++) {
				int ally = friendlyNeighboringChainIds.get(i);
				if (liberties[ally].size() <= liberties[c].size()) {
					mergeChains(c, ally);
				} else {
					mergeChains(ally, c);
					c = ally;
				}
			}
			liberties[c].union(lastPlayLiberties);
			assert liberties[c].contains(p);
			liberties[c].removeKnownPresent(p);
			if (liberties[c].size() == 1) {
				chainsInAtari[colorToPlay].add(c);
			} else {
				chainsInAtari[colorToPlay].remove(c);
			}
		}
	}

	/**
	 * Returns the score, including komi and counting only stones (not
	 * territory). Positive scores are good for black.
	 */
	public int approximateScore() {
		return stoneCounts[BLACK] - stoneCounts[WHITE] + komi;
	}

	/**
	 * Returns the winner, including komi and counting only stones (not
	 * territory).
	 */
	public int approximateWinner() {
		if (approximateScore() <= 0) {
			return WHITE;
		}
		return BLACK;
	}

	/**
	 * Clears this Board, initializing all data structures.
	 */
	public void clear() {
		setKomi(7.5);
		turn = 0;
		passes = 0;
		colorToPlay = BLACK;
		handicap = 0;
		hash = 0L;
		friendlyNeighboringChainIds = new IntList(4);
		enemyNeighboringChainIds = new IntList(4);
		lastPlayLiberties = new IntSet(FIRST_POINT_BEYOND_BOARD);
		neighborsOfCapturedStone = new IntList(4);
		selfAtariLiberties = new IntSet(FIRST_POINT_BEYOND_BOARD);
		moves = new int[MAX_MOVES_PER_GAME];
		colors = new int[EXTENDED_BOARD_AREA];
		neighborhoods = new char[EXTENDED_BOARD_AREA];
		vacantPoints = new IntSet(FIRST_POINT_BEYOND_BOARD);
		neighborCounts = new int[EXTENDED_BOARD_AREA];
		koPoint = NO_POINT;
		chainsInAtari = new IntSet[] { new IntSet(FIRST_POINT_BEYOND_BOARD),
				new IntSet(FIRST_POINT_BEYOND_BOARD) };
		chainIds = new int[EXTENDED_BOARD_AREA];
		stoneCounts = new int[NUMBER_OF_PLAYER_COLORS];
		chainNextPoints = new int[FIRST_POINT_BEYOND_BOARD];
		liberties = new IntSet[FIRST_POINT_BEYOND_BOARD];
		adjacentChains = new BitVector(FIRST_POINT_BEYOND_BOARD);
		superKoTable = new SuperKoTable();
		diagonalColorCount = new int[NUMBER_OF_COLORS];
		for (int p = 0; p < EXTENDED_BOARD_AREA; p++) {
			neighborCounts[p] = initialNeighborCounts();
			chainIds[p] = p;
			colors[p] = OFF_BOARD_COLOR;
			if (ON_BOARD[p]) {
				liberties[p] = new IntSet(FIRST_POINT_BEYOND_BOARD);
				colors[p] = VACANT;
				vacantPoints.addKnownAbsent(p);
				int edgeCount = 0;
				for (int i = 0; i < 4; i++) {
					int n = NEIGHBORS[p][i];
					if (!ON_BOARD[n]) {
						edgeCount++;
					}
				}
				neighborCounts[p] += edgeCount * EDGE_INCREMENT;
			}
		}
		// We need everything set up before we can compute the patterns.
		for (int p : ALL_POINTS_ON_BOARD) {
			computeNeighborhood(p);
		}
	}

	/**
	 * Recomputes the neighborhood around p and stores it.
	 * 
	 * @see #updateNeighborhoods(int)
	 */
	protected void computeNeighborhood(int p) {
		assert ON_BOARD[p];
		neighborhoods[p] = 0;
		for (int i = 0; i < 8; i++) {
			neighborhoods[p] = (char) ((neighborhoods[p] >>> 2) | (colors[NEIGHBORS[p][i]] << 14));
		}
	}

	/**
	 * Copies all data from that into this. Similar to cloning that, but without
	 * the overhead of creating a new object.
	 */
	public void copyDataFrom(Board that) {
		superKoTable.copyDataFrom(that.superKoTable);
		System.arraycopy(that.colors, 0, colors, 0, EXTENDED_BOARD_AREA);
		System.arraycopy(that.neighborhoods, 0, neighborhoods, 0,
				EXTENDED_BOARD_AREA);
		System.arraycopy(that.neighborCounts, 0, neighborCounts, 0,
				EXTENDED_BOARD_AREA);
		System.arraycopy(that.chainNextPoints, 0, chainNextPoints, 0,
				FIRST_POINT_BEYOND_BOARD);
		for (int p : ALL_POINTS_ON_BOARD) {
			liberties[p].copyDataFrom(that.liberties[p]);
		}
		System.arraycopy(that.chainIds, 0, chainIds, 0, EXTENDED_BOARD_AREA);
		System.arraycopy(that.moves, 0, moves, 0, that.turn);
		chainsInAtari[BLACK].copyDataFrom(that.chainsInAtari[BLACK]);
		chainsInAtari[WHITE].copyDataFrom(that.chainsInAtari[WHITE]);
		vacantPoints.copyDataFrom(that.vacantPoints);
		stoneCounts[BLACK] = that.stoneCounts[BLACK];
		stoneCounts[WHITE] = that.stoneCounts[WHITE];
		komi = that.komi;
		koPoint = that.koPoint;
		handicap = that.handicap;
		hash = that.hash;
		passes = that.passes;
		colorToPlay = that.colorToPlay;
		turn = that.turn;
	}

	@Override
	/**
	 * To be equals(), two Boards must have the stones in the same places, the
	 * same liberty counts, etc. Chain ids and chain next points may not match.
	 * 
	 * Warning -- this is expensive! Also, does not verify that super ko tables
	 * are identical.
	 */
	public boolean equals(Object thatObject) {
		if (this == thatObject) {
			return true;
		}
		if (thatObject == null) {
			return false;
		}
		if (getClass() != thatObject.getClass()) {
			return false;
		}
		Board that = (Board) thatObject;
		if (!Arrays.equals(colors, that.colors)) {
			return false;
		}
		for (int p : ALL_POINTS_ON_BOARD) {
			if (colors[p] != VACANT) {
				if (!liberties[chainIds[p]]
						.equals(that.liberties[that.chainIds[p]])) {
					return false;
				}
			}
		}
		if (vacantPoints.size() != that.vacantPoints.size()) {
			return false;
		}
		if (!Arrays.equals(stoneCounts, that.stoneCounts)) {
			return false;
		}
		if (koPoint != that.koPoint) {
			return false;
		}
		if (hash != that.hash) {
			return false;
		}
		if (colorToPlay != that.colorToPlay) {
			return false;
		}
		if (turn != that.turn) {
			return false;
		}
		if (passes != that.passes) {
			return false;
		}
		return true;
	}

	/**
	 * Fills the array moves with the moves this board has recorded from turn
	 * start to turn end, inclusive.
	 */
	public void fillMoves(int[] moves, int start, int end) {
		assert 0 <= start && start <= end && end < getTurn() : format(
				"Tried to fill moves from %d to %d", start, end);
		System.arraycopy(this.moves, start, moves, 0, end - start + 1);
	}

	/**
	 * Updates data structures at the end of a play. Used by play() and
	 * playFast().
	 */
	protected void finalizePlay(int p) {
		int lastVacantPointCount = vacantPoints.size();
		placeStone(colorToPlay, p);
		boolean surrounded = hasMaxNeighborsForColor(neighborCounts[p],
				opposite(colorToPlay));
		adjustFriendlyNeighbors(p);
		adjustEnemyNeighbors(p);
		if (liberties[chainIds[p]].size() == 1) {
			chainsInAtari[colorToPlay].add(chainIds[p]);
		}
		updateNeighborhoods(p);
		hash ^= ZOBRIST_HASHES[VACANT][koPoint];
		if ((lastVacantPointCount == vacantPoints.size()) & surrounded) {
			koPoint = vacantPoints.get(vacantPoints.size() - 1);
		} else {
			koPoint = NO_POINT;
		}
		hash ^= ZOBRIST_HASHES[VACANT][koPoint];
		colorToPlay = opposite(colorToPlay);
		passes = 0;
		moves[turn] = p;
		turn++;
	}

	/**
	 * Similar to playoutScore(), but can handle territories larger than one
	 * point. Assumes all stones on board are alive.
	 */
	public int finalScore() {
		boolean[] visited = new boolean[EXTENDED_BOARD_AREA];
		int territoryScore = 0;
		for (int i = 0; i < vacantPoints.size(); i++) {
			int p = vacantPoints.get(i);
			if (!visited[p]) {
				Set<Integer> block = new HashSet<Integer>();
				boolean[] hasNeighbor = new boolean[NUMBER_OF_PLAYER_COLORS];
				findTerritory(p, visited, hasNeighbor, block);
				if (hasNeighbor[BLACK]) {
					if (!hasNeighbor[WHITE]) { // Black territory
						territoryScore += block.size();
					}
				} else if (hasNeighbor[WHITE]) { // White territory
					territoryScore -= block.size();
				}
			}
		}
		return approximateScore() + territoryScore;
	}

	/**
	 * Similar to winner(), but based on finalScore() instead of playoutScore().
	 */
	public int finalWinner() {
		return (finalScore() <= 0) ? WHITE : BLACK;
	}

	/**
	 * Updates block to contain all unvisited points in the same block of
	 * territory as p. Also updates visited to mark these points and updates
	 * hasNeighbor to indicate which colors border on this block of territory.
	 * Used by finalScore().
	 */
	protected void findTerritory(int p, boolean[] visited,
			boolean[] hasNeighbor, Set<Integer> block) {
		visited[p] = true;
		block.add(p);
		for (int i = 0; i < 4; i++) {
			int n = NEIGHBORS[p][i];
			if (ON_BOARD[n]) {
				if (colors[n] == VACANT) { // Vacant neighbor
					if (!visited[n]) {
						findTerritory(n, visited, hasNeighbor, block);
					}
				} else { // Occupied neighbor
					hasNeighbor[colors[n]] = true;
				}
			}
		}
	}

	/**
	 * Returns the move required to capture the stone at p, or NO_POINT if p is
	 * not in atari.
	 */
	public int getCapturePoint(int p) {
		assert colors[p] < VACANT;
		if (getLibertyCount(p) > 1) {
			return NO_POINT;
		} else {
			return getLibertyOfChainInAtari(chainIds[p]);
		}
	}

	/** Returns the chain id of point p. */
	public int getChainId(int p) {
		return chainIds[p];
	}

	/** Returns the array chainNextPoints(). */
	public int[] getChainNextPoints() {
		return chainNextPoints;
	}

	/** Returns the chains of color that are in atari. */
	public IntSet getChainsInAtari(int color) {
		return chainsInAtari[color];
	}

	/** Returns the number of stones in the chain including p. */
	public int getChainSize(int p) {
		assert colors[p] != VACANT;
		int result = 0;
		int q = p;
		do {
			result++;
			q = chainNextPoints[q];
		} while (q != p);
		return result;
	}

	/** Returns the color of point p. */
	public int getColor(int p) {
		return colors[p];
	}

	/** Returns the color (BLACK or WHITE) to play next. */
	public int getColorToPlay() {
		return colorToPlay;
	}

	/** Returns the number of handicap stones used at the beginning of the game. */
	public int getHandicap() {
		return handicap;
	}
	
	/**
	 * Returns the zobrist hash of the current board position, including the
	 * simple ko point and color to play.
	 */
	public long getHash() {
		if (colorToPlay == WHITE) {
			return ~hash;
		}
		return hash;
	}

	/** Returns the komi. */
	public double getKomi() {
		return -komi + 0.5;
	}

	/**
	 * Returns the simple ko point, or NO_POINT if there is none.
	 */
	public int getKoPoint() {
		return koPoint;
	}

	/** Returns the liberties of p. */
	public IntSet getLiberties(int p) {
		assert isAPlayerColor(colors[p]);
		return liberties[chainIds[p]];
	}

	/**
	 * NOTE: Outside of testing, getLiberties() is much faster. This method
	 * exists only to test that one.
	 * 
	 * Fills a list with the liberties of the chain containing point p. If
	 * liberties has too much room, size is set appropriately. If it does not
	 * have enough room, the method returns after filling the list.
	 */
	protected void getLibertiesByTraversal(int p, IntList liberties) {
		assert isAPlayerColor(colors[p]);
		liberties.clear();
		int stone = p;
		do {
			if (getVacantNeighborCount(stone) > 0) {
				for (int i = 0; i < 4; i++) {
					int neighbor = NEIGHBORS[stone][i];
					if (getColor(neighbor) == VACANT) {
						liberties.addIfNotPresent(neighbor);
					}
					if (liberties.size() == liberties.capacity()) {
						return;
					}
				}
			}
			stone = chainNextPoints[stone];
		} while (stone != p);
	}

	/** Returns the number of liberties of the chain including point p. */
	public int getLibertyCount(int p) {
		assert isAPlayerColor(colors[p]);
		return liberties[chainIds[p]].size();
	}

	/**
	 * Returns the liberty of the chain with the specified id, assuming it is in
	 * atari.
	 */
	public int getLibertyOfChainInAtari(int chainId) {
		assert liberties[chainId].size() == 1;
		return liberties[chainId].get(0);
	}

	/**
	 * Returns the move played on turn t. If t is negative, returns NO_POINT.
	 */
	public int getMove(int t) {
		if (t < 0) {
			return NO_POINT;
		}
		return moves[t];
	}

	/**
	 * Returns the array of moves played on this board. Note that only elements
	 * in [0, t) are relevant.
	 */
	public int[] getMoves() {
		return moves;
	}

	/**
	 * Returns a String containing a human-readable list of all moves made so
	 * far on this board.
	 */
	public String getMoveSequence() {
		String result = "";
		for (int t = 0; t < turn; t++) {
			result += pointToString(moves[t]) + " ";
		}
		return result;
	}

	/**
	 * Returns the array of neighbor counts.
	 * 
	 * @see orego.core.NeighborCounts
	 */
	protected int[] getNeighborCounts() {
		return neighborCounts;
	}

	/**
	 * Gets the local 3x3 neighborhood around point p.
	 * 
	 * @see orego.patterns
	 */
	public final char getNeighborhood(int p) {
		assert ON_BOARD[p];
		assert colors[p] == VACANT;
		return neighborhoods[p];
	}

	/**
	 * Returns the number of consecutive passes ending the move sequence so far.
	 */
	public int getPasses() {
		return passes;
	}

	/**
	 * Returns the array stoneCounts, indicating the number of stones of each
	 * color on the board.
	 */
	protected int[] getStoneCounts() {
		return stoneCounts;
	}
	
	/** Returns the current turn number (e.g., 0 at the beginning of the game). */
	public int getTurn() {
		return turn;
	}

	/** Returns the number of vacant neighbors of point p. */
	public int getVacantNeighborCount(int p) {
		return extractNeighborCount(neighborCounts[p], VACANT);
	}

	/** Returns a list of vacant points. */
	public IntSet getVacantPoints() {
		return vacantPoints;
	}

	/**
	 * Returns the hash value that would result if the captured stones were
	 * removed. Used by play() to detect superko.
	 */
	public long hashAfterRemovingCapturedStones(int p) {
		long result = hash;
		result ^= ZOBRIST_HASHES[colorToPlay][p];
		int stonesCaptured = 0;
		adjacentChains.clear(); // Chains to be captured
		int enemy = opposite(colorToPlay);
		for (int i = 0; i < 4; i++) {
			if (colors[NEIGHBORS[p][i]] == enemy) {
				int c = chainIds[NEIGHBORS[p][i]];
				if ((liberties[c].size() == 1) & !adjacentChains.get(c)) {
					adjacentChains.set(c, true);
					int active = c;
					do {
						stonesCaptured++;
						result ^= ZOBRIST_HASHES[enemy][active];
						active = chainNextPoints[active];
					} while (active != c);
				}
			}
		}
		// Xor out the old simple ko point
		result ^= ZOBRIST_HASHES[VACANT][this.koPoint];
		// The new simple ko point is NOT xored back in, because the
		// superko table hashes should not include this information
		if (colorToPlay == BLACK) {
			return ~result;
		}
		return result;
	}

	/**
	 * Returns true if p is "like" an eye for colorToPlay, that is, is
	 * surrounded by friendly stones and having no more than one (zero at the
	 * board edge) diagonally adjacent enemy stones. It is almost always a bad
	 * idea to play in such a point.
	 */
	public boolean isEyelike(int p) {
		// One might imagine that, since we're maintaining patterns anyway, it
		// would be quicker to simply look this up. It has been tried, and
		// actually hurts slightly, even using a clever bit vector to store the
		// lookup table.
		assert colors[p] == VACANT : pointToString(p) + " is not vacant:\n"
				+ toString() + vacantPoints.toStringAsPoints();
		if (!hasMaxNeighborsForColor(neighborCounts[p], colorToPlay)) {
			return false;
		}
		for (int c = 0; c <= OFF_BOARD_COLOR; c++) {
			diagonalColorCount[c] = 0;
		}
		for (int i = 4; i < 8; i++) {
			int n = NEIGHBORS[p][i];
			diagonalColorCount[colors[n]]++;
		}
		return diagonalColorCount[opposite(colorToPlay)] < EYELIKE_THRESHOLD[p];
	}

	/**
	 * Returns true if p is a feasible choice to play. The point must not be
	 * played on an eyelike point, and must either be on the 3rd or 4th row from
	 * the edge or be within a knight's move of another stone.
	 */
	public boolean isFeasible(int p) {
		return !isEyelike(p)
				&& (Coordinates.THIRD_OR_FOURTH_LINE[p] || isWithinALargeKnightsMoveOfAnotherStone(p));
	}

	/** Returns true if chain is in atari. */
	public boolean isInAtari(int chain) {
		assert chainIds[chain] == chain;
		return liberties[chain].size() == 1;
	}

	/**
	 * Returns true if p is a legal move for color, without playing it.
	 */
	public boolean isLegal(int p) {
		// Passing is always legal
		if (p == PASS) {
			return true;
		}
		// Runaway playouts are cut off by making non-passes illegal
		if (turn >= MAX_MOVES_PER_GAME - 2) {
			return false;
		}
		assert ON_BOARD[p] : "Move not on board: " + p + "(" + pointToString(p)
				+ ")";
		// Check for occupied point
		if (colors[p] != VACANT) {
			return false;
		}
		// Check for simple ko violation
		if (p == koPoint) {
			return false;
		}
		// Process neighbors, checking for suicide
		if (isSuicidal(p)) {
			return false;
		}
		// Check for superko violation
		long proposed = hashAfterRemovingCapturedStones(p);
		if (superKoTable.contains(proposed) || superKoTable.contains(~proposed)) {
			return false;
		}
		// Hooray, it's legal!
		return true;
	}

	/** Returns true if the play at move p would be a self-atari for color. */
	public boolean isSelfAtari(int p, int color) {
		assert colors[p] == VACANT;
		selfAtariLiberties.clear();
		// We set p so we don't count it as a liberty.
		selfAtariLiberties.addKnownAbsent(p);
		enemyNeighboringChainIds.clear();
		adjacentChains.clear(); // Allies
		final int enemyColor = opposite(color);
		for (int i = 0; i < 4; i++) {
			int n = NEIGHBORS[p][i];
			int c = colors[n];
			int chain = chainIds[n];
			if (c == VACANT) {
				selfAtariLiberties.add(n);
			} else if (c == enemyColor && isInAtari(chain)) {
				selfAtariLiberties.add(n);
				enemyNeighboringChainIds.add(chain);
			} else if (c == color) {
				selfAtariLiberties.union(liberties[chain]);
				adjacentChains.set(chain, true);
			}
			if (selfAtariLiberties.size() >= 3) {
				return false;
			}
		}
		// We didn't avoid self-atari directly, but maybe a capture bought us a
		// distant liberty.
		// Check for that.
		for (int i = 0; i < enemyNeighboringChainIds.size(); i++) {
			int chain = enemyNeighboringChainIds.get(i);
			int stone = chain;
			do {
				for (int j = 0; j < 4; j++) {
					int neighbor = NEIGHBORS[stone][j];
					if (adjacentChains.get(chainIds[neighbor])) {
						// neighbor is in a chain p will join, so stone will be
						// a liberty
						selfAtariLiberties.add(stone);
						if (selfAtariLiberties.size() >= 3) {
							return false;
						}
					}
				}
				stone = chainNextPoints[stone];
			} while (stone != chain);
		}
		return true;
	}

	/**
	 * Visits neighbors of p, looking for potential captures and chains to merge
	 * with the new stone. As a side effect, loads the fields
	 * friendlyNeighboringChainIds, enemyNeighboringChainIds, and liberties.
	 * 
	 * @return true if playing at p would be suicidal.
	 */
	protected boolean isSuicidal(int p) {
		friendlyNeighboringChainIds.clear();
		enemyNeighboringChainIds.clear();
		lastPlayLiberties.clear();
		boolean suicide = true;
		for (int i = 0; i < 4; i++) {
			int n = NEIGHBORS[p][i];
			int neighborColor = colors[n];
			if (neighborColor == VACANT) { // Vacant point
				lastPlayLiberties.add(n);
				suicide = false;
			} else if (neighborColor == colorToPlay) { // Friendly neighbor
				int chainId = chainIds[n];
				friendlyNeighboringChainIds.addIfNotPresent(chainId);
				suicide &= (liberties[chainId].size() == 1);
			} else if (neighborColor != OFF_BOARD_COLOR) { // Enemy neighbor
				int chainId = chainIds[n];
				enemyNeighboringChainIds.addIfNotPresent(chainId);
				suicide &= !(liberties[chainId].size() == 1);
			}
		}
		return suicide;
	}

	/**
	 * Returns whether a point is within a knight's move or less of another
	 * stone.
	 */
	public boolean isWithinAKnightsMoveOfAnotherStone(int p) {
		int validPoints[] = Coordinates.KNIGHT_NEIGHBORHOOD[p];
		for (int i = 0; i < validPoints.length; i++) {
			if (colors[validPoints[i]] != VACANT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether a point is within a large knight's move or less of
	 * another stone.
	 */
	public boolean isWithinALargeKnightsMoveOfAnotherStone(int p) {
		int validPoints[] = Coordinates.LARGE_KNIGHT_NEIGHBORHOOD[p];
		for (int i = 0; i < validPoints.length; i++) {
			if (colors[validPoints[i]] != VACANT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the color of p if it is occupied, or the color of the player with
	 * more stones adjacent otherwise. Returns VACANT if adjacent color counts
	 * are equal.
	 */
	public int localOwner(int p) {
		if (colors[p] == VACANT) {
			int nc = neighborCounts[p];
			int difference = extractNeighborCount(nc, BLACK)
					- extractNeighborCount(nc, WHITE);
			if (difference > 0) {
				return BLACK;
			} else if (difference < 0) {
				return WHITE;
			} else {
				return VACANT;
			}
		}
		return colors[p];
	}

	/**
	 * Merges the stones in appendage into the chain at base. Each parameter is
	 * a stone in one of the chains to be merged.
	 * 
	 * @param base
	 *            if not too expensive to compute, should be the larger of the
	 *            two chains.
	 */
	protected void mergeChains(int base, int appendage) {
		liberties[base].union(liberties[appendage]);
		chainsInAtari[colors[appendage]].remove(appendage);
		int active = appendage;
		do {
			chainIds[active] = chainIds[base];
			active = chainNextPoints[active];
		} while (active != appendage);
		int temp = chainNextPoints[base];
		chainNextPoints[base] = chainNextPoints[appendage];
		chainNextPoints[appendage] = temp;
	}

	/** Plays a pass move. */
	public void pass() {
		if (koPoint != NO_POINT) {
			hash ^= ZOBRIST_HASHES[VACANT][koPoint];
			koPoint = NO_POINT;
		}
		colorToPlay = opposite(colorToPlay);
		// hash = ~hash;
		passes++;
		moves[turn] = PASS;
		turn++;
	}
	
	/** Places a stone at point p. */
	protected void placeStone(int color, int p) {
		stoneCounts[color]++;
		colors[p] = color;
		hash ^= ZOBRIST_HASHES[colors[p]][p];
		vacantPoints.remove(p);
		for (int i = 0; i < 4; i++) {
			int n = NEIGHBORS[p][i];
			neighborCounts[n] += NEIGHBOR_INCREMENT[color];
		}
	}

	/**
	 * Plays at p. If the move is illegal, there is no effect on Board data
	 * structures.
	 * 
	 * @param p
	 *            the location at which to play, or PASS.
	 * @return One of the PLAY_... constants defined in this class.
	 * @see #playFast(int)
	 */
	public int play(int p) {
		assert stoneCounts[BLACK] + stoneCounts[WHITE] + vacantPoints.size() == BOARD_AREA;
		// Passing is always legal
		if (p == PASS) {
			pass();
			return PLAY_OK;
		}
		// Runaway playouts are cut off by making non-passes illegal
		if (turn >= MAX_MOVES_PER_GAME - 2) {
			return PLAY_GAME_TOO_LONG;
		}
//		surroundingColors[turn] = getColorsAround(p);
		assert ON_BOARD[p] : pointToString(p);
		// Check for occupied point
		if (colors[p] != VACANT) {
			return PLAY_OCCUPIED;
		}
		// Check for simple ko violation
		if (p == koPoint) {
			return PLAY_KO_VIOLATION;
		}
		// Process neighbors, checking for suicide
		if (isSuicidal(p)) {
			return PLAY_SUICIDE;
		}
		// Check for superko violation
		long proposed = hashAfterRemovingCapturedStones(p);
		if (superKoTable.contains(proposed) || superKoTable.contains(~proposed)) {
			return PLAY_KO_VIOLATION;
		}
		// Hooray, it's legal!
		finalizePlay(p);
		// The hash to store for superko checking does not include the simple ko
		// point
		long hashToStore = hash ^ ZOBRIST_HASHES[VACANT][koPoint];
		if (colorToPlay == WHITE) {
			hashToStore = ~hashToStore;
		}
		superKoTable.add(hashToStore);
		return PLAY_OK;
	}

	/**
	 * Convenience method for writing tests.
	 * 
	 * @see Board#play(int)
	 */
	public int play(String label) {
		return play(at(label));
	}

	/**
	 * Plays at p. Similar to play(), but allows superko violations and does not
	 * check for passes or occupied points. In fact, once this has been used on
	 * a board, the superko table is no longer kept up-to-date. This method is
	 * used in the tails of playouts.
	 * 
	 * @param p
	 *            the location at which to play, or PASS.
	 * @return One of the PLAY_... constants defined in this class.
	 */
	public int playFast(int p) {
		assert stoneCounts[BLACK] + stoneCounts[WHITE] + vacantPoints.size() == BOARD_AREA;
		assert ON_BOARD[p] : pointToString(p);
		assert colors[p] == VACANT : pointToString(p) + "\n" + this;
//		surroundingColors[turn] = getColorsAround(p);
		// Check for simple ko violation
		if (p == koPoint) {
			return PLAY_KO_VIOLATION;
		}
		// Process neighbors, checking for suicide
		if (isSuicidal(p)) {
			return PLAY_SUICIDE;
		}
		// Hooray, it's legal!
		finalizePlay(p);
		return PLAY_OK;
	}

	/**
	 * Returns the score, including komi. Positive scores are good for black.
	 * Counts stones on board and one-point territories.
	 */
	public int playoutScore() {
		int eyeScore = 0;
		for (int i = 0; i < vacantPoints.size(); i++) {
			int p = vacantPoints.get(i);
			if (hasMaxNeighborsForColor(neighborCounts[p], BLACK)) {
				eyeScore++;
			} else if (hasMaxNeighborsForColor(neighborCounts[p], WHITE)) {
				eyeScore--;
			}
		}
		return approximateScore() + eyeScore;
	}

	/**
	 * Returns the winner, including komi.
	 * 
	 * @see #playoutScore()
	 */
	public int playoutWinner() {
		return (playoutScore() <= 0) ? WHITE : BLACK;
	}

	/** Removes stone s. */
	public void removeStone(int s) {
		int color = colors[s];
		stoneCounts[color]--;
		hash ^= ZOBRIST_HASHES[color][s];
		colors[s] = VACANT;
		vacantPoints.addKnownAbsent(s);
		computeNeighborhood(s);
		updateNeighborhoods(s);
		neighborsOfCapturedStone.clear();
		for (int k = 0; k < 4; k++) {
			int n = NEIGHBORS[s][k];
			neighborCounts[n] -= NEIGHBOR_INCREMENT[color];
			if (isAPlayerColor(colors[n])) {
				neighborsOfCapturedStone.addIfNotPresent(chainIds[n]);
			}
		}
		final int enemyColor = opposite(color);
		for (int k = 0; k < neighborsOfCapturedStone.size(); k++) {
			int c = neighborsOfCapturedStone.get(k);
			liberties[c].addKnownAbsent(s);
			if (liberties[c].size() > 1) {
				chainsInAtari[enemyColor].remove(c);
			}
		}
		// Clear chainId so that a stone removed by undo() doesn't appear to
		// still be part of its former chain
		// (This would be a problem when updating liberties if the same move
		// were replayed.)
		chainIds[s] = s;
	}

	/** For testing only. */
	public void setColorToPlay(int color) {
		colorToPlay = color;
	}

	/** Sets up the starting handicap for a board. */
	public void setUpHandicap(int handicapSize) {
		handicap = handicapSize;
		String[][] handicaps = { { "D4", "Q16" }, { "D4", "Q16", "D16" },
				{ "D4", "Q16", "D16", "Q4" },
				{ "D4", "Q16", "D16", "Q4", "K10" },
				{ "D4", "Q16", "D16", "Q4", "D10", "Q10" },
				{ "D4", "Q16", "D16", "Q4", "D10", "Q10", "K10" },
				{ "D4", "Q16", "D16", "Q4", "D10", "Q10", "K4", "K16" },
				{ "D4", "Q16", "D16", "Q4", ",D10", "Q10", "K4", "K16", "K10" } };
		for (int i = 0; i < handicapSize - 1; i++) {
			play(handicaps[handicapSize - 2][i]);
			play(Coordinates.PASS);
		}
		play(handicaps[handicapSize - 2][handicapSize - 1]);	
	}
	
	/** For testing only. */
	protected void setHash(long hash) {
		this.hash = hash;
	}

	/** Sets the komi. */
	public void setKomi(double komi) {
		this.komi = (int) (Math.ceil(-komi));
	}

	/** For testing only. */
	protected void setKoPoint(int koPoint) {
		this.koPoint = koPoint;
	}

	/** Sets the number of consecutive passes just before now. For testing only. */
	public void setPasses(int passes) {
		this.passes = passes;
	}

	/** For testing only. */
	protected void setTurn(int turn) {
		this.turn = turn;
	}

	/**
	 * Plays all of the stones in diagram, row by row from top to bottom. See
	 * the code of BoardTest for example diagrams.
	 */
	public void setUpProblem(int colorToPlay, String... diagram) {
		assert diagram.length == BOARD_WIDTH;
		assert diagram[0].length() == BOARD_WIDTH;
		clear();
		for (int r = 0; r < BOARD_WIDTH; r++) {
			for (int c = 0; c < BOARD_WIDTH; c++) {
				int color = charToColor(diagram[r].charAt(c));
				if (isAPlayerColor(color)) {
					if (this.colorToPlay != color) {
						play(PASS);
					}
					play(at(r, c));
				}
			}
		}
		if (this.colorToPlay != colorToPlay) {
			// TODO Would passing be cleaner? As written,
			// the parity of future moves may be wrong.
//			play(PASS);
			this.colorToPlay = colorToPlay;
		}
	}

	/**
	 * Similar to toString(), but in a format that can be read in by
	 * setUpProblem().
	 */
	public String[] toProblemString() {
		String[] result = new String[BOARD_WIDTH];
		for (int r = 0; r < BOARD_WIDTH; r++) {
			result[r] = "";
			for (int c = 0; c < BOARD_WIDTH; c++) {
				result[r] += Colors.colorToChar(colors[at(r, c)]);
			}
		}
		return result;
	}

	public String toString() {
		String result = "";
		// Upper indices
		result += BOARD_WIDTH < 10 ? " " : "  ";
		for (int c = 0; c < BOARD_WIDTH; c++) {
			result += " " + columnToString(c);
		}
		result += "\n";
		// Board
		for (int r = 0; r < BOARD_WIDTH; r++) {
			if (BOARD_WIDTH >= 10 && BOARD_WIDTH - r < 10) {
				result += " ";
			}
			result += rowToString(r);
			for (int c = 0; c < BOARD_WIDTH; c++) {
				result += " " + colorToChar(colors[at(r, c)]);
			}
			result += " " + rowToString(r) + "\n";
		}
		// Lower indices
		result += BOARD_WIDTH < 10 ? " " : "  ";
		for (int c = 0; c < BOARD_WIDTH; c++) {
			result += " " + columnToString(c);
		}
		result += "\n";
		return result;
	}

	/**
	 * Updates the neighbors of p's neighborhoods to reflect a change in p. If
	 * the pattern needs to be wholly recomputed, look at computeNeighborhood
	 * instead.
	 */
	protected void updateNeighborhoods(int p) {
		assert ON_BOARD[p];
		for (int i = 0; i < 8; i++) {
			int n = NEIGHBORS[p][i];
			if (colors[n] != VACANT) {
				continue;
			}
			int shift = UPDATE_NBR_MAP[i];
			// Clear out the bits
			neighborhoods[n] &= ~(0x3 << shift);
			// Reset the bits
			neighborhoods[n] |= colors[p] << shift;
		}
	}

}
