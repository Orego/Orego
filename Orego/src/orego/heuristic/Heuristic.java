package orego.heuristic;

import orego.core.Board;

public interface Heuristic {

	public int evaluate(int p, Board board);

}