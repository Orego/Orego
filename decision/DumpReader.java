package orego.decision;

import java.io.*;
import java.util.ArrayList;

/** Reads in playouts written to disk by DumpPlayer. */
public class DumpReader {

	public static void main(String[] args) {
		try {
			new DumpReader().read();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private ObjectInputStream in;
	
	private ArrayList<int[]> moves;

	private ArrayList<Integer> turn;

	private ArrayList<Integer> winner;

	public void closeFile() {
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public ArrayList<int[]> getMoves() {
		return moves;
	}

	public ArrayList<Integer> getTurn() {
		return turn;
	}

	public ArrayList<Integer> getWinner() {
		return winner;
	}

	public void openFile() {
		try {
			File file = new File("dump.data");
			in = new ObjectInputStream(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void read() throws IOException {
		openFile();
		moves = new ArrayList<int[]>();
		turn = new ArrayList<Integer>();
		winner = new ArrayList<Integer>();
		try {
			while (true) {
				moves.add((int[]) in.readObject());
				int t = in.readInt();
				turn.add(t);
				winner.add(in.readInt());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (EOFException e) {
			closeFile();
		}
	}

}
