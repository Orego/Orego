package orego.experiment;

import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import orego.core.*;

// TODO It would be nicer if these games were saved as SGF files.
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

	/** Flag to indicate a crashed game. If this is true, don't try to find the game winner. */
	private boolean crashed;
	
	/**
	 * String that is built to produce an SGF file.
	 */
	private String sgfoutput = "";
	
	/**
	 * @param black
	 *            shell command to start black player
	 * @param white
	 *            shell command to start white player
	 */
	public Game(String filename, String black, String white) {
		this.filename = filename;
		contestants = new String[] { black, white };
		sgfoutput += "(;FF[4]CA[UTF-8]AP[OREGOvs.GNUGO]KM[7.5]";
	}

	/**
	 * Sends the quit command to both contestants, so that the processes will
	 * end.
	 */
	protected void endPrograms() {
		out.println("Game over!");
		out.println(sgfoutput+")");
		out.println(board);
		out.println(board.getTurn() + " moves played");
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
		if (line.startsWith("=")) {
			if (mode == REQUESTING_MOVE) {
				String coordinates = line.substring(line.indexOf(' ') + 1);
				out.println(colorToString(getColorToPlay()) + " " + coordinates);
				//sgf output
				sgfoutput += (getColorToPlay() == BLACK ? ";B" : ";W");
				if (coordinates.equals("PASS")) {
					sgfoutput += "[]";
				}
				else if (coordinates.toLowerCase().equals("resign")) {
					//do nothing.
				}
				else {
					sgfoutput += "[" + rowToChar(row(at(coordinates))) + columnToChar(column(at(coordinates))) + "]";
				}
				//end sgf output
				out.flush();
				if (coordinates.toLowerCase().equals("resign")) {
					winner = opposite(getColorToPlay());
					endPrograms();
					return;
				}
				board.play(at(coordinates));
				mode = SENDING_MOVE;
				// Note the color reversal here, because the color to play has
				// already been switched
				toPrograms[getColorToPlay()]
						.println(spellOutColorName(opposite(getColorToPlay()))
								+ " " + coordinates);
				toPrograms[getColorToPlay()].flush();
			} else if (mode == SENDING_MOVE) {
				if (board.getPasses() == 2) {
					endPrograms();
					return;
				} else {
					mode = REQUESTING_MOVE;
					toPrograms[getColorToPlay()].println("genmove "
							+ spellOutColorName(getColorToPlay()));
					toPrograms[getColorToPlay()].flush();
				}
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
			out = new PrintWriter(filename);
			out.println("black: " + contestants[BLACK]);
			out.println("white: " + contestants[WHITE]);
			out.flush();
			winner = -1;
			Process[] programs = new Process[NUMBER_OF_PLAYER_COLORS];
			toPrograms = new PrintWriter[NUMBER_OF_PLAYER_COLORS];
			for (int color = 0; color < NUMBER_OF_PLAYER_COLORS; color++) {
				ProcessBuilder builder = new ProcessBuilder("nohup", "bash",
						"-c", contestants[color], "&");
				builder.redirectErrorStream(true);
				programs[color] = builder.start();
				out.println("Built process");
				toPrograms[color] = new PrintWriter(
						programs[color].getOutputStream());
				new Thread(new PlayerListener(color,
						programs[color].getInputStream(), this)).start();
				out.println("Started process");
			}
			out.flush();
			board = new Board();
			mode = REQUESTING_MOVE;
			toPrograms[BLACK].println("genmove black");
			toPrograms[BLACK].flush();
			// Wait for programs to finish
			out.println("Waiting for programs to finish");
			out.flush();
			for (int color = 0; color < NUMBER_OF_PLAYER_COLORS; color++) {
				programs[color].waitFor();
			}
			if (!crashed) {
				out.println("Programs have finished");
				if (winner == -1) { // Game not already resolved by resignation
					winner = board.finalWinner();
				}
				out.println("Winner: " + winner);
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
