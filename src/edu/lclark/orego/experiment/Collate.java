package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Collates data during or after an experiment. The most recent (possibly
 * nested) subdirectory of the results directory specified in system.properties
 * is used. Data are gathered from there. Results are both printed to stdout and
 * written to a file summary.txt in that subdirectory.
 */
public final class Collate {

	public static void main(String[] args) {
		new Collate().collate(new File(SYSTEM.resultsDirectory));
	}

	/** Prints s both to writer and standard output. */
	private static void output(PrintWriter writer, String s) {
		writer.println(s);
		System.out.println(s);
	}

	/** Orego command-line arguments given in all conditions. */
	private String always;

	/**
	 * Maps conditions names (e.g., "condition3") to Orego command-line option
	 * strings.
	 */
	private Map<String, String> conditions;

	/** Number of games Orego won in each condition. */
	private int[] oregoWins;

	/** Number of games played in each condition. */
	private int[] runs;

	/** Number of ties in each condition. */
	private int[] ties;

	/** Number of games the opponent lost on time in each condition. */
	private int[] timeLossesOpponent;

	/** Number of games Orego lost on time in each condition. */
	private int[] timeLossesOrego;

	/** Total number of moves made in each condition. */
	private int[] totalMoves;

	/**
	 * Gathers data from the most recent subdirectory (possibly nested) within
	 * directory and writes output to both summary.txt (in that subdirectory)
	 * and stdout.
	 */
	private void collate(File directory) {
		File mostRecent = directory;
		do {
			directory = mostRecent;
			final File[] files = directory.listFiles();
			mostRecent = directory.listFiles()[0];
			for (final File f : files) {
				if (f.getPath().compareTo(mostRecent.getPath()) > 0) {
					mostRecent = f;
				}
			}
		} while (mostRecent.isDirectory());
		prepare(directory);
		produceSummary(directory);
	}

	/** Extracts data from an SGF file and updates fields. */
	private void extractData(File file) {
		char oregoColor = ' ';
		String input = "";
		int condition = -1;
		try (Scanner s = new Scanner(file)) {
			while (s.hasNextLine()) {
				input += s.nextLine();
			}
			final StringTokenizer stoken = new StringTokenizer(input, "()[];");
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
						if (token.contains("B") && oregoColor == 'B'
								|| token.contains("W") && oregoColor == 'W') {
							timeLossesOrego[condition]++;
						} else if (token.contains("W") && oregoColor != 'W'
								|| token.contains("B") && oregoColor != 'B') {
							timeLossesOpponent[condition]++;
						}
					}
					if (token.charAt(0) == '0') {
						ties[condition]++;
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
						totalMoves[condition] += Long.parseLong(token
								.substring(6));
					}
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** Returns the index of the condition with name. */
	private int getConditionIndex(String name) {
		final String condition = name.substring(name.indexOf(always)
				+ always.length() + 1);
		int i = 0;
		for (final String conditionName : conditions.keySet()) {
			if (condition.equals(conditions.get(conditionName))) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Reads conditions and initializes fields to begin reading SGF files from
	 * folder.
	 */
	private void prepare(File folder) {
		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(folder + File.separator
					+ "experiment.txt"));
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		conditions = new TreeMap<>();
		for (final String s : properties.stringPropertyNames()) {
			if (s.startsWith("condition")) {
				System.out.println("Mapping <" + s + "> to <" + properties.get(s));
				conditions.put(s, (String) properties.get(s));
			} else if (s.equals("always")) {
				always = (String) properties.get(s);
			}
		}
		final int n = conditions.size();
		runs = new int[n];
		oregoWins = new int[n];
		timeLossesOrego = new int[n];
		timeLossesOpponent = new int[n];
		totalMoves = new int[n];
		ties = new int[n];
	}

	/** Writes summary information to summary.txt in folder and to stdout. */
	private void produceSummary(File folder) {
		for (final File file : folder.listFiles()) {
			if (file.getPath().endsWith(".sgf")) {
				extractData(file);
			}
		}
		try (PrintWriter writer = new PrintWriter(new File(folder
				+ File.separator + "summary.txt"))) {
			int i = 0;
			for (final String conditionName : conditions.keySet()) {
				output(writer,
						conditionName + ": " + conditions.get(conditionName));
				output(writer, "Orego win rate: " + (float) oregoWins[i]
						/ (float) runs[i] + " (" + oregoWins[i] + "/" + runs[i]
						+ ")");
				output(writer, "Average moves per game: "
						+ (float) totalMoves[i] / (float) runs[i]);
				output(writer, "Games Orego lost on time: "
						+ timeLossesOrego[i]);
				output(writer, "Games opponent lost on time: "
						+ timeLossesOpponent[i]);
				output(writer, "Tied games: " + ties[i]);
				output(writer, "\n");
				i++;
			}
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
