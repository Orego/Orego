package edu.lclark.orego.neural;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.HistoryObserver;


/**This is how I wrote the book*/
public class BookWritting {
	
	public static void main(String[] args) throws ClassNotFoundException {
		//writeNetwork();	
		DirectNetwork net = null;
		final File directory = new File(OREGO_ROOT + "networks" + File.separator
				+ "neuralbook.data");
		try (ObjectInputStream in = new ObjectInputStream(
				new FileInputStream(directory))) {
			net = (DirectNetwork) in.readObject();
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch(ClassNotFoundException c) {
			System.out.println("The class was not found");
			System.exit(1);
		}
		float[] networkTest = net.getOutputActivations();
		for(int i = 0; i < 19 * 19; i++){
			System.out.printf("%1.4f\t", networkTest[i+1]);
			if(i % 19 == 18){
				System.out.println();
			}
		}
	}

	private static void writeNetwork() {
		Board board = new Board(19);
		DirectNetwork network = new DirectNetwork(board, new HistoryObserver(board));
		network.train(100);
		network.update();
		network.writeBook();
	}
}
