package edu.lclark.orego.feature;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.SearchNode;
import edu.lclark.orego.patterns.PatternFinder;
import edu.lclark.orego.patterns.ShapeTable;

@SuppressWarnings("serial")
public class ShapeRater implements Rater {
	
	private final Board board;
	
	private final CoordinateSystem coords;
	
	private ShapeTable shapeTable;
	
	private double shapeThreshold;
	
	private final int bias;
	
	public ShapeRater(Board board, ShapeTable shapeTable, double shapeThreshold, int bias){
		this.bias = bias;
		this.shapeThreshold = shapeThreshold;
		this.board = board;
		this.coords = board.getCoordinateSystem();
		this.shapeTable = shapeTable;
	}

	@Override
	public void updateNode(SearchNode node) {
		for(short p : coords.getAllPointsOnBoard()){
			if(board.getColorAt(p) == VACANT){
				long hash = PatternFinder.getHash(board, p, 24);
				float winRate = shapeTable.getWinRate(hash);
				if(shapeTable.getWinRate(hash) > shapeThreshold){
					float winsToAdd = bias * winRate * winRate;
					node.update(p, (int)winsToAdd, winsToAdd);
				}
			}
		}
	}

	public void setTable(ShapeTable table) {
		shapeTable = table;		
	}

}
