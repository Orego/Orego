package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.MAX_POSSIBLE_BOARD_WIDTH;
import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.RESIGN;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.NonStoneColor.OFF_BOARD;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
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

@SuppressWarnings("serial")
public class Phenotype implements Mover {

	/** Convolutional neuron thresholds are in the range (-THRESHOLD_LIMIT, THRESHOLD_LIMIT). */
	public static final int THRESHOLD_LIMIT = 32;
	
	private LgrfTable table;
	
	private LinearLayer linearLayer;
	
	private ConvolutionalLayer convolutionalLayer;
	
	private Board board;
	
	private CoordinateSystem coords;
	
	private static final short[][][] NEIGHBORHOODS = new short[MAX_POSSIBLE_BOARD_WIDTH + 1][][];
	
	public static final short[][] OFFSETS = { { 0, -1 }, { 0, 1 }, { -1, 0 },
		{ 1, 0 }, { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }, { -2, 0 },
		{ 2, 0 }, { 0, -2 }, { 0, 2 }, { -2, -1 }, { -2, 1 }, { -1, -2 },
		{ -1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { 1, 2 }, { 2, 2 },
		{ 2, -2 }, { -2, 2 }, { -2, -2 }, { 3, 0 }, { -3, 0 }, { 0, -3 },
		{ 0, 3 }, { 4, 0 }, { -4, 0 }, { 0, -4 }, { 0, 4 }};
	
	public short[] getNeighborhood(short p){
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
				NEIGHBORHOODS[width] = new short[coords.getFirstPointBeyondBoard()][];
				for (final short p : pointsOnBoard) {
					NEIGHBORHOODS[width][p] = findNeighborhood(p);
				}
			}
			neighborhoods = NEIGHBORHOODS[width];
		}
	}

	public long contextAt(short p){
		short[] neighborhood = getNeighborhood(p);
		long context = 0;
		for(int i = 0; i < neighborhood.length; i++){
			Color c = board.getColorAt(neighborhood[i]);
			if (c == board.getColorToPlay()){
				context |= 0b00L << (2*i);
			} else if (c == VACANT){
				context |= 0b10L << (2*i);
			} else if (c == OFF_BOARD){
				context |= 0b11L << (2*i);
			}else{
				context |= 0b01L << (2*i);
			}
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
	 * @param replies Number of replies in the genotype. (The rest specified the network.) Must be even.
	 */
	public Phenotype(Board board, int replies, Genotype genotype) {
		this(board);
		assert replies % 2 == 0;
		long[] words = genotype.getWords();
		int w = 0;
		// Extract replies
		for (int i = 0; i < replies; w++, i += 2) {
			setReply((int) ((words[w] >>> 27) & MASKS[1]) == BLACK.index() ? BLACK : WHITE,
					(short) (words[w] & MASKS[9]),
					(short) ((words[w] >>> 9) & MASKS[9]),
					(short) ((words[w] >>> 18) & MASKS[9]));
			setReply((int) ((words[w] >>> (27 + 32)) & MASKS[1]) == BLACK.index() ? BLACK : WHITE,
					(short) ((words[w] >>> 32 ) & MASKS[9]),
					(short) ((words[w] >>> (9 + 32)) & MASKS[9]),
					(short) ((words[w] >>> (18 + 32)) & MASKS[9]));
		}
		// Extract value for network
		ConvolutionalNeuron [] neurons = new ConvolutionalNeuron[64];
		int n = 0;
		for (int i = replies/2; i < 19*64 + replies/2; i+=19, n++){
			int threshold = (int) words[i] % THRESHOLD_LIMIT;
			long [] excitation = new long [9];
			long [] inhibition = new long [9];
			for (int j = 0; j < 9; j++){
				excitation[j] = words[i + j + 1];
				inhibition[j] = words[i + j + 10];
			}
			neurons[n] = new ConvolutionalNeuron(threshold, excitation, inhibition);
		}
		// Convolutional layer
		convolutionalLayer.setNeurons(neurons);
		// Linear layer
		int f = replies/2 + 19*64;
		for(short to: coords.getAllPointsOnBoard()){
			for(short from: coords.getAllPointsOnBoard()){
//				for(; f < words.length; f+=8){
					for(int i= 0; i < 8; i++, f++) {
						for(int j = 0; j < 8; j++){
//							System.out.println(f);
							linearLayer.setWeight(to, from, j + i*8, (byte) (words[f] >> (8 * j)));							
						}
					}
				}
			
			linearLayer.setBias(to, (byte)words[f]);
			f++;
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
		convolutionalLayer = new ConvolutionalLayer(coords);
		linearLayer = new LinearLayer(convolutionalLayer, coords);
		allNeighborhoods();
	}

	public short bestMove(MersenneTwisterFast random) {
		// Try playing a stored reply
		final short ultimate = history.get(board.getTurn() - 1);
		final short penultimate = history.get(board.getTurn() - 2);
		short reply = table.getSecondLevelReply(board.getColorToPlay(),
				penultimate, ultimate);
		if (coords.isOnBoard(reply) && (board.getColorAt(reply) == VACANT) && filter.at(reply) && board.isLegal(reply)) {
			return reply;
		}
		reply = table.getFirstLevelReply(board.getColorToPlay(), ultimate);
		if (coords.isOnBoard(reply) && (board.getColorAt(reply) == VACANT) && filter.at(reply) && board.isLegal(reply)) {
			return reply;
		}
		// Ask the network
		convolutionalLayer.extractFeatures(board);
		convolutionalLayer.update();
		return linearLayer.bestMove(board, filter);
	}
	
	@Override
	public short selectAndPlayOneMove(MersenneTwisterFast random, boolean fast) {
		short p = bestMove(random);
		board.play(p);
		return p;
	}

	short getReply(StoneColor colorToPlay, short penultimateMove, short previousMove) {
		short p = table.getSecondLevelReply(colorToPlay, penultimateMove, previousMove);
		if (p == NO_POINT) {
			return table.getFirstLevelReply(colorToPlay, previousMove);
		}
		return p;
	}
	
	public void setReply(StoneColor colorToPlay, short penultimateMove, short previousMove, short reply) {
		if (coords.isOnBoard(reply)) {
			if (previousMove == NO_POINT || coords.isOnBoard(previousMove)) {
				if (!(penultimateMove == NO_POINT || coords.isOnBoard(penultimateMove))) {
					penultimateMove = RESIGN;
				}
				table.setReply(colorToPlay, penultimateMove, previousMove, reply);				
			}
		}
	}

	public void setBias(short p, byte bias) {
		linearLayer.setBias(p, bias);
	}

	public void randomizeBiases() {
		linearLayer.randomizeBiases();
	}

	public LinearLayer getLinearLayer() {
		return linearLayer;
	}

	public ConvolutionalLayer getConvolutionalLayer() {
		return convolutionalLayer;
	}

	/** Returns the number of moves that this phenotype correctly predicts from game. */
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
	
	public String contextToString(long a){
		
		return null;
	}

}
