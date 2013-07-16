package orego.sgf;

import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.NO_POINT;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;

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
	
	/**
	 * Builds board from the given sgf file. AB and AW commands are handled using
	 * placeInitialStone(), while B and W commands are handled using play().
	 * 
	 * @param file
	 * 				the sgf file.
	 * @param board
	 * 				the board which the sgf file will be converted to.
	 */
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
			StringTokenizer stoken = new StringTokenizer(input, ")[];");
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
	
	public static List<List<Integer>> sgfToBookGames(File file, int maxBookDepth) {
		List<List<Integer>> games = new ArrayList<List<Integer>>();
		String input = "";
		try{
			Scanner s = new Scanner(file);
			while (s.hasNextLine()) {
				input += s.nextLine();
			}
			s.close();
			input = input.replace("W[]", "W[tt]");
			input = input.replace("B[]", "B[tt]");
			StringTokenizer stoken = new StringTokenizer(input, ")[];");
			while(stoken.hasMoreTokens()) {
				String token = stoken.nextToken();
				if (token.equals("(")) {
					List<Integer> game = sgfToBookGame(stoken, maxBookDepth);
					if(game != null) {
						games.add(game);
					}else{
					}
				}
			}
			return games;
		} catch (FileNotFoundException e) {
			System.err.println("File not found!");
			e.printStackTrace();
		}
		return null;
	}
	
	protected static List<Integer> sgfToBookGame(StringTokenizer stoken, int maxBookDepth) {
		List<Integer> game = new ArrayList<Integer>();
		int turn = 0;
		while (turn <= maxBookDepth) {
			if(!stoken.hasMoreTokens()){
				return null;
			}
			String token = stoken.nextToken();
			if (token.equals("HA")) {
				// Handicap game, ignore.
				return null;
			} else if (token.equals("SZ")) {
				token = stoken.nextToken();
				int intToken = Integer.parseInt(token);
				if (intToken!=Coordinates.getBoardWidth()) {
					// Game is not the proper size, ignore.
					return null;
				}
			} else if (token.equals("AB") || token.equals("AW")) {
				// Game has added stones, ignore.
				return null;
			} else if (token.equals("PL")) {
				// Game changes color to play, ignore.
				return null;
			} else if (token.equals("B") || token.equals("W")) {
				token = stoken.nextToken();
				int move = sgfToPoint(token);
				if(move > NO_POINT) { // Also excludes PASS.
					game.add(move);
					turn++;
				} else {
					// Game has an early pass or bogus move, ignore.
					return null;
				}
			} else if (token.equals(")")) {
				return game;
			}
		}
		return game;
	}
	
	/** Returns the point represented by an sgf String. */
	public static int sgfToPoint(String label) {
		if(label.equals("tt")) {
			return PASS;
		}
		int c = label.charAt(0) - 'a';
		int r = label.charAt(1) - 'a';
		return Coordinates.at(r, c);
	}

}
