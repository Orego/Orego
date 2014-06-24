package edu.lclark.orego.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static edu.lclark.orego.core.StoneColor.*;
import edu.lclark.orego.core.Board;

public class PatternSuggesterTest {

	private Board board;
	
	private HistoryObserver history;
	
	private PatternSuggester patterns;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		history = new HistoryObserver(board);
		patterns = new PatternSuggester(board, history);
	}

	@Test
	public void test() {
		String[] diagram = {
				"O....",
				".....",
				"O....",
				".....",
				".....",
		};
		board.setUpProblem(diagram, BLACK);
		board.play("b4");
	}

}
