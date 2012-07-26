package orego.play;

import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.at;
import static orego.core.Coordinates.pointToString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import orego.book.OpeningBook;
import orego.core.Board;
import orego.heuristic.Heuristic;
import orego.policy.Policy;
import orego.policy.RandomPolicy;
import ec.util.MersenneTwisterFast;

// TODO When playing many games against GNU Go, occasionally this class complains that
// GNU Go has made an illegal move. I suspect that Player is actually making illegal
// moves (superko violations).

/** Chooses moves to play. The default implementation is a pure random player. */
public class Player implements Playable {

	/** The Board this player plays on. */
	private Board board;

	/** Used to generate moves in bestMove(). */
	private Heuristic[] heuristics;
	
	/** Used to generate moves in bestMove(). */
	private Policy policy;

	/** Move generator for the opening of the game. */
	private OpeningBook openingBook;

	/** Random number generator. */
	private MersenneTwisterFast random;

	/** A default player with a random policy. */
	public Player() {
		random = new MersenneTwisterFast();
		policy = new RandomPolicy();
		heuristics = new Heuristic[0];
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

	public int bestMove() {
		int move;
		if (getOpeningBook() != null) { // play from opening book
			move = getOpeningBook().nextMove(getBoard());
			if (move != NO_POINT) {
				return move;
			}
		}
		move = policy.selectAndPlayOneMove(random, board);
		// Replay the move with play() (instead of playFast()) to make sure it's
		// not a superko violation
		undo();
		if (board.isLegal(move)) {
			return move;
		}
		return PASS;
	}

	public int finalScore() {
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
	public Heuristic[] getHeuristics() {
		return heuristics;
	}
	
	/** Returns the number of milliseconds allocated per move. */
	public int getMillisecondsPerMove() {
		return 0; // this player responds almost instantly
	}

	/** Returns the policy this player uses. */
	public Policy getPolicy() {
		return policy;
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
	public void setHeuristics(Heuristic[] heuristics) {
		this.heuristics = heuristics;
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

	/** Sets the policy. */
	public void setPolicy(Policy policy) {
		assert policy != null : "Policy set to null.";
		this.policy = policy;
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
		} else if (property.equals("policy")) {
			String[] policyClasses = value.split(":");
			Policy prototype = null;
			for (int i = policyClasses.length - 1; i >= 0; i--) {
				String genClass = policyClasses[i];
				if (!genClass.contains(".")) {
					// set default path to policies if it isn't given
					genClass = "orego.policy." + genClass;
				}
				if (!genClass.endsWith("Policy")) {
					// complete the class name if a shortened version is used
					genClass = genClass + "Policy";
				}
				try {
					if (prototype == null) {
						prototype = (Policy) Class.forName(genClass)
								.newInstance();
					} else {
						prototype = (Policy) Class.forName(genClass)
								.getConstructor(Policy.class).newInstance(
										prototype);
					}
				} catch (Exception e) {
					System.err.println("Cannot construct policy: " + value);
					e.printStackTrace();
					System.exit(1);
				}
			}
			setPolicy(prototype);
		} else if (property.equals("heuristic") && ! value.isEmpty()) {
			ArrayList<Heuristic> heuristics = new ArrayList<Heuristic>();
			String[] heuristicClasses = value.split(":");
			for (int i = heuristicClasses.length - 1; i >= 0; i--) {
				String[] heuristicAndWeight = heuristicClasses[i].split("@");
				// if no weight was specified, default to 1
				double weight = heuristicAndWeight.length == 2 ? weight = Double.valueOf(heuristicAndWeight[1]) : 1;
				String genClass = heuristicAndWeight[0];
				if (!genClass.contains(".")) {
					// set default path to heuristic if it isn't given
					genClass = "orego.heuristic." + genClass;
				}
				if (!genClass.endsWith("Heuristic")) {
					// complete the class name if a shortened version is used
					genClass = genClass + "Heuristic";
				}
				try {
					Constructor<?> constructor = Class.forName(genClass).getConstructor(Double.TYPE);
					Heuristic heur = (Heuristic) constructor.newInstance(weight);
					// Heuristics are currently added in the REVERSE of the order
					// in which they were specified. If we ever do anything but
					// look at all of them, we may want to change this.
					heuristics.add(0, heur);
				} catch (Exception e) {
					System.err.println("Cannot construct heuristic: " + value);
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			// now set the instance array variable (we need a type for .toArray)
			setHeuristics(heuristics.toArray(new Heuristic[0]));			
		} else if (property.startsWith("policy.")) { // set a *property* on a given policy
			
			// Command format: gogui-set-param policy.Escape.threshold 21
			if (policy == null) {
				throw new UnsupportedOperationException("No policy exists when setting parameter '" + property + "'");
			}
			
			// TODO: use StringTokenizer for parsing?
			
			StringTokenizer parser = new StringTokenizer(property);
			
			// skipp the 'policy.' prefix
			parser.nextToken(".");
			
			String policy_name = parser.nextToken(".");
			
			String policy_property = parser.nextToken(" ");
			
			
			// now we find the policy matching the policy name
			Policy cur_policy = getPolicy();
			while (cur_policy != null) {
				
				// we strip the "Policy" suffix and make sure it matches the policy_name
				if (cur_policy.getClass().getSimpleName().replace("Policy", "").equals(policy_name)) {
					cur_policy.setProperty(policy_property, value);
					return;
				}
				
				// move down the chain of policies
				cur_policy = getPolicy().getFallback();
			}
				
			throw new UnknownPropertyException("No policy exists for '" + policy_name + "' when setting property '" + policy_property + "'");
		} else {
			throw new UnknownPropertyException(property
					+ " is not a known property");
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
		reset();
		board.setColorToPlay(colorToPlay);
		try {
			File file = new File(filepath);
			BufferedReader bf = new BufferedReader(new FileReader(file));
			String input = "";
			String current = "";
			char[][] ourBoard = new char[BOARD_WIDTH][BOARD_WIDTH];
			for (int i = 0; i < BOARD_WIDTH; i++) {
				for (int j = 0; j < BOARD_WIDTH; j++) {
					ourBoard[i][j] = '.';
				}
			}
			while ((current = bf.readLine()) != null) {
				input += current;
			}
			StringTokenizer stoken = new StringTokenizer(input, ";");
			stoken.nextToken();
			stoken.nextToken();
			String boardSetup = stoken.nextToken();
			stoken = new StringTokenizer(boardSetup, "[]()");
			int state = 0;
			String currentToken = "";
			while (stoken.hasMoreTokens()) {
				currentToken = stoken.nextToken();
				assert currentToken.length() == 2;
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
			String[] arrayOfStrings = new String[BOARD_WIDTH];
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
		board.fillMoves(moves, 0, getTurn() - 2);
		clearBoardWhilePreservingKomi();
		for (int p : moves) {
			board.play(p);
		}
		return true;
	}

}
