package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.SystemConfiguration.*;

import java.io.*;
import java.util.*;

/** Collates data during or after an experiment. */
public final class Collate {

	private int[] oregoWins;

	private int[] runs;

	private int[] timeLosses;

	private int[] totalMoves;

	/** Orego command-line arguments given in all conditions. */
	private String always;
	
	private Map<String, String> conditions;

	public void collate() {
		File folder = new File(SYSTEM.resultsDirectory);
		collate(folder);
	}

	public void collate(File file) {
		File mostRecent = file;
		do {
			file = mostRecent;
			File[] files = file.listFiles();
			mostRecent = file.listFiles()[0];
			for (File f : files) {
				if (f.getPath().compareTo(mostRecent.getPath()) > 0) {
					mostRecent = f;
				}
			}
		} while (mostRecent.isDirectory());
		getConditions(file);
		produceSummary(file);
	}

	public void getConditions(File folder) {
		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(folder + File.separator + "experiment.txt"));
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		conditions = new TreeMap<>();
		for (final String s : properties.stringPropertyNames()) {
			if (s.startsWith("condition")) {
				conditions.put(s, (String) properties.get(s));
			} else if (s.equals("always")) {
				always = (String) properties.get(s);
			}
		}
		int n = conditions.size();
		runs = new int[n];
		oregoWins = new int[n];
		timeLosses = new int[n];
		totalMoves = new int[n];
	}

	private void produceSummary(File folder) {
		for (File file : folder.listFiles()) {
			if (file.getPath().endsWith(".sgf")) {
				extractData(file);
			}
		}
		try (PrintWriter writer = new PrintWriter(new File(folder
				+ File.separator + "summary.txt"))) {
			int i = 0;
			for (String conditionName : conditions.keySet()) {
				output(writer, conditionName + ": " + conditions.get(conditionName));
				output(writer, "Orego win rate: "
						+ ((float) oregoWins[i] / (float) runs[i]) + " (" + oregoWins[i] + "/" + runs[i] + ")");
				output(writer, "Average moves per game: "
						+ ((float) totalMoves[i] / (float) runs[i]));
				output(writer, "Games out of time: " + timeLosses[i]);
				output(writer, "\n");
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** Prints s both to writer and standard output. */
	private static void output(PrintWriter writer, String s) {
		writer.println(s);
		System.out.println(s);
	}

	private void extractData(File file) {
		char oregoColor = ' ';
		String input = "";
		int condition = -1;
		try (Scanner s = new Scanner(file)) {
			while (s.hasNextLine()) {
				input += s.nextLine();
			}
			StringTokenizer stoken = new StringTokenizer(input, "()[];");
			boolean gameCompleted = false;
			while (stoken.hasMoreTokens()) {
				String token = stoken.nextToken();
				if (token.equals("PB")) { // If the player is black
					token = stoken.nextToken();
					if (token.contains("Orego")) {
						oregoColor = 'B';
						condition = getConditionIndex(token);
					}
				}
				if (token.equals("PW")) { // If the player is white
					token = stoken.nextToken();
					if (token.contains("Orego")) {
						oregoColor = 'W';
						condition = getConditionIndex(token);
					}
				}
				if (token.equals("RE")) { // Find the winner
					token = stoken.nextToken();
					if (token.contains("Time")) {
						timeLosses[condition]++;
					}
					if (token.charAt(0) == oregoColor) {
						oregoWins[condition]++;
					}
					gameCompleted = true;
					runs[condition]++;
				}
				if (token.equals("C")) {
					token = stoken.nextToken();
					if (gameCompleted && token.contains("moves")) {
						totalMoves[condition] += (Long.parseLong(token
								.substring(6)));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private int getConditionIndex(String token) {
		String condition = token.substring(token.indexOf(always) + always.length());
		int i = 0;
		for (String conditionName : conditions.keySet()) {
			if (condition.equals(conditions.get(conditionName))) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public static void main(String[] args) {
		new Collate().collate();

	}

}
