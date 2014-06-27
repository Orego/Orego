package edu.lclark.orego.sgf;

import static edu.lclark.orego.core.CoordinateSystem.PASS;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.CoordinateSystem.*;

public final class SgfParser {

	private CoordinateSystem coords;

	public SgfParser(CoordinateSystem coords) {
		this.coords = coords;
	}

	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		SgfParser parser = new SgfParser(CoordinateSystem.forWidth(19));
		List<List<Short>> games = parser.parseGamesFromFile(new File(
				"SgfTestFiles/19/1977-02-27.sgf"), 179);
		for (List<Short> game : games) {
			for (Short move : game) {
				System.out.println(parser.coords.toString(move));
			}
		}
	}

	public List<List<Short>> parseGamesFromFile(File file, int maxBookDepth) {
		List<List<Short>> games = new ArrayList<>();
		String input = "";
		try (Scanner s = new Scanner(file)) {
			while (s.hasNextLine()) {
				input += s.nextLine();
			}
			input = input.replace("W[]", "W[tt]");
			input = input.replace("B[]", "B[tt]");
			StringTokenizer stoken = new StringTokenizer(input, ")[];");
			while (stoken.hasMoreTokens()) {
				String token = stoken.nextToken();
				if (token.equals("(")) {
					List<Short> game = parseGame(stoken, maxBookDepth);
					if (game != null) {
						games.add(game);
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
	
	@SuppressWarnings("boxing")
	protected List<Short> parseGame(StringTokenizer stoken, int maxBookDepth) {
		List<Short> game = new ArrayList<>();
		int turn = 0;
		while (turn <= maxBookDepth) {
			if (!stoken.hasMoreTokens()) {
				return game;
			}
			String token = stoken.nextToken();
			if (token.equals("HA")) {
				// Handicap game, ignore.
				return null;
			} else if (token.equals("SZ")) {
				token = stoken.nextToken();
				int intToken = Integer.parseInt(token);
				if (intToken != 19) {
					// TODO Should we change the coordinate system here and not
					// die if the size is not 19?
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
				short move = sgfToPoint(token);
				if (move > RESIGN) { // Also excludes NO_POINT and PASS
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
	public short sgfToPoint(String label) {
		if (label.equals("tt")) {
			return PASS;
		}
		int c = label.charAt(0) - 'a';
		int r = label.charAt(1) - 'a';
		return coords.at(r, c);
	}
}
