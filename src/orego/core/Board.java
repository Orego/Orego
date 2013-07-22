package orego.core;

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
	public static int MAX_MOVES_PER_GAME = getBoardArea() * 3;

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
	 * Random numbers for Zobrist hashes, indexed by color and point. The last
	 * row is for the simple ko point.
	 */
	public static final long[][] ZOBRIST_HASHES = new long[3][getFirstPointBeyondBoard()];

	/**
	 * Maximum radius of a pattern.
	 */
	public static final int MAX_PATTERN_RADIUS = 4;
	
	/**
	 * Locations of nearby points for pattern maintenance.
	 * PATTERN_MAINTENANCE_OFFSETS[r][p] is an array of pairs {q, offset}, each
	 * indicating the pattern position of p within q's pattern.
	 */
	public static final int[][][][] PATTERN_MAINTENANCE_OFFSETS = new int[MAX_PATTERN_RADIUS + 1][getFirstPointBeyondBoard()][][];
	
	/**
	 * Random numbers for Zobrist hashes for patterns, indexed by radius, color and point.
	 */
	public static final long[][][] PATTERN_ZOBRIST_HASHES = new long[MAX_PATTERN_RADIUS + 1][OFF_BOARD_COLOR + 1][];

	
	static { // Initialize ZOBRIST_HASHES
		MersenneTwisterFast random = new MersenneTwisterFast(0L);
		for (int color = 0; color < ZOBRIST_HASHES.length; color++) {
			for (int p : getAllPointsOnBoard()) {
				ZOBRIST_HASHES[color][p] = random.nextLong();
			}
		}
		// Set the element below to zero, so that xoring in the ko point when
		// there isn't one has no effect.
		ZOBRIST_HASHES[VACANT][NO_POINT] = 0L;		
		// Initialize patterns
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			for (int color : new int[] {BLACK, WHITE, OFF_BOARD_COLOR}) {
				int size = (2 * radius + 1) * (2 * radius + 1);
				PATTERN_ZOBRIST_HASHES[radius][color] = new long[size];
				for (int p = 0; p < size; p++) {
					PATTERN_ZOBRIST_HASHES[radius][color][p] = random.nextLong();
				}
			}
		}
		// Initialize offsets for pattern maintenance
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			for (int p : getAllPointsOnBoard()) {
				int row = row(p);
				int column = column(p);
				ArrayList<int[]> pairs = new ArrayList<int[]>();
				for (int r = row - radius; r <= row + radius; r++) {
					for (int c = column - radius; c <= column + radius; c++) {
						if (isValidOneDimensionalCoordinate(r) && isValidOneDimensionalCoordinate(c)) {
							pairs.add(new int[] {at(r, c), (row - r + radius) * (radius * 2 + 1) + column - c + radius});
						}
					}
				}
				PATTERN_MAINTENANCE_OFFSETS[radius][p] = pairs.toArray(new int[0][0]);
			}
		}
	}
	
	/**
	 * Records whether or not we should keep track of current pattern in all points on board (probably computation intensive).
	 */
	private boolean maintainPatternHashes = false;
	
	/** Incrementally maintained pattern hash by point and radius. */
	private long[][] patternHashAtPoint;
	
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
	
	/** The black stones on the board at the beginning of the game. */
	private IntSet initialBlackStones;
	
	/** The white stones on the board at the beginning of the game. */
	private IntSet initialWhiteStones;

	/** Komi, stored in a form that speeds score counting. */
	private double komi;

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
		maintainPatternHashes = false;
		clear();
	}
	
	public Board(boolean maintainPatternHashes){
		this.maintainPatternHashes = maintainPatternHashes;
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
	protected void adjustEnemyNeighbors(int color, int p) {
		int enemyColor = opposite(color);
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
	protected void adjustFriendlyNeighbors(int color, int p) {
		if (friendlyNeighboringChainIds.size() == 0) {
			// If there are no friendly neighbors, create a new, one-stone chain
			chainNextPoints[p] = p;
			liberties[p].copyDataFrom(lastPlayLiberties);
			if (liberties[p].size() == 1) {
				chainsInAtari[color].addKnownAbsent(p);
			}
		} else if (friendlyNeighboringChainIds.size() == 1) {
			// If there is only one friendly neighbor, add this stone to that
			// chain
			int c = friendlyNeighboringChainIds.get(0);
			liberties[c].addAll(lastPlayLiberties);
			liberties[c].removeKnownPresent(p);
			addStone(p, c);
			if (liberties[c].size() == 1) {
				chainsInAtari[color].add(c);
			} else {
				chainsInAtari[color].remove(c);
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
			liberties[c].addAll(lastPlayLiberties);
			assert liberties[c].contains(p);
			liberties[c].removeKnownPresent(p);
			if (liberties[c].size() == 1) {
				chainsInAtari[color].add(c);
			} else {
				chainsInAtari[color].remove(c);
			}
		}
	}

	/**
	 * Returns the score, including komi and counting only stones (not
	 * territory). Positive scores are good for black.
	 */
	public double approximateScore() {
		return stoneCounts[BLACK] - stoneCounts[WHITE] + komi;
	}

	/**
	 * Returns the winner, including komi and counting only stones (not
	 * territory).
	 */
	public int approximateWinner() {
		return winnerFromScore(approximateScore());
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
		initialBlackStones = new IntSet(getFirstPointBeyondBoard());
		initialWhiteStones = new IntSet(getFirstPointBeyondBoard());
		MAX_MOVES_PER_GAME = getBoardArea() * 3;
		friendlyNeighboringChainIds = new IntList(4);
		enemyNeighboringChainIds = new IntList(4);
		lastPlayLiberties = new IntSet(getFirstPointBeyondBoard());
		neighborsOfCapturedStone = new IntList(4);
		selfAtariLiberties = new IntSet(getFirstPointBeyondBoard());
		moves = new int[MAX_MOVES_PER_GAME];
		colors = new int[getExtendedBoardArea()];
		vacantPoints = new IntSet(getFirstPointBeyondBoard());
		neighborCounts = new int[getExtendedBoardArea()];
		koPoint = NO_POINT;
		chainsInAtari = new IntSet[] { new IntSet(getFirstPointBeyondBoard()),
				new IntSet(getFirstPointBeyondBoard()) };
		chainIds = new int[getExtendedBoardArea()];
		stoneCounts = new int[NUMBER_OF_PLAYER_COLORS];
		chainNextPoints = new int[getFirstPointBeyondBoard()];
		liberties = new IntSet[getFirstPointBeyondBoard()];
		adjacentChains = new BitVector(getFirstPointBeyondBoard());
		superKoTable = new SuperKoTable();
		for (int p = 0; p < getExtendedBoardArea(); p++) {
			neighborCounts[p] = FOUR_VACANT_NEIGHBORS;
			chainIds[p] = p;
			colors[p] = OFF_BOARD_COLOR;
			if (isOnBoard(p)) {
				liberties[p] = new IntSet(getFirstPointBeyondBoard());
				colors[p] = VACANT;
				vacantPoints.addKnownAbsent(p);
				int edgeCount = 0;
				for (int i = 0; i < 4; i++) {
					int n = getNeighbors(p)[i];
					if (!isOnBoard(n)) {
						edgeCount++;
					}
				}
				neighborCounts[p] += edgeCount * EDGE_INCREMENT;
			}
		}
		
		if (maintainPatternHashes){
			maintainPatternHashes = false;
			patternHashAtPoint = new long[getFirstPointBeyondBoard()][MAX_PATTERN_RADIUS + 1];
			for (int p=0; p < getFirstPointBeyondBoard(); p++){
				if (getColor(p)==VACANT){
					for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++){
						patternHashAtPoint[p][radius] = getPatternHash(p, radius);
					}
				}
			}
			maintainPatternHashes = true;
		}
	}

	/**
	 * Copies all data from that into this. Similar to cloning that, but without
	 * the overhead of creating a new object.
	 */
	public void copyDataFrom(Board that) {
		superKoTable.copyDataFrom(that.superKoTable);
		System.arraycopy(that.colors, 0, colors, 0, getExtendedBoardArea());
		System.arraycopy(that.neighborCounts, 0, neighborCounts, 0,
				getExtendedBoardArea());
		System.arraycopy(that.chainNextPoints, 0, chainNextPoints, 0,
				getFirstPointBeyondBoard());
		for (int p : getAllPointsOnBoard()) {
			liberties[p].copyDataFrom(that.liberties[p]);
		}
		System.arraycopy(that.chainIds, 0, chainIds, 0, getExtendedBoardArea());
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
		maintainPatternHashes = that.maintainPatternHashes;
		if (maintainPatternHashes){
			patternHashAtPoint = new long[getFirstPointBeyondBoard()][MAX_PATTERN_RADIUS + 1];
			for (int p : getAllPointsOnBoard()){
				System.arraycopy(that.patternHashAtPoint[p], 0, patternHashAtPoint[p], 0, MAX_PATTERN_RADIUS + 1);
			}
		}
	}

	/**
	 * Updates data structures at the end of a play. Used by play() and
	 * playFast().
	 */
	protected void finalizePlay(int p) {
		finalizePlay(colorToPlay, p, true);
	}
	
	/**
	 * Updates data structures at the end of a play, except for move and turn.
	 * Used by placeInitialStone()
	 */
	protected void finalizePlay(int color, int p) {
		finalizePlay(color, p, false);
	}
	
	/**
	 * Updates data structures at the end of a play.
	 */
	protected void finalizePlay(int color, int p, boolean updateTurn) {
		int lastVacantPointCount = vacantPoints.size();
		placeStone(color, p);
		boolean surrounded = hasMaxNeighborsForColor(neighborCounts[p],
				opposite(color));
		adjustFriendlyNeighbors(color, p);
		adjustEnemyNeighbors(color, p);
		if (liberties[chainIds[p]].size() == 1) {
			chainsInAtari[color].add(chainIds[p]);
		}
		hash ^= ZOBRIST_HASHES[VACANT][koPoint];
		if ((lastVacantPointCount == vacantPoints.size()) & surrounded) {
			koPoint = vacantPoints.get(vacantPoints.size() - 1);
		} else {
			koPoint = NO_POINT;
		}
		hash ^= ZOBRIST_HASHES[VACANT][koPoint];
		if(updateTurn) {			
			colorToPlay = opposite(color);
			passes = 0;
			moves[turn] = p;
			turn++;
		}
	}

	/**
	 * Similar to playoutScore(), but can handle territories larger than one
	 * point. Assumes all stones on board are alive.
	 */
	public double finalScore() {
		boolean[] visited = new boolean[getExtendedBoardArea()];
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
		return winnerFromScore(finalScore());
	}

	/**
	 * Returns the pattern hash around p for the given radius. SLOW! If maintaining patterns incrementally,
	 * use getPatternHash instead.
	 */
	protected long findPatternHash(int p, int radius) {
		long result = 0L;
		int row = row(p);
		int column = column(p);
		int i = 0;
		for (int r = row - radius; r <= row + radius; r++) {
			for (int c = column - radius; c <= column + radius; c++) {
				if (isValidOneDimensionalCoordinate(r) && isValidOneDimensionalCoordinate(c)) {
					int color = getColor(at(r, c));
					if (color != VACANT) {
						result ^= PATTERN_ZOBRIST_HASHES[radius][color][i];
					}
				} else {
					result ^= PATTERN_ZOBRIST_HASHES[radius][OFF_BOARD_COLOR][i];					
				}
				i++;
			}
		}
		return result;
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
			int n = getNeighbors(p)[i];
			if (isOnBoard(n)) {
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

	/** Returns the next point in p's chain. Only makes sense for occupied points. */
	public int getChainNextPoint(int p) {
		return chainNextPoints[p];
	}

	/** Returns the chains of color that are in atari. */
	public IntSet getChainsInAtari(int color) {
		return chainsInAtari[color];
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

	/** Returns the black stones placed on the board before the game started. */
	public IntSet getInitialBlackStones() {
		return initialBlackStones;
	}
	
	/** Returns the white stones placed on the board before the game started. */
	public IntSet getInitialWhiteStones() {
		return initialWhiteStones;
	}
	
	/** Returns the komi. */
	public double getKomi() {
		return -komi;
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
	 * <p>
	 * Fills a list with the liberties of the chain containing point p. If
	 * liberties has too much room, size is set appropriately. It is assumed
	 * that liberties has enough room; if it didn't, this method would crash
	 * when trying to add more.
	 */
	protected void getLibertiesByTraversal(int p, IntList liberties) {
		assert isAPlayerColor(colors[p]);
		liberties.clear();
		int stone = p;
		do {
			if (getVacantNeighborCount(stone) > 0) {
				for (int i = 0; i < 4; i++) {
					int neighbor = getNeighbors(stone)[i];
					if (getColor(neighbor) == VACANT) {
						liberties.addIfNotPresent(neighbor);
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
	 * Gets the local 3x3 neighborhood around point p.
	 * 
	 * @see orego.patterns
	 */
	public final char getNeighborhood(int p) {
		assert isOnBoard(p);
		char result = 0;
		for (int i = 0; i < 8; i++) {
			result = (char) ((result >>> 2) | (colors[getNeighbors(p)[i]] << 14));
		}
		assert colors[p] == VACANT;
		return result;
	}
	
	/**
	 * Gets the local 3x3 neighborhood around point p with colors reversed.
	 */
	public final char getNeighborhoodColorsReversed(int p){
		assert isOnBoard(p);
		char result = 0;
		for(int i = 0; i < 8; i++){
			if(colors[getNeighbors(p)[i]] == WHITE || colors[getNeighbors(p)[i]] == BLACK){
				result = (char) ((result >>> 2) | (opposite(colors[getNeighbors(p)[i]]) << 14));
			} else {
				result = (char) ((result >>> 2) | (colors[getNeighbors(p)[i]] << 14));
			}
		}
		assert colors[p] == VACANT;
		return result;
	}

	/**
	 * Returns the number of consecutive passes ending the move sequence so far.
	 */
	public int getPasses() {
		return passes;
	}
	
	/**
	 * Returns the pattern hash around p for the given radius.
	 */
	public long getPatternHash(int p, int radius) {
		if (maintainPatternHashes){
			return patternHashAtPoint[p][radius];
		}
		return findPatternHash(p, radius);
	}	
	
		
		
		
		
//		//Check if you've run into an off board point, so that you don't wrap around to the other side of the board.
//		if (!foundEdge && getColor(p) == OFF_BOARD_COLOR) {
//			foundEdge = true;
//		}
//		//Follow the x-axis first, then explore the y-axis.
//		if (yOffset == 0 && Math.abs(xOffset) < distanceFromCenter) {
//			if (xOffset <= 0) {
//				patternHash ^= getPatternHash(distanceFromCenter, xOffset - 1, yOffset, foundEdge, p - EAST);
//			}
//			if (xOffset >= 0) {
//				patternHash ^= getPatternHash(distanceFromCenter, xOffset + 1, yOffset, foundEdge, p + EAST);
//			}
//		}
//		if (Math.abs(yOffset) < distanceFromCenter) {
//			if (yOffset <= 0) {
//				patternHash ^= getPatternHash(distanceFromCenter, xOffset, yOffset - 1, foundEdge, p - getSouth());
//			}
//			if (yOffset >= 0) {
//				patternHash ^= getPatternHash(distanceFromCenter, xOffset, yOffset + 1, foundEdge, p + getSouth());
//			}
//		}
//		//Combine a hash if it is not off the board
//		if (!foundEdge) {
//			patternHash ^= ZOBRIST_PATTERNS[distanceFromCenter - 1][getColor(p)][pointToPatternOffset(distanceFromCenter-1,xOffset,yOffset)];
//		}
//		//Otherwise, combine an off board hash into the current one.
//		else {
//			patternHash ^= ZOBRIST_PATTERNS[distanceFromCenter - 1][OFF_BOARD_COLOR][pointToPatternOffset(distanceFromCenter-1,xOffset,yOffset)];
//		}
	
	/**
	 * Returns the PLAY_... constant for play and placeInitialStone.
	 */
	protected int getPlayConstant(int color, int p) {
		// Runaway playouts are cut off by making non-passes illegal
				if (turn >= MAX_MOVES_PER_GAME - 2) {
					return PLAY_GAME_TOO_LONG;
				}
				assert isOnBoard(p) : pointToString(p);
				// Check for occupied point
				if (colors[p] != VACANT) {
					return PLAY_OCCUPIED;
				}
				// Check for simple ko violation
				if (p == koPoint) {
					return PLAY_KO_VIOLATION;
				}
				// Process neighbors, checking for suicide
				if (isSuicidal(color, p)) {
					return PLAY_SUICIDE;
				}
				// Check for superko violation
				long proposed = hashAfterRemovingCapturedStones(color, p);
				if (superKoTable.contains(proposed) || superKoTable.contains(~proposed)) {
					return PLAY_KO_VIOLATION;
				}
				return PLAY_OK;
	}

	/**
	 * Returns the array stoneCounts, indicating the number of stones of each
	 * color on the board.
	 */
	public int[] getStoneCounts() {
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
	 * Get Zobrist hash for a particular color and position.
	 */
	public long getZobristPatternHash(int patternType, int color, int position) {
		return PATTERN_ZOBRIST_HASHES[patternType][color][position];
	}

	/**
	 * Returns the hash value that would result if the captured stones were
	 * removed. Used by play() to detect superko.
	 */
	public long hashAfterRemovingCapturedStones(int color, int p) {
		long result = hash;
		result ^= ZOBRIST_HASHES[color][p];
		adjacentChains.clear(); // Chains to be captured
		int enemy = opposite(color);
		for (int i = 0; i < 4; i++) {
			if (colors[getNeighbors(p)[i]] == enemy) {
				int c = chainIds[getNeighbors(p)[i]];
				if ((liberties[c].size() == 1) & !adjacentChains.get(c)) {
					adjacentChains.set(c, true);
					int active = c;
					do {
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
		int count = 0;
		int opposite = opposite(colorToPlay);
		for (int i = 4; i < 8; i++) {
			if (colors[getNeighbors(p)[i]] == opposite) {
				count++;
			}
		}
		return count < getEyelikeThreshold(p);
	}

	/**
	 * Returns true if p is a feasible choice to play. The point must not be
	 * eyelike and be either on the 3rd or 4th row from the edge or within a
	 * large knight's move of another stone.
	 */
	public boolean isFeasible(int p) {
		return !isEyelike(p)
				&& (Coordinates.isOnThirdOrFourthLine(p) || isWithinALargeKnightsMoveOfAnotherStone(p));
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
		assert isOnBoard(p) : "Move not on board: " + p + "(" + pointToString(p)
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
		long proposed = hashAfterRemovingCapturedStones(colorToPlay, p);
		if (superKoTable.contains(proposed) || superKoTable.contains(~proposed)) {
			return false;
		}
		// Hooray, it's legal!
		return true;
	}
	
	protected boolean isMaintainingPatternHashes()	{
		return maintainPatternHashes;
	}

	/** Returns true if the play at move p would be a self-atari for color. */
	public boolean isSelfAtari(int p, int color) {
		assert colors[p] == VACANT;
		// Eliminate easy cases
		if (getVacantNeighborCount(p) >= 2) {
			return false;
		}
		selfAtariLiberties.clear();
		// Remember p so we don't count it as a liberty.
		selfAtariLiberties.addKnownAbsent(p);
		enemyNeighboringChainIds.clear();
		adjacentChains.clear(); // Allies
		final int enemyColor = opposite(color);
		for (int i = 0; i < 4; i++) {
			int n = getNeighbors(p)[i];
			int c = colors[n];
			int chain = chainIds[n];
			if (c == VACANT) {
				selfAtariLiberties.add(n);
			} else if (c == enemyColor && isInAtari(chain)) {
				selfAtariLiberties.add(n);
				enemyNeighboringChainIds.add(chain);
			} else if (c == color) {
				selfAtariLiberties.addAll(liberties[chain]);
				adjacentChains.set(chain, true);
			}
			if (selfAtariLiberties.size() >= 3) {
				// If we only had p and one other liberty, p would be self-atari
				return false;
			}
		}
		// We didn't avoid self-atari directly, but maybe a capture bought us a
		// distant liberty.
		for (int i = 0; i < enemyNeighboringChainIds.size(); i++) {
			int chain = enemyNeighboringChainIds.get(i);
			int stone = chain;
			do {
				for (int j = 0; j < 4; j++) {
					int neighbor = getNeighbors(stone)[j];
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

	protected boolean isSuicidal(int p) {
		return isSuicidal(colorToPlay, p);
	}
	
	/**
	 * Visits neighbors of p, looking for potential captures and chains to merge
	 * with the new stone. As a side effect, loads the fields
	 * friendlyNeighboringChainIds, enemyNeighboringChainIds, and liberties.
	 * 
	 * @return true if playing at p would be suicidal.
	 */
	protected boolean isSuicidal(int color, int p) {
		friendlyNeighboringChainIds.clear();
		enemyNeighboringChainIds.clear();
		lastPlayLiberties.clear();
		boolean suicide = true;
		for (int i = 0; i < 4; i++) {
			int n = getNeighbors(p)[i];
			int neighborColor = colors[n];
			if (neighborColor == VACANT) { // Vacant point
				lastPlayLiberties.add(n);
				suicide = false;
			} else if (neighborColor == color) { // Friendly neighbor
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
	 * Returns whether a point is within a knight's move of another stone.
	 */
	public boolean isWithinAKnightsMoveOfAnotherStone(int p) {
		int validPoints[] = Coordinates.getKnightNeighborhood(p);
		for (int i = 0; i < validPoints.length; i++) {
			if (colors[validPoints[i]] != VACANT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether a point is within a large knight's move of another stone.
	 */
	public boolean isWithinALargeKnightsMoveOfAnotherStone(int p) {
		int validPoints[] = Coordinates.getLargeKnightNeighborhood(p);
		for (int i = 0; i < validPoints.length; i++) {
			if (colors[validPoints[i]] != VACANT) {
				return true;
			}
		}
		return false;
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
		liberties[base].addAll(liberties[appendage]);
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
		passes++;
		moves[turn] = PASS;
		turn++;
	}
	
	/**
	 * Returns an array of ints with every two bits in the array representing a point and color
	 * @param radius
	 * @param p
	 * @return
	 */
	public int[] patternToArray(int radius, int p) {
		int i = 0, length = radius * (radius + 1) / 2;
		int[] output = new int[length];
		for (int row = row(p) - radius; row <= row(p) + radius; row++) {
			for (int column = column(p) - radius; column <= column(p) + radius; column++) {
				if (!(column == column(p) && row == row(p))) {//don't store anything about point itself--later, will assume vacant

					output[i/16]<<=2;
					assert (output[i/16]&0x3)==0;
					if (Coordinates.isValidOneDimensionalCoordinate(row)
							&& Coordinates
									.isValidOneDimensionalCoordinate(column))
						output[i/16] |= getColor(at(row, column));
					else
						output[i/16] |= OFF_BOARD_COLOR;
					i += 2;
				}
			}
		}

		return output;
	}
	
	/** Places a stone at point p. */
	protected void placeStone(int color, int p) {
		stoneCounts[color]++;
		colors[p] = color;
		
		if (maintainPatternHashes){
			updatePatternHashes(p, color);
		}
		
		hash ^= ZOBRIST_HASHES[colors[p]][p];
		vacantPoints.remove(p);
		for (int i = 0; i < 4; i++) {
			int n = getNeighbors(p)[i];
			neighborCounts[n] += NEIGHBOR_INCREMENT[color];
		}
	}
	
	/**
	 * Places a stone at p, without incrementing the turn or changing the color to play.
	 * If the move is illegal, there is no effect on Board data structures.
	 * 
	 * @param color
	 *			  the color of the stone to play.
	 * @param p
	 *            the location at which to play, or PASS.
	 * @return One of the PLAY_... constants defined in this class.
	 */
	public int placeInitialStone(int color, int p) {
		assert stoneCounts[BLACK] + stoneCounts[WHITE] + vacantPoints.size() == getBoardArea();
		// Passing is always legal
		if (p == PASS) {
			pass();
			return PLAY_OK;
		}
		// Check if move is legal
		int playLegality = getPlayConstant(color, p);
		if(playLegality != PLAY_OK) {
			return playLegality;
		}
		// Hooray, it's legal!
		finalizePlay(color, p);
		// Add this stone to the initial stones IntSet
		if(color == BLACK) {
			initialBlackStones.add(p);
		} else {
			initialWhiteStones.add(p);
		}
		// The hash to store for superko checking does not include the simple ko
		// point
		long hashToStore = hash ^ ZOBRIST_HASHES[VACANT][koPoint];
		if (color == WHITE) {
			hashToStore = ~hashToStore;
		}
		superKoTable.add(hashToStore);
		return PLAY_OK;
	}
	
	/**
	 * Convenience method for writing tests.
	 * 
	 * @see Board#placeInitialStone(int, int)
	 */
	public int placeInitialStone(int color, String string) {
		return placeInitialStone(color, at(string));
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
		assert stoneCounts[BLACK] + stoneCounts[WHITE] + vacantPoints.size() == getBoardArea();
		// Passing is always legal
		if (p == PASS) {
			pass();
			return PLAY_OK;
		}
		// Check if move is legal
		int playLegality = getPlayConstant(colorToPlay, p);
		if(playLegality != PLAY_OK) {
			return playLegality;
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
		assert stoneCounts[BLACK] + stoneCounts[WHITE] + vacantPoints.size() == getBoardArea();
		assert isOnBoard(p) : pointToString(p);
		assert colors[p] == VACANT : pointToString(p) + "\n" + this;
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
	public double playoutScore() {
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
		return winnerFromScore(playoutScore());
	}
	
//	private int pointToPatternOffset(int pattern, int xOffset, int yOffset){
//		return pattern+1+xOffset+((yOffset+pattern+1)*((pattern)*2+3));
//	}
	
	public String printPattern(int radius, int p, boolean printNewLines) {
		String output = "";
		for (int row = row(p)-radius; row <= row(p)+radius; row++){
			for (int column = column(p)-radius; column <= column(p)+radius; column++){
				if (Coordinates.isValidOneDimensionalCoordinate(row)&&Coordinates.isValidOneDimensionalCoordinate(column))
					output+=colorToChar(getColor(at(row,column)));
				else
					output+=colorToChar(OFF_BOARD_COLOR);
			}
			if (printNewLines)
				output+='\n';
		}
		return output;
	}

	/** Removes stones. */
	public void removeStone(int s) {
		int color = colors[s];
		
		if (maintainPatternHashes){
			updatePatternHashes(s, color);
		}
		
		stoneCounts[color]--;
		hash ^= ZOBRIST_HASHES[color][s];
		colors[s] = VACANT;
		vacantPoints.addKnownAbsent(s);
		neighborsOfCapturedStone.clear();
		for (int k = 0; k < 4; k++) {
			int n = getNeighbors(s)[k];
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

	/** Sets up the starting handicap for a board. Must be between 2 and 9 stones. */
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
			placeInitialStone(BLACK, handicaps[handicapSize - 2][i]);
		}
		placeInitialStone(BLACK, handicaps[handicapSize - 2][handicapSize - 1]);	
		if(handicapSize > 0){
			komi = 0;
		}
	}
	
	/** Sets the komi. */
	public void setKomi(double komi) {
		this.komi = -komi;
	}

	/** Sets the number of consecutive passes just before now. For testing only. */
	public void setPasses(int passes) {
		this.passes = passes;
	}
	
	/** Sets the black stones placed on the board before the game started. */
	public void setAndPlaceInitialBlackStones(IntSet stones) {
		initialBlackStones.addAll(stones);
		for(int i = 0; i < initialBlackStones.size(); i++) {
			placeInitialStone(BLACK, initialBlackStones.get(i));
		}
	}
	
	public void setAndPlaceInitialWhiteStones(IntSet stones) {
		initialWhiteStones.addAll(stones);
		for(int i = 0; i < initialWhiteStones.size(); i++) {
			placeInitialStone(WHITE, initialWhiteStones.get(i));
		}
	}

	/**
	 * Plays all of the stones in diagram, row by row from top to bottom. See
	 * the code of BoardTest for example diagrams.
	 */
	public void setUpProblem(int colorToPlay, String... diagram) {
		assert diagram.length == getBoardWidth();
		assert diagram[0].length() == getBoardWidth();
		clear();
		for (int r = 0; r < getBoardWidth(); r++) {
			for (int c = 0; c < getBoardWidth(); c++) {
				int color = charToColor(diagram[r].charAt(c));
				if (isAPlayerColor(color)) {
					int p = at(r, c);
					placeInitialStone(color, p);
				}
			}
		}
		this.colorToPlay = colorToPlay;
	}

	/**
	 * Similar to toString(), but in a format that can be read in by
	 * setUpProblem().
	 */
	public String[] toProblemString() {
		String[] result = new String[getBoardWidth()];
		for (int r = 0; r < getBoardWidth(); r++) {
			result[r] = "";
			for (int c = 0; c < getBoardWidth(); c++) {
				result[r] += Colors.colorToChar(colors[at(r, c)]);
			}
		}
		return result;
	}

	public String toString() {
		String result = "";
		// Upper indices
		result += "  ";
		for (int c = 0; c < getBoardWidth(); c++) {
			result += " " + columnToString(c);
		}
		result += "\n";
		// Board
		for (int r = 0; r < getBoardWidth(); r++) {
			if (getBoardWidth() >= 10 && getBoardWidth() - r < 10) {
				result += " ";
			}
			result += rowToString(r);
			for (int c = 0; c < getBoardWidth(); c++) {
				result += " " + colorToChar(colors[at(r, c)]);
			}
			result += " " + rowToString(r) + "\n";
		}
		// Lower indices
		result += "  ";
		for (int c = 0; c < getBoardWidth(); c++) {
			result += " " + columnToString(c);
		}
		result += "\n";
		return result;
	}
	
	/**
	 * Updates pattern hashes changed by placement (or removal) of point p
	 * @param p The point changed
	 * @param color The non-vacant color that was (or now is) at p
	 */
	protected void updatePatternHashes(int p, int color) {
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			for (int[] pair : PATTERN_MAINTENANCE_OFFSETS[radius][p]) {
				patternHashAtPoint[pair[0]][radius] ^= PATTERN_ZOBRIST_HASHES[radius][color][pair[1]];
			}
		}
	}

	// Utility: Calculate a winner from a score
	protected static int winnerFromScore(double score) {
		// This test for double equality is ok because we only expect
		// integer and half-integer values (and if we miss a couple ties, it
		// won't be the end of the world).
		if(score == 0) {
			return VACANT;
		}
		else if(score < 0) {
			return WHITE;
		}
		else {
			return BLACK;
		}
	}

}
