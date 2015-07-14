package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.MAX_POSSIBLE_BOARD_WIDTH;
import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.RESIGN;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.NonStoneColor.OFF_BOARD;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.feature.Conjunction;
import edu.lclark.orego.feature.Disjunction;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.LgrfTable;
import edu.lclark.orego.feature.NearAnotherStone;
import edu.lclark.orego.feature.NotEyeLike;
import edu.lclark.orego.feature.OnThirdOrFourthLine;
import edu.lclark.orego.feature.Predicate;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

import java.util.*;

@SuppressWarnings("serial")
public class Phenotype implements Mover {

	private LgrfTable table;

	private Set<Long> contexts;

	private Board board;

	private CoordinateSystem coords;

	private static final short[][][] NEIGHBORHOODS = new short[MAX_POSSIBLE_BOARD_WIDTH + 1][][];

	public static final short[][] OFFSETS = { { 0, -1 }, { 0, 1 }, { -1, 0 },
			{ 1, 0 }, { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }, { -2, 0 },
			{ 2, 0 }, { 0, -2 }, { 0, 2 }, { -2, -1 }, { -2, 1 }, { -1, -2 },
			{ -1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { 1, 2 }, { 2, 2 },
			{ 2, -2 }, { -2, 2 }, { -2, -2 }, { 3, 0 }, { -3, 0 }, { 0, -3 },
			{ 0, 3 }, { 4, 0 }, { -4, 0 }, { 0, -4 }, { 0, 4 } };

	public short[] getNeighborhood(short p) {
		return NEIGHBORHOODS[coords.getWidth()][p];
	}

	private short[] findNeighborhood(short p) {
		final int r = coords.row(p), c = coords.column(p);
		final short[] result = new short[OFFSETS.length];
		int count = 0;
		for (int i = 0; i < OFFSETS.length; i++) {
			final int rr = r + OFFSETS[i][0];
			final int cc = c + OFFSETS[i][1];
			if (coords.isValidOneDimensionalCoordinate(rr)
					&& coords.isValidOneDimensionalCoordinate(cc)) {
				result[count] = coords.at(rr, cc);
				count++;
			} else {
				result[count] = coords.getFirstPointBeyondBoard();
				count++;
			}
		}
		// Create a small array and copy the elements into it
		return java.util.Arrays.copyOf(result, count);
	}

	/**
	 * Large-knight neighborhoods around points. First index is point around
	 * which neighborhood is defined.
	 */
	private short[][] neighborhoods;

	public void allNeighborhoods() {
		final int width = coords.getWidth();
		synchronized (NEIGHBORHOODS) {
			if (NEIGHBORHOODS[width] == null) {
				final short[] pointsOnBoard = coords.getAllPointsOnBoard();
				NEIGHBORHOODS[width] = new short[coords
						.getFirstPointBeyondBoard()][];
				for (final short p : pointsOnBoard) {
					NEIGHBORHOODS[width][p] = findNeighborhood(p);
				}
			}
			neighborhoods = NEIGHBORHOODS[width];
		}
	}

	public long contextAt(short p) {
		short[] neighborhood = getNeighborhood(p);
		long context = 0;
		int shift = 0;
		for (int i = 0; i < neighborhood.length; i++, shift += 2) {
			long c = board.getColorAt(neighborhood[i]).index();
			if (c < 2 && board.getColorToPlay() == WHITE) { // If it's a stone
															// color
				c = 1 - c;
			}
			context |= c << shift;
		}
		return context;
	}

	/** MASKS[i] has the i lowest-order bits on. */
	public static final long[] MASKS = new long[64];

	static {
		for (int i = 0; i < MASKS.length; i++) {
			MASKS[i] = (1L << i) - 1;
		}
	}

	/**
	 * @param replyLongs
	 *            Number of longs in each genotype specifying replies (rather
	 *            than contexts).
	 */
	public Phenotype(Board board, int replyLongs, Genotype genotype) {
		this(board);
		long[] words = genotype.getWords();
		// Extract replies
		for (int i = 0; i < replyLongs; i++) {
			setReply(
					(int) ((words[i] >>> 27) & MASKS[1]) == BLACK.index() ? BLACK
							: WHITE, (short) (words[i] & MASKS[9]),
					(short) ((words[i] >>> 9) & MASKS[9]),
					(short) ((words[i] >>> 18) & MASKS[9]));
			setReply(
					(int) ((words[i] >>> (27 + 32)) & MASKS[1]) == BLACK.index() ? BLACK
							: WHITE, (short) ((words[i] >>> 32) & MASKS[9]),
					(short) ((words[i] >>> (9 + 32)) & MASKS[9]),
					(short) ((words[i] >>> (18 + 32)) & MASKS[9]));
		}
		// Extract contexts
		contexts = new HashSet<>();
		for (int i = replyLongs; i < words.length; i++) {
			contexts.add(words[i]);
		}
	}

	private HistoryObserver history;

	private Predicate filter;

	public Phenotype(Board board) {
		this.board = board;
		coords = board.getCoordinateSystem();
		history = new HistoryObserver(board);
		filter = new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board)));
		table = new LgrfTable(coords);
		contexts = new HashSet<>();
		allNeighborhoods();
	}

	public short bestMove(MersenneTwisterFast random) {
		// Try playing a stored reply
		final short ultimate = history.get(board.getTurn() - 1);
		final short penultimate = history.get(board.getTurn() - 2);
		short reply = table.getSecondLevelReply(board.getColorToPlay(),
				penultimate, ultimate);
		if (coords.isOnBoard(reply) && (board.getColorAt(reply) == VACANT)
				&& filter.at(reply) && board.isLegal(reply)) {
			return reply;
		}
		reply = table.getFirstLevelReply(board.getColorToPlay(), ultimate);
		if (coords.isOnBoard(reply) && (board.getColorAt(reply) == VACANT)
				&& filter.at(reply) && board.isLegal(reply)) {
			return reply;
		}
		// Try finding a move with a good spatial context
		final short start = (short) random.nextInt(board.getVacantPoints()
				.size());
		short i = start;
		final short skip = PRIMES[random.nextInt(PRIMES.length)];
		do {
			final short p = board.getVacantPoints().get(i);
			if (filter.at(p) && board.isLegal(p)
					&& containsContext(contextAt(p))) {
				return p;
			}
			// Advancing by a random prime skips through the array
			// in a manner analogous to double hashing.
			i = (short) ((i + skip) % board.getVacantPoints().size());
		} while (i != start);
		// No move found
		return NO_POINT;
	}

	@Override
	public short selectAndPlayOneMove(MersenneTwisterFast random, boolean fast) {
		short p = bestMove(random);
		board.play(p);
		return p;
	}

	short getReply(StoneColor colorToPlay, short penultimateMove,
			short previousMove) {
		short p = table.getSecondLevelReply(colorToPlay, penultimateMove,
				previousMove);
		if (p == NO_POINT) {
			return table.getFirstLevelReply(colorToPlay, previousMove);
		}
		return p;
	}

	public void setReply(StoneColor colorToPlay, short penultimateMove,
			short previousMove, short reply) {
		if (coords.isOnBoard(reply)) {
			if (previousMove == NO_POINT || coords.isOnBoard(previousMove)) {
				if (!(penultimateMove == NO_POINT || coords
						.isOnBoard(penultimateMove))) {
					penultimateMove = RESIGN;
				}
				table.setReply(colorToPlay, penultimateMove, previousMove,
						reply);
			}
		}
	}

	/**
	 * Returns the number of moves that this phenotype correctly predicts from
	 * game.
	 */
	@SuppressWarnings("boxing")
	public int hits(Short[] game) {
		MersenneTwisterFast random = new MersenneTwisterFast();
		board.clear();
		int result = 0;
		for (short p : game) {
			if (bestMove(random) == p) {
				result++;
			}
			board.play(p);
		}
		return result;
	}

	/** Note that # indicates a friendly stone, O an enemy stone. */
	public String contextToString(long a) {
		char[] c = new char[32];
		for (int i = 0; i < c.length; i++) {
			long temp = (a & (0b11L << (i * 2))) >>> (i * 2);
			if (temp == 0) {
				c[i] = BLACK.toChar();
			} else if (temp == 1) {
				c[i] = WHITE.toChar();
			} else if (temp == 2) {
				c[i] = VACANT.toChar();
			} else {
				c[i] = OFF_BOARD.toChar();
			}
		}
		String result = "    " + c[29] + "\n" + "    " + c[25] + "\n" + "  "
				+ c[23] + c[12] + c[8] + c[13] + c[22] + "\n" + "  " + c[14]
				+ c[4] + c[2] + c[5] + c[15] + "\n" + c[30] + c[26] + c[10]
				+ c[0] + " " + c[1] + c[11] + c[27] + c[31] + "\n" + "  "
				+ c[18] + c[6] + c[3] + c[7] + c[19] + "\n" + "  " + c[21]
				+ c[17] + c[9] + c[16] + c[20] + "\n" + "    " + c[24] + "\n"
				+ "    " + c[28] + "\n";
		return result;
	}

	public boolean containsContext(long context) {
		return contexts.contains(context);
	}

	public void addContext(long context) {
		contexts.add(context);
	}

}
