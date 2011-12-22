package orego.decision;

import java.io.IOException;

import orego.core.Board;
import orego.util.IntSet;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;

/** Uses information from playouts read by DumpReader to create decision trees. */
public class DumpReader1 extends DumpReader {

	private Node[] roots;
	
	public static void main(String[] args) {
		try {
			String[] problem = { //
					".OO#O....", // 9
					".OO#OOOOO", // 8
					".O######O", // 7
					".O#.#####", // 6
					"OO#####OO", // 5
					"#####.#O.", // 4
					"O######O.", // 3
					"OOOOO#OO.", // 2
					"....O#OO." // 1
			      // ABCDEFGHJ
			};
			int[] testMoves = { at("c1"), at("b1"), at("a7"), at("a8"), at("g9"),
					at("h9"), at("j3"), at("j2") };
			DumpReader1 dump = new DumpReader1();
			dump.process(problem, testMoves, 108);
			System.out.println(dump.roots[at("c1")].toString(""));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public DumpReader1() {
		roots = new Node[LAST_POINT_ON_BOARD + 1];
	}

	public int process(String[] problem, int[] testMoves, int startMove) throws IOException {
			read();
			for (int i = 0; i < LAST_POINT_ON_BOARD + 1; i++) {
				roots[i] = new Node(new DataCollection(), 0);
			}
			for (int i = 0; i < getMoves().size(); i++) {
				for (int t = startMove; t < getTurn().get(i); t++) {
					if (t % 2 == 1) {
						boolean win = getWinner().get(i) == WHITE;
						int move = getMoves().get(i)[t];
						int prev = getMoves().get(i)[t - 1];
						int penult = getMoves().get(i)[t - 2];
//						int antepenult = getMoves().get(i)[t - 3];
						if (move != PASS && prev != PASS) {
							assert move != prev;
						}
//						roots[move].addDatum(new Datum(new int[] {prev, penult, antepenult}, win));
						roots[move].addDatum(new Datum(new int[] {prev, penult}, win));
//						roots[move].addDatum(new Datum(new int[] {prev}, win));
					}
				}
			}
			Board board = new Board();
			board.setUpProblem(BLACK, problem);
			if (board.getTurn() % 2 == 1) {
				// TODO This is a kludge dealing with the inconsistency of
				// move parity and a board's colorToPlay field
				// Should we remove one to avoid redundancy?
				board.setColorToPlay(WHITE);
				board.play(PASS);
			}
			assert ((board.getTurn() % 2 == 0) == (board.getColorToPlay() == BLACK));
			assert (board.getColorToPlay() == BLACK);
			if (testOnce(board, testMoves)) {
				return 1;
			} else {
				return 0;
			}
	}

	public boolean testOnce(Board board, int[] testMoves) {
		for (int i = 0; i < testMoves.length; i++) {
			int move = findBestMove(board);
			if (i % 2 == 1 && (move != testMoves[i])) {
//				System.out.println("Decision trees thought " + pointToString(move) + " but was supposed to think " + pointToString(testMoves[i]));
//				System.out.println("Previous moves: " + pointToString(board.getMove(board.getTurn() - 1)) + ", " + pointToString(board.getMove(board.getTurn() - 2)));
//				System.out.println("For move chosen (" + pointToString(move) + ", " + roots[move].getWinRate(board) + "):\n" + roots[move].toString(""));
//				System.out.println("For correct move (" + pointToString(testMoves[i]) + ", " + roots[testMoves[i]].getWinRate(board) + "):\n" + roots[testMoves[i]].toString(""));
				return false;
			}
			board.play(testMoves[i]);
		}
		return true;
	}

	public int findBestMove(Board board) {
		double highest = 0;
		int highNode = 0;
		IntSet vacant = board.getVacantPoints();
		for (int i = 0; i < vacant.size(); i++) {
			int p = vacant.get(i);
			if (board.isLegal(p)) {
				double winRate = roots[p].getWinRate(board);
				if (highest < winRate) {
					highest = winRate;
					highNode = p;
				}
			}
		}
		return highNode;
	}
	
}
