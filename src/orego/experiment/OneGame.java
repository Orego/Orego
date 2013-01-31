package orego.experiment;

import orego.play.*;
import orego.mcts.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import static orego.experiment.Debug.*;
import static java.io.File.*;

/**
 * Has Orego play one game against itself. This may be useful for generating
 * logging data.
 */
public class OneGame {

	public static void main(String[] args) throws UnknownPropertyException {
		setDebugFile(OREGO_ROOT_DIRECTORY + separator + "log.txt");
		setDebugToStderr(true);
		Lgrf2Player[] players = { new Lgrf2Player(),
				new Lgrf2Player() };
		for (Lgrf2Player player : players) {
			// Set any properties here as player.setProperty("property", "value")
			player.reset();
		}
		int current = BLACK;
		while (players[BLACK].getBoard().getPasses() < 2) {
			int p = players[current].bestMove();
			if (p == RESIGN) {
				break;
			}
			for (Lgrf2Player player : players) {
				player.acceptMove(p);
			}
			current = opposite(current);
		}
	}

}
