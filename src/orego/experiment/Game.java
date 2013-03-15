package orego.experiment;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import orego.core.*;
import orego.ui.Orego;

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
	
	/** Shell commands to start the two playersShellCommands. */
	private String[] playersShellCommands;
	
	/** File to which the results of this game are sent. */
	private String sgfGameLogBetweenPlayersFilename;
	
	/** One of QUITTING, REQUESTING MOVE, or SENDING_MOVE. */
	private int mode;
	
	/** Prints to the file specified by sgfGameLogBetweenPlayersFilename. */
	private PrintWriter sgfGameLogBetweenPlayers;
	
	/** Prints to the two program processes. */
	public PrintWriter[] playerProgramsSTDIN;
	
	/** Number of the winning player (BLACK or WHITE). */
	private int winner;
	
	/** Flag to indicate a crashed game. If this is true, don't try to find the game winner. */
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
	public Game(String sgfLogFilename, String black, String white) {
		try {
			this.sgfGameLogBetweenPlayersFilename = sgfLogFilename;
			sgfGameLogBetweenPlayers = new PrintWriter(sgfLogFilename);
			
			playersShellCommands = new String[] { black, white };
			
			sgfGameLogBetweenPlayers.println("(;FF[4]CA[UTF-8]AP[Orego"+Orego.VERSION_STRING+"]KM[7.5]GM[1]SZ["+Coordinates.BOARD_WIDTH+"]");
			sgfGameLogBetweenPlayers.println("PB["+black+"]");
			sgfGameLogBetweenPlayers.println("PW["+white+"]");
			
			if (black.contains("Orego")) {
				oregoColor = orego.core.Colors.BLACK;
			}
			else {
				oregoColor = orego.core.Colors.WHITE;
			}
			
			sgfGameLogBetweenPlayers.flush();
			starttime = System.currentTimeMillis();
		} catch (Throwable e) {
			sgfGameLogBetweenPlayers.println("In " + sgfLogFilename + ":");
			sgfGameLogBetweenPlayers.println(board);
			e.printStackTrace(sgfGameLogBetweenPlayers);
			sgfGameLogBetweenPlayers.flush();
			sgfGameLogBetweenPlayers.close();
			System.exit(1);
		}
		
	}
	
	/**
	 * Sends the quit command to both playersShellCommands, so that the processes will
	 * end.
	 */
	protected void endPrograms() {
		
		sgfGameLogBetweenPlayers.println(";C[starttime:"+starttime+"]");
		sgfGameLogBetweenPlayers.flush();
		sgfGameLogBetweenPlayers.println(";C[endtime:"+System.currentTimeMillis()+"]");
		sgfGameLogBetweenPlayers.flush();
		sgfGameLogBetweenPlayers.println(")");
		sgfGameLogBetweenPlayers.flush();
		
		mode = QUITTING;
		for (int color = 0; color < NUMBER_OF_PLAYER_COLORS; color++) {
			playerProgramsSTDIN[color].println("quit");
			playerProgramsSTDIN[color].flush();
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
		if (color == oregoColor && line.contains("playout")){
			sgfGameLogBetweenPlayers.println(";C["+line.substring(line.indexOf(' ') + 1)+"]");
			sgfGameLogBetweenPlayers.flush();
			endPrograms();
			return;
		}
		
		if (line.startsWith("=")) {
			if (mode == REQUESTING_MOVE) {
				String coordinates = line.substring(line.indexOf(' ') + 1);
				//sgf output
				if (coordinates.equals("PASS")) {
					sgfGameLogBetweenPlayers.println((getColorToPlay() == BLACK ? ";B" : ";W")+"[]");
					sgfGameLogBetweenPlayers.flush();
				}
				else if (coordinates.toLowerCase().equals("resign")) {
					//do nothing.
				}
				else {
					sgfGameLogBetweenPlayers.println((getColorToPlay() == BLACK ? ";B" : ";W")+"[" + rowToChar(row(at(coordinates))) + columnToChar(column(at(coordinates))) + "]");
					sgfGameLogBetweenPlayers.flush();
				}
				//end sgf output
				if (coordinates.toLowerCase().equals("resign")) {
					winner = opposite(getColorToPlay());
					sgfGameLogBetweenPlayers.println(";RE["+ (winner == BLACK ? "B" : "W") + "+R]");
					sgfGameLogBetweenPlayers.flush();
					sgfGameLogBetweenPlayers.println(";C[moves:"+board.getTurn()+"]");
					sgfGameLogBetweenPlayers.flush();
					playerProgramsSTDIN[oregoColor].println("playout_count");
					playerProgramsSTDIN[oregoColor].flush();
					return;
				}
				board.play(at(coordinates));
				mode = SENDING_MOVE;
				// Note the color reversal here, because the color to play has
				// already been switched
				playerProgramsSTDIN[getColorToPlay()]
				.println(spellOutColorName(opposite(getColorToPlay()))
						 + " " + coordinates);
				playerProgramsSTDIN[getColorToPlay()].flush();
			} else if (mode == SENDING_MOVE) {
				if (board.getPasses() == 2) {
					sgfGameLogBetweenPlayers.println(";RE[" + (board.finalWinner() == BLACK ? "B" : "W") +"+"+Math.abs(board.finalScore())+"]");
					sgfGameLogBetweenPlayers.flush();
					sgfGameLogBetweenPlayers.println(";C[moves:"+board.getTurn()+"]");
					sgfGameLogBetweenPlayers.flush();
					playerProgramsSTDIN[oregoColor].println("playout_count");
					playerProgramsSTDIN[oregoColor].flush();
					return;
				} else {
					mode = REQUESTING_MOVE;
					playerProgramsSTDIN[getColorToPlay()].println("genmove "
														 + spellOutColorName(getColorToPlay()));
					playerProgramsSTDIN[getColorToPlay()].flush();
				}
			} else { // Mode is QUITTING
				// Do nothing
			}
		} else {
			if (line.length() > 0) {
				crashed = true;
				sgfGameLogBetweenPlayers.println("In " + sgfGameLogBetweenPlayersFilename + ":");
				sgfGameLogBetweenPlayers.println(board);
				sgfGameLogBetweenPlayers.println("Got something other than an acknowledgment: "
							+ line);
				endPrograms();
				while (s.hasNextLine()) {
					sgfGameLogBetweenPlayers.println(s.nextLine());
				}
				sgfGameLogBetweenPlayers.flush();
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
			
			Process[] programs 	= new Process[NUMBER_OF_PLAYER_COLORS];
			
			playerProgramsSTDIN = new PrintWriter[NUMBER_OF_PLAYER_COLORS];
			
			for (int color = 0; color < NUMBER_OF_PLAYER_COLORS; color++) {
				
				ProcessBuilder builder = new ProcessBuilder("nohup", "bash", "-c", playersShellCommands[color], "&");
				
				// redirect the error stream to standard out
				builder.redirectErrorStream(true);
				
				programs[color] = builder.start();
								
				playerProgramsSTDIN[color] = new PrintWriter(
													programs[color].getOutputStream());
				
				// setup a listener to STDOUT of the given program.
				// handleResponse will be called whenever we get a response.
				new Thread(new PlayerListener(color,
											  programs[color].getInputStream(), this)).start();
			}
			
			// Before starting the game, wait 5s for Orego to establish networking
			Thread.sleep(TimeUnit.SECONDS.toMillis(5));
			
			board = new Board();
			mode = REQUESTING_MOVE;
			
			// seed the game by telling black to go first
			playerProgramsSTDIN[BLACK].println("genmove black");
			playerProgramsSTDIN[BLACK].flush();
			
			// Wait for programs to finish.
			// This code is key because otherwise each player would run asynchronously.
			// We are blocking the entire thread while waiting.
			for (int color = 0; color < NUMBER_OF_PLAYER_COLORS; color++) {
				programs[color].waitFor();
			}
			
			if (!crashed) {
				if (winner == -1) { // Game not already resolved by resignation
					winner = board.finalWinner();
				}
			}
			
			for (int c = BLACK; c < NUMBER_OF_PLAYER_COLORS; c++) {
				playerProgramsSTDIN[c].close();
				programs[c].getInputStream().close();
				programs[c].getOutputStream().close();
				programs[c].getErrorStream().close();
				programs[c].destroy();
			}
			
			sgfGameLogBetweenPlayers.close();
		} catch (Throwable e) {
			
			sgfGameLogBetweenPlayers.println("In " + sgfGameLogBetweenPlayersFilename + ":");
			sgfGameLogBetweenPlayers.println(board);
			
			e.printStackTrace(sgfGameLogBetweenPlayers);
			
			sgfGameLogBetweenPlayers.flush();
			sgfGameLogBetweenPlayers.close();
			
			System.exit(1);
		}
		return winner;
	}
	
}
