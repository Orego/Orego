package edu.lclark.orego.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.PlayerBuilder;
import edu.lclark.orego.sgf.SgfParser;

public class MovePrediction {

	public static void main(String[] args) {
		MovePrediction movePrediction = new MovePrediction();
		SgfParser parser = new SgfParser(CoordinateSystem.forWidth(19), false);
		File folder = new File(
				"/Network/Servers/maccsserver.lclark.edu/Users/mdreyer/Desktop/kgs-19-2013-11-new/");
		File[] files = folder.listFiles();
		for (int i = 0; i < 100; i++) {
			if (files[i].getPath().endsWith(".sgf")) {
				List<List<Short>> games = parser.parseGamesFromFile(files[i], Integer.MAX_VALUE);
				for (List<Short> game : games) {
					movePrediction.processGame(game);
				}
			}
		}
		movePrediction.writeData();
	}

	private double[][] correct;

	private double[][] total;

	private Player mcPlayer;

	private Player shapePlayer;

	private Player shapeMcPlayer;

	public MovePrediction() {
		correct = new double[3][300];
		total = new double[3][300];

		mcPlayer = new PlayerBuilder().build();
		shapePlayer = new PlayerBuilder().shape(true).shapeBias(1000)
				.shapeScalingFactor(0.999f).shapeMinStones(9).msecPerMove(0).build();
		shapeMcPlayer = new PlayerBuilder().shape(true).shapeBias(1000)
				.shapeScalingFactor(0.999f).shapeMinStones(9).build();
	}

	@SuppressWarnings("boxing")
	private void processGame(List<Short> game) {
		mcPlayer.clear();
		shapePlayer.clear();
		shapeMcPlayer.clear();
		for (int i = 0; i < 300; i++) {
			if (i > game.size() - 1) {
				return;
			}
			short realMove = game.get(i);
			if (realMove == mcPlayer.bestMove()) {
				correct[0][i]++;
			}
			if (realMove == shapePlayer.bestMove()) {
				correct[1][i]++;
			}
			if (realMove == shapeMcPlayer.bestMove()) {
				correct[2][i]++;
			}
			for (int j = 0; j < 3; j++) {
				total[j][i]++;
			}
			mcPlayer.acceptMove(realMove);
			shapePlayer.acceptMove(realMove);
			shapeMcPlayer.acceptMove(realMove);
		}
	}
	
	private void writeData(){
		try (PrintWriter writer = new PrintWriter(new File(
				"/Network/Servers/maccsserver.lclark.edu/Users/mdreyer/Desktop/MovePrediction.csv"))) {
			for(int i = 0; i < 300; i++){
				writer.println((correct[0][i] / total[0][i]) + "," + (correct[1][i] / total[1][i]) + "," + (correct[2][i] / total[2][i]));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
