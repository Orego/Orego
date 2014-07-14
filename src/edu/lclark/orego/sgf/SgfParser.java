package edu.lclark.orego.sgf;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.CoordinateSystem.RESIGN;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import edu.lclark.orego.core.CoordinateSystem;

public final class SgfParser {

	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		final SgfParser parser = new SgfParser(CoordinateSystem.forWidth(19));
		final List<List<Short>> games = parser.parseGamesFromFile(new File(
				"sgf-test-files/19/1977-02-27.sgf"), 179);
		for (final List<Short> game : games) {
			for (final Short move : game) {
				System.out.println(parser.coords.toString(move));
			}
		}
	}

	private final CoordinateSystem coords;

	public SgfParser(CoordinateSystem coords) {
		this.coords = coords;
	}

	@SuppressWarnings("boxing")
	protected List<Short> parseGame(StringTokenizer stoken, int maxBookDepth) {
		final List<Short> game = new ArrayList<>();
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
				final int intToken = Integer.parseInt(token);
				if (intToken != 19) {
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
				final short move = sgfToPoint(token);
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

	public List<Short> parseGameFromFile(File file) {
		String input = "";
		try (Scanner s = new Scanner(file)) {
			while (s.hasNextLine()) {
				input += s.nextLine();
			}
			input = input.replace("W[]", "W[tt]");
			input = input.replace("B[]", "B[tt]");
			final StringTokenizer stoken = new StringTokenizer(input, ")[];");
			while (stoken.hasMoreTokens()) {
				final String token = stoken.nextToken();
				if (token.equals("(")) {
					final List<Short> game = parseGame(stoken, 500);
					if (game != null) {
						return game;
					}
				}
			}
		} catch (final FileNotFoundException e) {
			System.err.println("File not found!");
			e.printStackTrace();
		}
		return null;
	}

	public List<List<Short>> parseGamesFromFile(File file, int maxBookDepth) {
		final List<List<Short>> games = new ArrayList<>();
		String input = "";
		try (Scanner s = new Scanner(file)) {
			while (s.hasNextLine()) {
				input += s.nextLine();
			}
			input = input.replace("W[]", "W[tt]");
			input = input.replace("B[]", "B[tt]");
			final StringTokenizer stoken = new StringTokenizer(input, ")[];");
			while (stoken.hasMoreTokens()) {
				final String token = stoken.nextToken();
				if (token.equals("(")) {
					final List<Short> game = parseGame(stoken, maxBookDepth);
					if (game != null) {
						games.add(game);
					}
				}
			}
			return games;
		} catch (final FileNotFoundException e) {
			System.err.println("File not found!");
			e.printStackTrace();
		}
		return null;
	}

	/** Returns the point represented by an sgf String. */
	public short sgfToPoint(String label) {
		if (label.equals("tt")) {
			return PASS;
		}
		final int c = label.charAt(0) - 'a';
		final int r = label.charAt(1) - 'a';
		return coords.at(r, c);
	}
}
