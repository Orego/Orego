package orego.play;

import java.util.Set;
import java.util.StringTokenizer;
import orego.core.Board;

/**
 * Interface used by orego.ui.Orego to interact with a player (e.g., an
 * orego.play.Player).
 */
public interface Playable {

	/**
	 * Accepts the move if possible.
	 * 
	 * @return one of the PLAY_... constants from orego.core.Board
	 */
	public int acceptMove(int p);

	/** Returns the best move for the color to play. */
	public int bestMove();

	/**
	 * @see orego.core.Board#finalScore()
	 */
	public double finalScore();

	/**
	 * @return the board
	 */
	public Board getBoard();

	/**
	 * Returns the commands handled by this Player. Subclasses should override
	 * this, invoke the super version, and add additional commands to the result
	 * before returning it.
	 */
	public Set<String> getCommands();

	/**
	 * Returns the GoGui commands handled by this Player. Subclasses should
	 * override this, invoke the super version, and add additional commands to
	 * the result before returning it.
	 */
	public Set<String> getGoguiCommands();

	/**
	 * Handles a nonstandard GTP command. Returns the response String, or null
	 * if the command is an error (e.g., is not a known command).
	 */
	public String handleCommand(String command, StringTokenizer arguments);

	/** Clears the board and does anything else necessary to start a new game. */
	public void reset();

	/**
	 * @see orego.core.Board#setKomi(double)
	 */
	public void setKomi(double komi);

	/**
	 * Sets the given property to the given value.
	 */
	public void setProperty(String property, String value)
			throws UnknownPropertyException;

	/**
	 * Adjusts the time per move based on the remaining time in the game. Note
	 * that this in in seconds, not milliseconds.
	 */
	public void setRemainingTime(int seconds);

	/** Resets board, then plays a series of moves from an .sgf file. */
	public void setUpSgf(String filePath, int colorToPlay);

	/**
	 * Undoes the last move if possible.
	 * 
	 * @return true if the undo succeeded.
	 */
	public boolean undo();

}
