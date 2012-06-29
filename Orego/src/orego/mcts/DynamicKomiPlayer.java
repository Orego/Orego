package orego.mcts;

/** This player uses dynamic komi to affect win rates of playouts. */
public class DynamicKomiPlayer extends Lgrf2Player {

	/** slowly reduces the amount of positive komi given */
	private double ratchet;
	public static final int HANDICAP_STONE_VALUE = 7;
	// losing threshold
	public static final double RED = .45;
	// winning threshold
	public static final double GREEN = .5;
	//number of handicap stones
	private int handicap;
	// cut off between use of linear handicap and value situational handicap
	private int cutOff;

	public DynamicKomiPlayer() {
		super();
		ratchet = Double.MAX_VALUE;
		handicap = getBoard().getHandicap();
		cutOff = 19 + 2 * handicap;
	}

	/**
	 * Changes the komi value for the game based on the win rate from a current
	 * board state.
	 */
	public void valueSituationalCompensation() {
		double value = getRoot().overallWinRate();
		if (handicap != 0 && getBoard().getTurn() < cutOff) {
			// sets initial komi to 7 times the handicap stones to a max of 30,
			// which slowly reduces during the first 20 moves
			if (handicap > 3) {
				getBoard().setKomi(30);
			} else {
				ratchet = Double.MAX_VALUE;
				getBoard().setKomi(HANDICAP_STONE_VALUE * handicap * (1 - ((getBoard().getTurn() - 2 * handicap - 1) / cutOff)));
			}
		} else {
			// if we are losing a lot then reduce the komi by 1
			if (value < RED) {
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
			} else if (value > GREEN && getBoard().getKomi() < ratchet) {
				if (Math.abs(getBoard().getKomi() + 1) < 30) {
					getBoard().setKomi(getBoard().getKomi() + 1);
				}
			}
		}
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
