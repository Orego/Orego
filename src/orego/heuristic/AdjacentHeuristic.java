package orego.heuristic;

import orego.core.*;
import static orego.core.Colors.VACANT;


public class AdjacentHeuristic extends Heuristic{

	//Inherits constructor
	public AdjacentHeuristic(int weight) {
		super(weight);
	}

	/** Suggests moves orthogonally adjacent to player's last move */
	@Override
	public void prepare(Board board){
		super.prepare(board);
		int lastMove = board.getMove(board.getTurn() - 2);
		//creates an array of points orthogonally adjacent to last move
		int[] neighbors = {lastMove + 20, 
				lastMove - 20, 
				lastMove + 1,
				lastMove - 1};
		for(int i = 0; i < 4; i++){
			if(board.getColor(neighbors[i]) == VACANT){
				recommend(neighbors[i]);
			}
		}	
	}
	
	@Override
	public AdjacentHeuristic clone(){
		return (AdjacentHeuristic)super.clone();
	}

}
