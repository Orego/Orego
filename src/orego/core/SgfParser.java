package orego.core;

import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.getBoardWidth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.StringTokenizer;

public class SgfParser {

	/**
	 * Builds board from the given sgf file. AB and AW commands are handled using
	 * placeInitialStone(), while B and W commands are handled using play().
	 * 
	 * @param filepath
	 * 				the name of the sgf file.
	 * @param board
	 * 				the board which the sgf file will be converted to.
	 */
	public static Board sgfToBoard(String filepath) {
		if (!filepath.toLowerCase().endsWith(".sgf")) {
			System.err.println(filepath + " is not an sgf file!");
		} else {
			File file = new File(filepath);
			return sgfToBoard(file);
		}
		return null;
	}
	
	public static Board sgfToBoard(File file) {
		Board board = new Board();
		String input = "";
		try {
			Scanner s;
			s = new Scanner(file);
			while (s.hasNextLine()) {
				input += s.nextLine();
			}
			input = input.replace("W[]", "W[tt]");
			input = input.replace("B[]", "B[tt]");
			StringTokenizer stoken = new StringTokenizer(input, "()[];");
			int addStoneState = 0;
			// Ignores AE
			while (stoken.hasMoreTokens()) {
				String token = stoken.nextToken();
				if (token.equals("AW")) {
					addStoneState = 1;
					token = stoken.nextToken();
				} else if (token.equals("AB")) {
					addStoneState = 2;
					token = stoken.nextToken();
				} else if (token.equals("W") || token.equals("B")) {
					addStoneState = 0;
					token = stoken.nextToken();
					if (token.equals("tt")) {
						board.play(PASS);
					} else {
						board.play(sgfToPoint(token));
					}
				}
				if(token.charAt(0) >= 'a') {						
					if (addStoneState == 1) {
						board.placeInitialStone(Colors.WHITE, sgfToPoint(token));
					} else if (addStoneState == 2) {
						board.placeInitialStone(Colors.BLACK, sgfToPoint(token));
					}
				} else {
					addStoneState = 0;
				}
			}
			s.close();
		} catch (FileNotFoundException e) {
			System.err.println("File not found!");
			e.printStackTrace();
		}
		return board;
	}
	
	/** Returns the point represented by an sgf String. */
	public static int sgfToPoint(String label) {
		int c = label.charAt(0) - 'a';
		int r = label.charAt(1) - 'a';
		return Coordinates.at(r, c);
	}

}
