package orego.play;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.at;
import static orego.core.Coordinates.getBoardWidth;
import static orego.core.Coordinates.pointToString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import orego.book.OpeningBook;
import orego.core.Board;
import orego.heuristic.Heuristic;
import orego.heuristic.HeuristicList;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

// TODO When playing many games against GNU Go, occasionally this class complains that
// GNU Go has made an illegal move. I suspect that Player is actually making illegal
// moves (superko violations).

/** Chooses moves to play. The default implementation is a pure random player. */
public class Player implements Playable {
	
	/** The Board this player plays on. */
	private Board board;

	/** Used to generate moves in bestMove(). */
	private HeuristicList heuristics;
	
	/** Move generator for the opening of the game. */
	private OpeningBook openingBook;

	/** Random number generator. */
	private MersenneTwisterFast random;

	/** A default player with a random policy. */
	public Player() {
		random = new MersenneTwisterFast();
		heuristics = new HeuristicList();
	}

	public int acceptMove(int p) {
		try {
			// If move is legal, play it on the board
			return board.play(p);
		} catch (AssertionError e) {
			System.err.println("Move sequence leading to this error:");
			System.err.print(getMoveSequence());
			System.err.println(pointToString(p));
			System.err.flush();
			throw e;
		}
	}

	/**
	 * Accepts a sequence of moves as returned by getMoveSequence(). The moves
	 * are presumed to alternate starting with the current color to play, and to
	 * all be legal.
	 * 
	 * @param moves a sequence of moves separated by spaces, e.g., "a1 b2 pass".
	 */
	public void acceptMoveSequence(String moves) {
		Scanner s = new Scanner(moves);
		while (s.hasNext()) {
			acceptMove(at(s.next()));
		}
	}

	// TODO Is this delegate method necessary?
	/** Selects and plays a random moves, choosing the heuristically best move, with ties broken randomly. */
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		return heuristics.selectAndPlayOneMove(random, board);
	}

	public int bestMove() {
		int move;
		if (getOpeningBook() != null) { // play from opening book
			move = getOpeningBook().nextMove(getBoard());
			if (move != NO_POINT) {
				return move;
			}
		}
		move = selectAndPlayOneMove(random, board);
		// Replay the move with play() (instead of playFast()) to make sure it's
		// not a superko violation
		undo();
		if (board.isLegal(move)) {
			return move;
		}
		return PASS;
	}

	public double finalScore() {
		return board.finalScore();
	}

	public Board getBoard() {
		return board;
	}

	public Set<String> getCommands() {
		Set<String> result = new TreeSet<String>();
		
		// set any general property
		result.add("orego-set-param");
				
		return result;
	}

	public Set<String> getGoguiCommands() {
		Set<String> result = new TreeSet<String>();
		
		// set any general property
		result.add("none/Set Property/orego-set-param %s %s");
		
		return result;
	}

	/** Returns the array of heuristics associated with this player. */
	public HeuristicList getHeuristics() {
		return heuristics;
	}
	
	/** Returns the number of milliseconds allocated per move. */
	public int getMillisecondsPerMove() {
		return 0; // this player responds almost instantly
	}

	/** Returns the move played at time t, or NO_POINT if t is negative. */
	public int getMove(int t) {
		return board.getMove(t);
	}

	/**
	 * Returns a String containing a human-readable list of all moves made so
	 * far on this board.
	 */
	public String getMoveSequence() {
		return board.getMoveSequence();
	}

	/** Returns this players opening book. */
	public OpeningBook getOpeningBook() {
		return openingBook;
	}

	/** Returns the current turn number. */
	public int getTurn() {
		return board.getTurn();
	}

	public String handleCommand(String command, StringTokenizer arguments) {
		// set any property
		if (command.equals("orego-set-param")) {
			try {
				String prop_name = arguments.nextToken();
				String prop_value = arguments.nextToken();
				setProperty(prop_name, prop_value);
				reset(); // when we change a parameter, we need to restart
				return "Set '" + prop_name + "' to '" + prop_value + "'";
			} catch (Exception e) {
				return e.toString();
			}
		}
		return null;
	}

	public void reset() {
		if (board == null) {
			board = new Board();
		} else { // The new board must have the same komi as the old one
			clearBoardWhilePreservingKomi();
		}
	}

	protected void clearBoardWhilePreservingKomi() {
		double komi = board.getKomi();
		board.clear();
		board.setKomi(komi);
	}

	protected void setBoard(Board board) {
		this.board = board;
	}

	/** Sets the board's color to play. Used only in tests and problems. */
	public void setColorToPlay(int color) {
		board.setColorToPlay(color);
	}

	/** Sets the heuristics. */
	public void setHeuristics(HeuristicList list) {
		this.heuristics = list;
	}

	/** Sets the komi or handicap for the current game. */
	public void setKomi(double komi) {
		board.setKomi(komi);
	}

	/**
	 * Sets the opening book. For testing only -- to use the full book, use
	 * setProperty().
	 */
	public void setOpeningBook(OpeningBook book) {
		openingBook = book;
	}

	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("book")) {
			try {
				String genClass = value;
				if (!genClass.startsWith("orego.")) {
					genClass = "orego.book." + genClass;
				}
				openingBook = (OpeningBook) Class.forName(genClass)
						.newInstance();
				orego.experiment.Debug.debug("Opening book: " + openingBook);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		else {
			getHeuristics().setProperty(property, value); // toss off to the heuristics list
			// remove any heuristics which now have a weight of zero
			getHeuristics().removeZeroWeightedHeuristics();
		}
	}

	public void setRemainingTime(int seconds) {
		// do nothing, this player is almost instant
	}

	/**
	 * Plays all of the stones in diagram, row by row from top to bottom. See
	 * the code of BoardTest for example diagrams.
	 */
	public void setUpProblem(int colorToPlay, String[] diagram) {
		board.setUpProblem(colorToPlay, diagram);
	}

	public void setUpSgf(String filepath, int colorToPlay) {
		System.err.println("filepath: "+filepath+" colorToPlay:"+colorToPlay);
		reset();
		board.setColorToPlay(colorToPlay);
		try {
			File file = new File(filepath);
			BufferedReader bf = new BufferedReader(new FileReader(file));
			String input = "";
			String current = "";
			char[][] ourBoard = new char[getBoardWidth()][getBoardWidth()];
			for (int i = 0; i < getBoardWidth(); i++) {
				for (int j = 0; j < getBoardWidth(); j++) {
					ourBoard[i][j] = '.';
				}
			}
			while ((current = bf.readLine()) != null) {
				input += current;
			}
			bf.close();
			StringTokenizer stoken = new StringTokenizer(input, ";");

				stoken.nextToken();
				stoken.nextToken();

			String boardSetup = stoken.nextToken();
			stoken = new StringTokenizer(boardSetup, "[]()");
			int state = 0;
			String currentToken = "";
			while (stoken.hasMoreTokens()) {
				currentToken = stoken.nextToken();
				assert currentToken.length() == 2 : "The token " + currentToken + " is not exactly two characters long";
				if (currentToken.equals("AB")) {
					// Add black stones (handicap)
					state = 0;
				} else if (currentToken.equals("AW")) {
					// Add white stones
					state = 1;
				} else if (Character.isUpperCase(currentToken.charAt(0))) {
					// Other special SGF codes; ignore
					state = 2;
				} else if (state == 0) {
					if (currentToken.length() == 2) {
						int row = currentToken.charAt(1) - 'a';
						int col = currentToken.charAt(0) - 'a';
						ourBoard[row][col] = '#';
						// place black stone here.
					}
				} else if (state == 1) {
					if (currentToken.length() == 2) {
						int row = currentToken.charAt(1) - 'a';
						int col = currentToken.charAt(0) - 'a';
						ourBoard[row][col] = 'O';
						// place white stone here.
					}
				}
			}
			String[] arrayOfStrings = new String[getBoardWidth()];
			for (int i = 0; i < arrayOfStrings.length; i++) {
				arrayOfStrings[i] = new String(ourBoard[i]);
			}
			getBoard().setUpProblem(colorToPlay, arrayOfStrings);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	@Override
	/** Slightly friendlier than the default; doesn't include the hex address. */
	public String toString() {
		return getClass().getCanonicalName();
	}

	public boolean undo() {
		if (getTurn() == 0) { // Beginning of game, can't undo
			return false;
		} else if (getTurn() == 1) { // Don't want to replay any moves
			clearBoardWhilePreservingKomi();
			return true;
		}
		int[] moves = new int[getTurn() - 1];
		for (int t = 0; t < getTurn() - 1; t++) {
			moves[t] = board.getMove(t);
		}
		clearBoardWhilePreservingKomi();
		for (int p : moves) {
			board.play(p);
		}
		return true;
	}

}
