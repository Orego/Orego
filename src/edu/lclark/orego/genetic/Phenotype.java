package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.LgrfSuggester;
import edu.lclark.orego.feature.LgrfTable;
import edu.lclark.orego.feature.NotEyeLike;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.move.PredicateMover;
import edu.lclark.orego.move.SuggesterMover;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;

@SuppressWarnings("serial")
public class Phenotype implements Mover {

	private LgrfSuggester replier;
	
	private LinearLayer linearLayer;
	
	private ConvolutionalLayer convolutionalLayer;
	
	private Board board;
	
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
		short p = linearLayer.bestMove(board);
		board.play(p);
		return p;
	}

	public void setReply(StoneColor colorToPlay, short penultimateMove, short previousMove, short reply) {
		replier.getTable().setReply(colorToPlay, penultimateMove, previousMove, reply);
	}

	public void setBias(short p, byte bias) {
		linearLayer.setBias(p, bias);
	}
	
//	public changeConvolutionalLayer() {
//		
//	}

}
