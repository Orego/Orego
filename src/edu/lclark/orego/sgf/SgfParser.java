package edu.lclark.orego.sgf;

import static edu.lclark.orego.core.CoordinateSystem.PASS;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import static java.lang.Integer.MAX_VALUE;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.experiment.Logging;

/** Parses SGF files. */
public final class SgfParser {

	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		final SgfParser parser = new SgfParser(CoordinateSystem.forWidth(19), true);
		final List<List<Short>> games = parser.parseGamesFromFile(new File(
				"sgf-test-files/19/1977-02-27.sgf"), 179);
		for (final List<Short> game : games) {
			for (final Short move : game) {
				System.out.println(parser.coords.toString(move));
			}
		}
	}

	private final CoordinateSystem coords;
	
	private boolean breakOnFirstPass;

	public SgfParser(CoordinateSystem coords, boolean breakOnFirstPass) {
		this.coords = coords;
		this.breakOnFirstPass = breakOnFirstPass;
	}

	/**
	 * Parses moves from a StringTokenizer.
	 * 
	 * @param maxBookDepth
	 *            Only look at this many moves. For no limit, use
	 *            Integer.MAX_VALUE;
	 * @return
	 */
	@SuppressWarnings("boxing")
	private List<Short> parseGame(StringTokenizer stoken, int maxBookDepth) {
		final List<Short> game = new ArrayList<>();
		int turn = 0;
		while (turn <= maxBookDepth) {
			if (!stoken.hasMoreTokens()) {
				return game;
			}
			String token = stoken.nextToken();
			if (token.equals("HA")) {
				// Handicap game, discard it.
				return null;
			} else if (token.equals("SZ")) {
				token = stoken.nextToken();
				final int intToken = Integer.parseInt(token);
				if (intToken != 19) {
					// Game is not the proper size, discard it.
					return null;
				}
			} else if (token.equals("AB") || token.equals("AW")) {
				// Game has added stones, discard it.
				return null;
			} else if (token.equals("PL")) {
				// Game changes color to play, discard it.
				return null;
			} else if (token.equals("B") || token.equals("W")) {
				token = stoken.nextToken();
				final short move = sgfToPoint(token);
				if (maxBookDepth != MAX_VALUE && move == PASS) {
					// Weird early pass when reading for book; discard game
					return null;
				}
				if(breakOnFirstPass && move == PASS){
					return game;
				}
				game.add(move);
				turn++;
			} else if (token.equals(")")) {
				return game;
			}
		}
		return game;
	}

	/**
	 * Parses one game and returns it. Used to read in one game in response to a
	 * GTP command.
	 */
	public List<Short> parseGameFromFile(File file) {
		Logging.log("BEFORE");
		final List<List<Short>> games = parseGamesFromFile(file, MAX_VALUE);
		Logging.log("" + games.size());
		return games.get(0);
	}

	/**
	 * Reads in all games from file and returns them in a list. Each element of the list is a list of moves (shorts).
	 * 
	 * @param maxBookDepth The maximum number of moves to examine in each game, or Integer.MAX_VALUE for no limit.
	 */
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
			System.exit(1);
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
		short result = coords.at(r, c);
		assert coords.isOnBoard(result);
		return result;
	}

}
