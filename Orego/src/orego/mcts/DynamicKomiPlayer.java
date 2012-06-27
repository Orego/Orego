package orego.mcts;

import orego.core.Coordinates;

public class DynamicKomiPlayer extends RavePlayer {
	
	public void valueSituationalCompensation() {
		int handicaps = 0;
		for (int i = 18; i > 0; i= i - 2) {
			if(getBoard().getMove(i) == Coordinates.PASS){
				handicaps++;
			}
		}
		if(getBoard().getTurn()<20){
			
		}
	}

}
