package orego.mcts;

import orego.policy.Policy;
import static orego.core.Colors.*;

public class KomiRunnable extends McRunnable {

	public KomiRunnable(DynamicKomiPlayer player, Policy policy) {
		super(player, policy);
		orego.experiment.Debug.setDebugFile("/Network/servers/maccsserver.lclark.edu/Users/nsylvester/Desktop/Dynamic_Komi.txt");
		orego.experiment.Debug.debug("DEBUGGING");
	}

	/**
	 * Performs runs and incorporate them into player's search tree until this
	 * thread is interrupted.
	 */
	@Override
	public void run() {
		orego.experiment.Debug.debug("RUN METHOD");
		boolean limitPlayouts = getPlayer().getMillisecondsPerMove() <= 0;
		int playouts = 0;
		int limit = getPlayer().getPlayoutLimit();
		while ((limitPlayouts && playouts < limit)
				|| (!limitPlayouts & getPlayer().shouldKeepRunning())) {
			performMcRun();
			playouts++;
			if (playouts % 1000 == 0
					&& getPlayer().getBoard().getColorToPlay() == BLACK) {
				DynamicKomiPlayer player = (DynamicKomiPlayer) getPlayer();
				player.valueSituationalCompensation();
				
			}
		}
	}
}
