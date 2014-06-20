package edu.lclark.orego.experiment;

import java.io.*;
import java.util.Scanner;

import static edu.lclark.orego.sgf.SgfWriter.*;
import static edu.lclark.orego.ui.Orego.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.experiment.Game.State.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.score.ChineseFinalScorer;
import edu.lclark.orego.score.Scorer;
import edu.lclark.orego.ui.Orego;

/** Allows two independent GTP programs to play a game. */
public final class Game {

	static enum State { REQUESTING_MOVE, SENDING_MOVE, QUITTING, SENDING_TIME_LEFT }

	// TODO Put this in a configuration file
	private static final int GAME_TIME_IN_SECONDS = 600;
	
	/** The amount of time (in nanoseconds) each player has used so far. */
	private final long[] timeUsed;

	/**
	 * The system time (in nanoseconds) the player was asked for a move. This
	 * is used to calculate how much of their time each player has used.
	 */
	private long timeLastMoveWasRequested;

	/** The board on which this game is played. */
	private final Board board;

	/** Shell commands to start the two contestants. */
	private final String[] contestants;

	/** File to which the results of this game are sent. */
	private final String filename;

	/** For scoring games. */
	private final Scorer scorer;
	
	/** State of the program. */
	private State mode;

	/** Prints to the file specified by filename. */
	private PrintWriter out;

	/** Prints to the two program processes. */
	private PrintWriter[] toPrograms;

	/** Color the winning player (BLACK or WHITE). */
	private Color winner;

	/**
	 * Flag to indicate a crashed game. If this is true, don't try to find the
	 * game winner.
	 */
	private boolean crashed;

	/** Which player Orego is. */
	private final StoneColor oregoColor;

	/** System time (in nanoseconds) when the game started. */
	private long starttime;

	/**
	 * @param filename
	 *            File where output should be sent.
	 * @param black
	 *            Shell command to start black player.
	 * @param white
	 *            Shell command to start white player.
	 */
	public Game(String filename, String black, String white) {
		this.filename = filename;
		timeUsed = new long[] { 0, 0 };
		contestants = new String[] { black, white };
		try {
			out = new PrintWriter(filename);
		} catch (Throwable e) {
			out.println("In " + filename + ":");
//			out.println(board);
			e.printStackTrace(out);
			out.flush();
			out.close();
			System.exit(1);
		}
		// TODO Extract board size, komi from Orego command string
		int boardSize = 9;
		double komi = 7.5;
		out.println("(;FF[4]CA[UTF-8]AP[Orego" + VERSION_STRING
				+ "]KM[" + komi + "]GM[1]SZ[" + boardSize + "]");
		out.println("PB[" + black + "]");
		out.println("PW[" + white + "]");
		out.flush();
		if (black.contains("Orego")) {
			oregoColor = BLACK;
		} else {
			oregoColor = WHITE;
		}
		board = new Board(boardSize);
		scorer = new ChineseFinalScorer(board, komi);
		starttime = System.nanoTime();
	}

	/**
	 * Sends the quit command to both contestants, so that the processes will
	 * end.
	 */
	private void endPrograms() {
		out.println(";C[starttime:" + starttime + "]");
		out.println(";C[endtime:" + System.nanoTime() + "]");
		out.println(")");
		out.flush();
		mode = QUITTING;
		for (StoneColor color : StoneColor.values()) {
			toPrograms[color.index()].println("quit");
			toPrograms[color.index()].flush();
		}
		// TODO When do we close the output file?
	}

	/**
	 * Returns the current color to play.
	 */
	private StoneColor getColorToPlay() {
		return board.getColorToPlay();
	}

	// TODO This used to be synchronized. Why?
	/**
	 * Handles a response string (line) from the player of the given color.
	 * 
	 * At the beginning of this method, mode is set to the action to which we
	 * are handling a response. For example, if mode is REQUESTING_MOVE, we are
	 * handling a move returned by a player.
	 */
	private void handleResponse(StoneColor color, String line, Scanner s) {
		if (line.startsWith("=")) {
			if (mode == REQUESTING_MOVE) {
				// Accumulate the time the player spent their total
				timeUsed[getColorToPlay().index()] += System
						.nanoTime() - timeLastMoveWasRequested;
				long timeLeftForThisPlayer = GAME_TIME_IN_SECONDS - timeUsed[getColorToPlay().index()] / 1000000000;
				String timeLeftIndicator = (getColorToPlay() == BLACK ? "BL" : "WL") + "[" + timeLeftForThisPlayer + "]";
				String coordinates = line.substring(line.indexOf(' ') + 1);
				// TODO Make this a field?
				CoordinateSystem coords = board.getCoordinateSystem();
				// Begin SGF output
				if (coordinates.equals("PASS")) {
					out.println((getColorToPlay() == BLACK ? ";B" : ";W")
							+ "[]" + timeLeftIndicator);
					out.flush();
				} else if (coordinates.toLowerCase().equals("resign")) {
					winner = getColorToPlay().opposite();
					out.println(";RE[" + (winner == BLACK ? "B" : "W") + "+R]");
					out.println(";C[moves:" + board.getTurn() + "]");
					out.flush();
					return;
				} else {
					out.println((getColorToPlay() == BLACK ? ";B" : ";W") + "["
							+ toSgf(coords.at(coordinates), coords) + "]" + timeLeftIndicator);
					out.flush();
				}
				// End SGF output
				// TODO Board should probably be able to accept coordinates as a String
				board.play(coords.at(coordinates));
				if (board.getPasses() == 2) {
					out.println(";RE["
							+ (scorer.winner() == BLACK ? "B" : "W") + "+"
							+ Math.abs(scorer.score()) + "]");
					out.println(";C[moves:" + board.getTurn() + "]");
					out.flush();
					return;
				}
				// We are going to send the move now. After the response
				// received, we want to be in a mode indicating what we are
				// finishing. If we aren't tracking game time, pretend we sent
				// the time left to skip that step.
				if (GAME_TIME_IN_SECONDS > 0) {
					mode = SENDING_MOVE;
				} else {
					mode = SENDING_TIME_LEFT;
				}
				// Note the color reversal here, because the color to play has
				// already been switched
				toPrograms[getColorToPlay().index()]
				.println(getColorToPlay().opposite()
						 + " " + coordinates);
				toPrograms[getColorToPlay().index()].flush();
			} else if (mode == SENDING_MOVE) {
				mode = SENDING_TIME_LEFT;
				// Tell the player how much time they have left in the game
				long timeLeftForThisPlayer = GAME_TIME_IN_SECONDS - timeUsed[getColorToPlay().index()] / 1000000000;
				toPrograms[getColorToPlay().index()].println("time_left "
						+ getColorToPlay() + " "
						+ timeLeftForThisPlayer + " 0");
				toPrograms[getColorToPlay().index()].flush();
			} else if (mode == SENDING_TIME_LEFT) {
				// Ignore the player's response to the time_left command.
				// Request a move.
				mode = REQUESTING_MOVE;
				toPrograms[getColorToPlay().index()].println("genmove "
						+ getColorToPlay());
				toPrograms[getColorToPlay().index()].flush();
				timeLastMoveWasRequested = System.nanoTime();
			} else { // Mode is QUITTING
				// Do nothing
			}
		} else {
			if (line.length() > 0) {
				crashed = true;
				out.println("In " + filename + ":");
				out.println(board);
				out.println("Got something other than an acknowledgment: "
						+ line);
				endPrograms();
				while (s.hasNextLine()) {
					out.println(s.nextLine());
				}
				out.flush();
				System.exit(1);
			}
		}
	}

	/**
	 * Plays the game.
	 * 
	 * @return the color of the winning player (BLACK or WHITE), as defined in
	 *         orego.core.Colors.
	 */
	public int play() {
		try {
			winner = -1;
			Process[] programs = new Process[NUMBER_OF_PLAYER_COLORS];
			toPrograms = new PrintWriter[NUMBER_OF_PLAYER_COLORS];
			for (int color = 0; color < NUMBER_OF_PLAYER_COLORS; color++) {
				ProcessBuilder builder = new ProcessBuilder("nohup", "bash",
						"-c", contestants[color], "&");
				builder.redirectErrorStream(true);
				programs[color] = builder.start();
				toPrograms[color] = new PrintWriter(
						programs[color].getOutputStream());
				new Thread(new PlayerListener(color,
						programs[color].getInputStream(), this)).start();
			}
			board.clear();
			// start by telling the first player how much time they have left,
			// which gets the game started (see the handleResponse() method).
			if (GAME_TIME_IN_SECONDS > 0) {
				mode = SENDING_TIME_LEFT;
				int timeLeftInSeconds = GAME_TIME_IN_SECONDS - timeUsed[getColorToPlay()] / 1000;
				toPrograms[getColorToPlay()].println("time_left "
						+ COLOR_NAMES[getColorToPlay()] + " "
						+ timeLeftInSeconds + " 0");
				toPrograms[getColorToPlay()].flush();
			} else {
				mode = REQUESTING_MOVE;
				toPrograms[getColorToPlay()].println("genmove "
						+ COLOR_NAMES[getColorToPlay()]);
				toPrograms[getColorToPlay()].flush();
				timeLastMoveWasRequested = System.nanoTime();
			}
			// Wait for programs to finish
			for (int color = 0; color < NUMBER_OF_PLAYER_COLORS; color++) {
				programs[color].waitFor();
			}
			if (!crashed) {
				if (winner == -1) { // Game not already resolved by resignation
					winner = board.finalWinner();
				}
			}
			for (int c = BLACK; c < NUMBER_OF_PLAYER_COLORS; c++) {
				toPrograms[c].close();
				programs[c].getInputStream().close();
				programs[c].getOutputStream().close();
				programs[c].getErrorStream().close();
				programs[c].destroy();
			}
			out.close();
		} catch (Throwable e) {
			out.println("In " + filename + ":");
			out.println(board);
			e.printStackTrace(out);
			out.flush();
			out.close();
			System.exit(1);
		}
		return winner;
	}
}
