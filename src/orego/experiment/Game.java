package orego.experiment;

import java.io.*;
import java.util.Scanner;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import orego.core.*;
import orego.ui.Orego;
import static orego.experiment.ExperimentConfiguration.*;

/** Allows two independent GTP programs to play a game. */
public class Game {

	/** Mode where this game is quitting. */
	public static final int QUITTING = 2;

	/** Mode where this game is requesting a move from one of the players. */
	public static final int REQUESTING_MOVE = 0;

	/**
	 * Mode where this game is sending a move, played by one player, to both
	 * players.
	 */
	public static final int SENDING_MOVE = 1;

	/**
	 * Mode where this game is sending the time_left command to the player who
	 * is about to play.
	 */
	public static final int SENDING_TIME_LEFT = 3;

	/** The amount of time each player has used so far. */
	private int[] timeUsedInMilliseconds;

	/**
	 * The system time (in milliseconds) the player was asked for a move. This
	 * is used to calculate how much of their time each player has used.
	 */
	private long timeLastMoveWasRequested;

	/** The board on which this game is played. */
	private Board board;

	/** Shell commands to start the two contestants. */
	private String[] contestants;

	/** File to which the results of this game are sent. */
	private String filename;

	/** One of QUITTING, REQUESTING MOVE, or SENDING_MOVE. */
	private int mode;

	/** Prints to the file specified by filename. */
	private PrintWriter out;

	/** Prints to the two program processes. */
	public PrintWriter[] toPrograms;

	/** Number of the winning player (BLACK or WHITE). */
	private int winner;

	/**
	 * Flag to indicate a crashed game. If this is true, don't try to find the
	 * game winner.
	 */
	private boolean crashed;

	/** int containing whether Orego is black or white */
	private int oregoColor;

	/** long that has the start of the game. */
	private long starttime;

	/**
	 * @param black
	 *            shell command to start black player
	 * @param white
	 *            shell command to start white player
	 */
	public Game(String filename, String black, String white) {
		try {
			timeUsedInMilliseconds = new int[2];
			this.filename = filename;
			out = new PrintWriter(filename);
			contestants = new String[] { black, white };
			out.println("(;FF[4]CA[UTF-8]AP[Orego"+Orego.VERSION_STRING+"]KM[7.5]GM[1]SZ["+getBoardWidth()+"]");
			out.println("PB["+black+"]");
			out.println("PW["+white+"]");
			if (black.contains("Orego")) {
				oregoColor = orego.core.Colors.BLACK;
			} else {
				oregoColor = orego.core.Colors.WHITE;
			}
			out.flush();
			starttime = System.currentTimeMillis();
		} catch (Throwable e) {
			out.println("In " + filename + ":");
			out.println(board);
			e.printStackTrace(out);
			out.flush();
			out.close();
			System.exit(1);
		}

	}

	/**
	 * Sends the quit command to both contestants, so that the processes will
	 * end.
	 */
	protected void endPrograms() {
		out.println(";C[starttime:" + starttime + "]");
		out.flush();
		out.println(";C[endtime:" + System.currentTimeMillis() + "]");
		out.flush();
		out.println(")");
		out.flush();
		mode = QUITTING;
		for (int color = 0; color < NUMBER_OF_PLAYER_COLORS; color++) {
			toPrograms[color].println("quit");
			toPrograms[color].flush();
		}
	}

	/**
	 * Returns the current color to play.
	 */
	public int getColorToPlay() {
		return board.getColorToPlay();
	}

	/**
	 * Handles a response string (line) from the player of the given color.
	 */
	protected synchronized void handleResponse(int color, String line, Scanner s) {
		if (color == oregoColor && line.contains("playout")) {
			out.println(";C[" + line.substring(line.indexOf(' ') + 1) + "]");
			out.flush();
			endPrograms();
			return;
		}
		if (line.startsWith("=")) {
			if (mode == REQUESTING_MOVE) {
				// accumulate the time the player spent their total
				timeUsedInMilliseconds[getColorToPlay()] += System
						.currentTimeMillis() - timeLastMoveWasRequested;
				String coordinates = line.substring(line.indexOf(' ') + 1);
				// sgf output
				if (coordinates.equals("PASS")) {
					out.println((getColorToPlay() == BLACK ? ";B" : ";W")
							+ "[]");
					out.flush();
				}
				else if (coordinates.toLowerCase().equals("resign")) {
					//do nothing.
				}
				else {
					out.println((getColorToPlay() == BLACK ? ";B" : ";W")+"[" + rowToSgfChar(row(at(coordinates))) + columnToSgfChar(column(at(coordinates))) + "]");
					out.flush();
				}
				// end sgf output
				if (coordinates.toLowerCase().equals("resign")) {
					winner = opposite(getColorToPlay());
					out.println(";RE[" + (winner == BLACK ? "B" : "W") + "+R]");
					out.flush();
					out.println(";C[moves:" + board.getTurn() + "]");
					out.flush();
					toPrograms[oregoColor].println("playout_count");
					toPrograms[oregoColor].flush();
					return;
				}
				board.play(at(coordinates));
				mode = SENDING_MOVE;
				// Note the color reversal here, because the color to play has
				// already been switched
				toPrograms[getColorToPlay()]
				.println(COLOR_NAMES[opposite(getColorToPlay())]
						 + " " + coordinates);
				toPrograms[getColorToPlay()].flush();
			} else if (mode == SENDING_MOVE) {
				if (board.getPasses() == 2) {
					out.println(";RE["
							+ (board.finalWinner() == BLACK ? "B" : "W") + "+"
							+ Math.abs(board.finalScore()) + "]");
					out.flush();
					out.println(";C[moves:" + board.getTurn() + "]");
					out.flush();
					toPrograms[oregoColor].println("playout_count");
					toPrograms[oregoColor].flush();
					return;
				} else {
					mode = SENDING_TIME_LEFT;
					// tell the player how much time they have left in the game
					int timeLeftInSeconds = GAME_TIME_IN_SECONDS
							- timeUsedInMilliseconds[getColorToPlay()] / 1000;
					toPrograms[getColorToPlay()].println("time_left "
							+ COLOR_NAMES[getColorToPlay()] + " "
							+ timeLeftInSeconds + " 0");
					toPrograms[getColorToPlay()].flush();
				}
			} else if (mode == SENDING_TIME_LEFT) {
				// ignore the player's response to the time_left command.
				// request a move.
				mode = REQUESTING_MOVE;
				toPrograms[getColorToPlay()].println("genmove "
						+ COLOR_NAMES[getColorToPlay()]);
				toPrograms[getColorToPlay()].flush();
				timeLastMoveWasRequested = System.currentTimeMillis();
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
			board = new Board();
			// start by telling the first player how much time they have left,
			// which gets the game started (see the handleResponse() method).
			mode = SENDING_TIME_LEFT;
			int timeLeftInSeconds = GAME_TIME_IN_SECONDS - timeUsedInMilliseconds[getColorToPlay()] / 1000;
			toPrograms[getColorToPlay()].println("time_left "
					+ COLOR_NAMES[getColorToPlay()] + " "
					+ timeLeftInSeconds + " 0");
			toPrograms[getColorToPlay()].flush();
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
