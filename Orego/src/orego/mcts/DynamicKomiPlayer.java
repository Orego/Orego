package orego.mcts;

import orego.play.UnknownPropertyException;

/** This player uses dynamic komi to affect win rates of playouts. */
public class DynamicKomiPlayer extends Lgrf2Player {

	/** slowly reduces the amount of positive komi given */
	private double ratchet;
	public static final int HANDICAP_STONE_VALUE = 7;
	// losing threshold
	public static final double RED = .45;
	// winning threshold
	public static final double GREEN = .5;
	// number of handicap stones
	private int handicap;
	// cut off between use of linear handicap and value situational handicap
	private int cutOff;

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
		// orego.experiment.Debug.setDebugFile("debug.txt");
		ratchet = Double.MAX_VALUE;
		cutOff = 19 + 2 * handicap;
		if (handicap > 0) {
			linearHandicap();
		}
	}

	/**
	 * Changes the komi value for the game based on the win rate from a current
	 * board state.
	 */
	public void valueSituationalCompensation() {
		orego.experiment.Debug.debug("IN");
		double value = getRoot().overallWinRate();
		handicap = getBoard().getHandicap();
		if (getBoard().getTurn() < cutOff) {
			if (handicap > 0) {
				linearHandicap();
			}
		} else {
			// If game reaches 95% completion we stop using negative komi.
			if (getBoard().getVacantPoints().size() <= 86
					&& getBoard().getKomi() < 0) {
				getBoard().setKomi(0);
			}
			// if we are losing a lot then reduce the komi by 1
			if (value < RED) {
				// record the lowest positive komi given in this situation by
				// the ratchet variable
				if (getBoard().getKomi() > 0) {
					ratchet = getBoard().getKomi();
				}
				// when the game is about 95% complete stop adjusting komi
				// 86 comes from 20% of board is empty at the end of a game and
				// we stop using negative komi at 95% of game completed. This
				// comes from http://pasky.or.cz/go/dynkomi.pdf and an email on
				// the computer go mailing list.
				if (getBoard().getVacantPoints().size() > 86) {
					getBoard().setKomi(getBoard().getKomi() - 1);
				}
				// if we are winning a lot then increase the komi
			} else if (value > GREEN && getBoard().getKomi() < ratchet) {
				orego.experiment.Debug.debug("GOT IN TO THE GREEN, RATCHET IS "
						+ ratchet);
				if (getBoard().getKomi() + 1 < 30) {
					getBoard().setKomi(getBoard().getKomi() + 1);
				}
			}

		}
//		orego.experiment.Debug.debug("Overall Winrate: "
//				+ getRoot().overallWinRate() + "\nKomi: "
//				+ getBoard().getKomi());
		// orego.experiment.Debug.debug("Komi: " + getBoard().getKomi());
	}

	private void linearHandicap() {
		// sets initial komi to 7 times the handicap stones to a max of 30,
		// which slowly reduces during the first 20 moves
		getBoard()
				.setKomi(
						HANDICAP_STONE_VALUE
								* handicap
								* (1 - ((getBoard().getTurn() - 2 * handicap - 1) / cutOff)));
		if (getBoard().getKomi() > 30) {
			getBoard().setKomi(30);
		}
	}

	@Override
	public void reset() {
		super.reset();
		handicap = getBoard().getHandicap();
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
