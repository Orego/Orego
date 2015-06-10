package edu.lclark.orego.neural;

import static edu.lclark.orego.core.StoneColor.*;
import edu.lclark.orego.core.Board;

public class BoardExperiment {

	public static void main(String[] args) {
		new BoardExperiment().run();
	}

	private Board board;

	private int boardInputs;

	private int boardSize;

	BoardExperiment() {
		board = new Board(boardSize);
	}

	private void run() {
		boardSize = 5;
		boardInputs = 4;
		Network smallBoard = new Network(boardSize * boardSize * boardInputs,
				10, 2, boardSize * boardSize);
		double[][] training = new double[3][boardSize * boardSize * boardInputs];
		double[][] trainingCorrect = new double[3][4];
		board = new Board(boardSize);
		String[] before1 = { ".....", ".....", ".....", ".....", ".....", };
		board.setUpProblem(before1, BLACK);
		board.play("a2");
		board.play("b3");
		Extractor extractor = new Extractor(board);
		int p = 0; // place in training array
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				training[0][p] = extractor.isBlack(row, col);
				p++;
			}
		}
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				training[0][p] = extractor.isWhite(row, col);
				p++;
			}
		}
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				training[0][p] = extractor.isUltimateMove(row, col);
				p++;
			}
		}
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				training[0][p] = extractor.isPenultimateMove(row, col);
				p++;
			}
		}
		trainingCorrect[0] = new double[] { 1, 8, 0, 23 };
		String[] before2 = { ".....", ".....", ".....", ".....", ".....", };
		board.setUpProblem(before2, BLACK);
		board.play("b3");
		board.play("a2");
		Extractor extractor2 = new Extractor(board);
		p = 0; // place in training array
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				training[1][p] = extractor2.isBlack(row, col);
				p++;
			}
		}
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				training[1][p] = extractor2.isWhite(row, col);
				p++;
			}
		}
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				training[1][p] = extractor2.isUltimateMove(row, col);
				p++;
			}
		}
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				training[1][p] = extractor2.isPenultimateMove(row, col);
				p++;
			}
		}
		trainingCorrect[1] = new double[] { 1, 23, 0, 8 };

		String[] before3 = { "#....", "#....", ".....", "..OO.", ".....", };
		board.setUpProblem(before3, BLACK);
		Extractor extractor3 = new Extractor(board);
		p = 0; // place in training array
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				training[2][p] = extractor3.isBlack(row, col);
				p++;
			}
		}
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				training[2][p] = extractor3.isWhite(row, col);
				p++;
			}
		}
		trainingCorrect[2] = new double[] { 1, 16, 0, 19 };
		int updates = 1000;
		for (int i = 0; i < updates; i++) {
			int k = (int) (Math.random() * (training.length - 1));
			smallBoard.train(trainingCorrect[k][0],
					(int) trainingCorrect[k][1], training[k]);
			smallBoard.train(trainingCorrect[k][2],
					(int) trainingCorrect[k][3], training[k]);
			// for (int z = 0; z < 3; z++) {
			// for (int j = 0; j < 25; j++) {
			// System.out.print(smallBoard.test(training[z])[j] + "\t");
			// if(j % 5 == 4){
			// System.out.println();
			// }
			// }
			// System.out.println();
			// }
			// System.out.println("---");
		}
		for (int z = 0; z < 2; z++) {
			for (int j = 0; j < 25; j++) {
				System.out.print(smallBoard.test(training[z])[j] + "\t");
				if (j % 5 == 4) {
					System.out.println();
				}
			}
			System.out.println();
		}
	}

}
