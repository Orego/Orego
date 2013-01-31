package orego.decision;

import static orego.core.Coordinates.at;
import static orego.core.Coordinates.pointToString;
import orego.mcts.*;
import java.io.*;

public class DumpPlayer extends Lgrf2Player {

	private ObjectOutputStream out;

	public void closeFile() {
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		try {
			super.incorporateRun(winner, runnable);
			out.reset();
			out.writeObject(runnable.getBoard().getMoves());
			out.writeInt(runnable.getTurn());
			out.writeInt(winner);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void openFile() {
		try {
			File file = new File("dump.data");
			out = new ObjectOutputStream(new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public boolean testOnce(int[] testMoves) {
		for (int i = 0; i < testMoves.length; i++) {
			if (i % 2 == 1 && (bestMove() != testMoves[i])) {
//				System.out.println("DumpPlayer thought " + pointToString(bestMove()) + " but was supposed to think " + pointToString(testMoves[i]));
				return false;
			}
			acceptMove(testMoves[i]);
		}
		return true;
	}

}
