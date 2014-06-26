package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.SystemConfiguration.*;
import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;

public final class Collator {

	private int oregoWins;

	private int runs;

	private int timeLosses;

	private int totalMoves;

	private int fileCount;

	public void collate() {
		File folder = new File(SYSTEM.resultsDirectory);
		if (folder.exists())
		{
			File[] files = folder.listFiles();
			if (files != null) {
				File mostRecent = folder.listFiles()[0];
				for (File file : folder.listFiles()) {
					if (file.getPath().compareTo(mostRecent.getPath()) > 0) {
						mostRecent = file;
					}
				}
				collate(mostRecent.getPath());
			}
		}
	}
	
	public void collate(String filePath){
		produceSummary(new File(filePath));
	}

	private void produceSummary(File folder) {
		for (File file : folder.listFiles()) {
			if (file.getPath().endsWith(".sgf")) {
				extractData(file);
				fileCount++;
			}
		}
		try(PrintWriter writer = new PrintWriter(new File(folder + File.separator + "summary.txt"))){
			writer.println("Total games played: " + runs);
			writer.println("Orego win rate: " + ((float)oregoWins / (float)runs));
			writer.println("Average moves per game: " + ((float)totalMoves / (float)fileCount));
			writer.println("Games out of time: " + timeLosses);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void extractData(File file) {
		char oregoColor = ' ';
		String input = "";
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
					}
				}
				if (token.equals("PW")) { // If the player is white
					token = stoken.nextToken();
					if (token.contains("Orego")) {
						oregoColor = 'W';
					}
				}
				if (token.equals("RE")) { // Find the winner
					token = stoken.nextToken();
					if(token.contains("Time")){
						timeLosses++;
					}
					if (token.charAt(0) == oregoColor) {
						oregoWins++;
					}
					runs++;
				}
				if (token.equals("C")) {
					token = stoken.nextToken();
					if (token.contains("moves")) {
						totalMoves += (Long.parseLong(token.substring(6)));
					}
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		new Collator().collate();

	}

}
