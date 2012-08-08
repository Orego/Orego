package orego.mcts;

import static orego.core.Colors.VACANT;
import orego.core.Coordinates;
import orego.play.UnknownPropertyException;
import static orego.heuristic.AbstractPatternHeuristic.*;

public class Lgrf2PatternPlayer extends Lgrf2Player {

	private int weight;
	
	public Lgrf2PatternPlayer(){
		weight = 1;
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("patternWeight")) {
			weight = Integer.parseInt(value);
		} else {
			super.setProperty(property, value);
		}
	}

	@Override
	public void updateForAcceptMove(int p) {
		super.updateForAcceptMove(p);
		for (int point : Coordinates.ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(point) == VACANT) {
				char neighborhood = getBoard().getNeighborhood(point);
				if (GOOD_NEIGHBORHOODS[getBoard().getColorToPlay()]
						.get(neighborhood)) {
					getRoot().addWins(point, weight);
				}
				if (BAD_NEIGHBORHOODS[getBoard().getColorToPlay()]
						.get(neighborhood)) {
					getRoot().addLosses(point, weight);
				}
			}
		}
	}

}
