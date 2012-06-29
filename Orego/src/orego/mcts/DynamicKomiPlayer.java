package orego.mcts;

import orego.play.UnknownPropertyException;

/** This player uses dynamic komi to affect win rates of playouts. */
public class DynamicKomiPlayer extends Lgrf2Player {

	/** slowly reduces the amount of positive komi given */
	private double ratchet;

	public static void main(String[] args) {
		DynamicKomiPlayer p = new DynamicKomiPlayer();
		try {
			p.setProperty("policy", "Escape:Pattern:Capture");
			p.setProperty("threads", "2");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		double[] benchMarkInfo = p.benchmark();
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
				+ benchMarkInfo[1]);
	}

	public DynamicKomiPlayer() {
		super();
//		orego.experiment.Debug.setDebugFile("debug.txt");
		ratchet = Double.MAX_VALUE;
	}

	/**
	 * Changes the komi value for the game based on the win rate from a current
	 * board state.
	 */
	public void valueSituationalCompensation() {
		// losing threshold
		double red = .45;
		// winning threshold
		double green = .5;
		double value = getRoot().overallWinRate();
		int handicap = getBoard().getHandicap();
		// cut off between use of linear handicap and value situational handicap
		int m = 19 + 2 * handicap;
		if (handicap != 0 && getBoard().getTurn() < m) {
			// sets initial komi to 7 times the handicap stones to a max of 30,
			// which slowly reduces during the first 20 moves
			if (handicap > 3) {
				getBoard().setKomi(30);
			} else {
				ratchet = Double.MAX_VALUE;
				getBoard().setKomi(7 * handicap * (1 - ((getBoard().getTurn() - 2 * handicap - 1) / m)));
			}
		} else {
			// if we are losing a lot then reduce the komi by 1
			if (value < red) {
				// record the lowest positive komi given in this situation by
				// the ratchet variable
				if (getBoard().getKomi() > 0) {
					ratchet = getBoard().getKomi();
				}
				//when the game is about 95% complete stop adjusting komi
				if (getBoard().getVacantPoints().size() > 18) {
					if (Math.abs(getBoard().getKomi() - 1) < 30) {
						getBoard().setKomi(getBoard().getKomi() - 1);
					}
				}
				//if we are winning a lot then increase the komi
			} else if (value > green && getBoard().getKomi() < ratchet) {
				if (Math.abs(getBoard().getKomi() + 1) < 30) {
					getBoard().setKomi(getBoard().getKomi() + 1);
				}
			}
		}
//		orego.experiment.Debug.debug("Komi: " + getBoard().getKomi());
	}
	
	@Override
	public void reset() {
		super.reset();
		if (getTable() == null) {
			setTable(new TranspositionTable(getPrototypeNode()));
		}
		getTable().sweep();
		getTable().findOrAllocate(getBoard().getHash());
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new KomiRunnable(this, getPolicy().clone()));
		}
		SearchNode root = getRoot();
		if (root.isFresh()) {
			((KomiRunnable) getRunnable(0)).getPolicy().updatePriors(root,
					getBoard(), getPriors());
		}
	}

}
