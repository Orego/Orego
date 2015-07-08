package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.genetic.Phenotype.THRESHOLD_LIMIT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.LgrfSuggester;
import edu.lclark.orego.feature.LgrfTable;
import edu.lclark.orego.feature.NotEyeLike;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;

@SuppressWarnings("serial")
public class Phenotype implements Mover {

	/** Convolutional neuron thresholds are in the range (-THRESHOLD_LIMIT, THRESHOLD_LIMIT). */
	public static final int THRESHOLD_LIMIT = 32;
	
	private LgrfSuggester replier;
	
	private LinearLayer linearLayer;
	
	private ConvolutionalLayer convolutionalLayer;
	
	private Board board;
	
	private CoordinateSystem coords;
	
	/** MASKS[i] has the i lowest-order bits on. */
	public static final long[] MASKS = new long[64];
	
	static {
		for (int i = 0; i < MASKS.length; i++) {
			MASKS[i] = (1L << i) - 1;
		}
	}
	
	/**
	 * @param replies Number of replies in the genotype. (The rest specified the network.)
	 */
	public Phenotype(Board board, int replies, Genotype genotype) {
		this(board);
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
		//TODO what if replies is odd?
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
		int f = 1221;
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
	
	
	
	public Phenotype(Board board) {
		this.board = board;
		coords = board.getCoordinateSystem();
		replier = new LgrfSuggester(board, new HistoryObserver(board), new LgrfTable(coords), new NotEyeLike(board));
		convolutionalLayer = new ConvolutionalLayer(coords);
		linearLayer = new LinearLayer(convolutionalLayer, coords);
	}

	@Override
	public short selectAndPlayOneMove(MersenneTwisterFast random, boolean fast) {
		// Try playing a stored reply
		final ShortSet suggestedMoves = replier.getMoves();
		if (suggestedMoves.size() > 0) {
			short p = suggestedMoves.get(0);
			Legality legality = fast ? board.playFast(p) : board.play(p);
			if (legality == OK) {
				return p;
			}
		}
		// Ask the network
		convolutionalLayer.extractFeatures(board);
		convolutionalLayer.update();
		short p = linearLayer.bestMove(board);
		board.play(p);
		return p;
	}

	short getReply(StoneColor colorToPlay, short penultimateMove, short previousMove) {
		LgrfTable table = replier.getTable();
		short p = table.getSecondLevelReply(colorToPlay, penultimateMove, previousMove);
		if (p == NO_POINT) {
			return table.getFirstLevelReply(colorToPlay, previousMove);
		}
		return p;
	}
	
	public void setReply(StoneColor colorToPlay, short penultimateMove, short previousMove, short reply) {
		CoordinateSystem coords = board.getCoordinateSystem();
		if (coords.isOnBoard(reply)) {
			if (previousMove == NO_POINT || coords.isOnBoard(previousMove)) {
				if (!(penultimateMove == NO_POINT || coords.isOnBoard(penultimateMove))) {
					penultimateMove = RESIGN;
				}
				replier.getTable().setReply(colorToPlay, penultimateMove, previousMove, reply);				
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
	
//	public changeConvolutionalLayer() {
//		
//	}

}
