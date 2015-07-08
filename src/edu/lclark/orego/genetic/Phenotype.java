package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.StoneColor.*;
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

	private LgrfSuggester replier;
	
	private LinearLayer linearLayer;
	
	private ConvolutionalLayer convolutionalLayer;
	
	private Board board;
	
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
		for (int i = 0; i < replies; w++, i += 2) {
			setReply((int) ((words[w] >>> 27) & MASKS[1]) == BLACK.index() ? BLACK : WHITE,
					(short) (words[w] & MASKS[9]),
					(short) ((words[w] >>> 9) & MASKS[9]),
					(short) ((words[w] >>> 18) & MASKS[9]));
		}
	}
	
	public Phenotype(Board board) {
		this.board = board;
		CoordinateSystem coords = board.getCoordinateSystem();
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
