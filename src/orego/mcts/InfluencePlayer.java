package orego.mcts;

import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.getAllPointsOnBoard;
import static orego.core.Coordinates.pointToString;

import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Influence;

public class InfluencePlayer extends TimePlayer {
	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-influence-values");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("gfx/Influence values/gogui-influence-values");
		return result;
	}	
	
	protected String goguiInfluence() {
		Influence i = new Influence(getBoard());
		i.bouzyDilate(4);
		i.bouzyErode(13);
		//i.zobristDilate(4);
		String result = "";
		for (int p : getAllPointsOnBoard()) {
			int v = i.getValue(p);
			String cs = "";
			if ((v > 0) && (v < 10)) {
				cs = "#666666";
			} else if ((v < 0) && (v > -10)) {
				cs = "#999999";
			} else if (v > 0) {
				cs = "#444444";
			} else if (v < 0) {
				cs = "#bbbbbb";
			}
			else { // v = 0 so color it pink
				cs = "#ff00ff";
			}
			result += String.format("\nCOLOR %s %s\nLABEL %s %d", cs, pointToString(p), pointToString(p), v);
		}
		return result;
	}

	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		String result = null;
		if (command.equals("gogui-influence-values")) {
			result = goguiInfluence();
		} else {
			result = super.handleCommand(command, arguments);
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return result;
	}
}
