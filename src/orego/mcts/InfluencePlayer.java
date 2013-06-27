package orego.mcts;

import static orego.core.Coordinates.getAllPointsOnBoard;
import static orego.core.Coordinates.pointToString;

import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Influence;
import orego.play.UnknownPropertyException;

public class InfluencePlayer extends TimePlayer {

	public static final int BOUZY = 0;

	public static final int ZOBRIST = 1;

	private int dilations = 4;

	private int erosions = 13;

	private int influenceType = BOUZY;

	private int maxInfluenceToBias = 1;

	private int winsToAdd = 5;

	@Override
	public void beforeStartingThreads() {
		// do the biasing: getRoot().addWins(p, number of wins);
		super.beforeStartingThreads();
		Influence inf = new Influence(getBoard());
		if (influenceType == BOUZY) {
			inf.bouzyDilate(dilations);
			inf.bouzyErode(erosions);
		} else if (influenceType == ZOBRIST) {
			inf.zobristDilate(dilations);
		}

		for (int p : getAllPointsOnBoard()) {
			if (Math.abs(inf.getValue(p)) <= maxInfluenceToBias) {
				System.err.println(pointToString(p) + " has influence " + inf.getValue(p) + ", so we are adding " + winsToAdd + " wins.");
				getRoot().addWins(p, winsToAdd);
			}
		}
	}

	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-influence-values");
		return result;
	}

	public int getDilations() {
		return dilations;
	}

	public int getErosions() {
		return erosions;
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
		// i.zobristDilate(4);
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
			} else { // v = 0 so color it pink
				cs = "#ff00ff";
			}
			result += String.format("\nCOLOR %s %s\nLABEL %s %d", cs,
					pointToString(p), pointToString(p), v);
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

	public void setDilations(int dilations) {
		this.dilations = dilations;
	}

	public void setErosions(int erosions) {
		this.erosions = erosions;
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("influencetype")) {
			if (value.equals("bouzy")) {
				influenceType = BOUZY;
			} else if (value.equals("zobrist")) {
				influenceType = ZOBRIST;
			} else {
				assert false : "Invalid influence type: use either bouzy or zobrist";
				influenceType = BOUZY; // default if assertions are off
			}
		} else if (property.equals("influencebias")) {
			winsToAdd = Integer.parseInt(value);
		} else if (property.equals("dilations")) {
			dilations = Integer.parseInt(value);
		} else if (property.equals("erosions")) {
			erosions = Integer.parseInt(value);
		} else if (property.equals("maxinfluencetobias")) {
			maxInfluenceToBias = Integer.parseInt(value);
		} else {
			super.setProperty(property, value);
		}
	}
}
