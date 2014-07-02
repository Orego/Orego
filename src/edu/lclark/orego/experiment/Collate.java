package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.SystemConfiguration.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

/** Collates data during or after an experiment. */
public final class Collate {

	private int[] oregoWins;

	private int[] runs;

	private int[] timeLosses;

	private int[] totalMoves;

	private int[] fileCount;

	private String[] conditions;

	public void collate() {
		File folder = new File(SYSTEM.resultsDirectory);
		collate(folder);
	}

	public void collate(File file) {

		File[] files = file.listFiles();
		if (files != null) {
			File mostRecent = file.listFiles()[0];
			for (File f : files) {
				if (file.getPath().compareTo(mostRecent.getPath()) > 0) {
					mostRecent = f;
				}
			}
			collate(mostRecent);
		} else {
			getConditions(file);
			produceSummary(file);
		}
	}

	public void getConditions(File folder) {
		ArrayList<String> conditionList = new ArrayList<>();
		File properties = new File(folder + File.separator + "experiment.txt");
		try (Scanner s = new Scanner(properties)) {
			while (s.hasNextLine()) {
				String nextLine = s.nextLine();
				if (nextLine.contains("condition")) {
					int j = nextLine.indexOf('=');
					if (j > 0) {
						conditionList.add(nextLine.substring(j + 1));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		int conditionCount = conditionList.size();
		runs = new int[conditionCount];
		oregoWins = new int[conditionCount];
		timeLosses = new int[conditionCount];
		totalMoves = new int[conditionCount];
		fileCount = new int[conditionCount];
		conditions = new String[conditionCount];
		conditions = conditionList.toArray(conditions);
	}

	private void produceSummary(File folder) {
		for (File file : folder.listFiles()) {
			if (file.getPath().endsWith(".sgf")) {
				extractData(file);
			}
		}
		try (PrintWriter writer = new PrintWriter(new File(folder
				+ File.separator + "summary.txt"))) {
			for (int i = 0; i < conditions.length; i++) {
				output(writer, "Condition: " + conditions[i]);
				output(writer, "Total games played: " + runs[i]);
				output(writer, "Orego win rate: "
						+ ((float) oregoWins[i] / (float) runs[i]));
				output(writer, "Average moves per game: "
						+ ((float) totalMoves[i] / (float) fileCount[i]));
				output(writer, "Games out of time: " + timeLosses[i]);
				output(writer, "\n");
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
			while (stoken.hasMoreTokens()) {
				String token = stoken.nextToken();
				if (token.equals("PB")) { // If the player is black
					token = stoken.nextToken();
					if (token.contains("Orego")) {
						oregoColor = 'B';
						for (int i = 0; i < conditions.length; i++) {
							if (token.contains(conditions[i])) {
								condition = i;
							}
						}
					}
				}
				if (token.equals("PW")) { // If the player is white
					token = stoken.nextToken();
					if (token.contains("Orego")) {
						oregoColor = 'W';
						for (int i = 0; i < conditions.length; i++) {
							if (token.contains(conditions[i])) {
								condition = i;
							}
						}
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
					runs[condition]++;
				}
				if (token.equals("C")) {
					token = stoken.nextToken();
					if (token.contains("moves")) {
						totalMoves[condition] += (Long.parseLong(token
								.substring(6)));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		fileCount[condition]++;
	}

	public static void main(String[] args) {
		new Collate().collate();

	}

}
